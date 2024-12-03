package ie.matlesz.mygeekdb.viewmodel

import Series
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ie.matlesz.mygeekdb.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SeriesViewModel(application: Application) : AndroidViewModel(application) {
  private val _series = MutableLiveData<List<Series>>(emptyList())
  val series: LiveData<List<Series>> = _series

  private val _favorites = MutableLiveData<List<Series>>(emptyList())
  val favorites: LiveData<List<Series>> = _favorites

  private val sharedPreferences =
          application.getSharedPreferences("series_preferences", Context.MODE_PRIVATE)
  private val gson = Gson()

  init {
    loadSavedFavorites()
    fetchSeriesRecommendations()
  }

  private val _searchResults = MutableLiveData<List<Series>>(emptyList())
  val searchResults: LiveData<List<Series>> = _searchResults

  private fun loadSavedFavorites() {
    val favoritesJson = sharedPreferences.getString("favorites", null)
    if (favoritesJson != null) {
      val type = object : TypeToken<List<Series>>() {}.type
      val savedFavorites = gson.fromJson<List<Series>>(favoritesJson, type)
      _favorites.value = savedFavorites
    }
  }

  private fun saveFavorites(favorites: List<Series>) {
    val favoritesJson = gson.toJson(favorites)
    sharedPreferences.edit().putString("favorites", favoritesJson).apply()
  }

  fun toggleFavorite(series: Series) {
    val currentFavorites = _favorites.value.orEmpty()
    val updatedSeries = series.copy(isFavorite = !series.isFavorite)

    val newFavorites =
            if (currentFavorites.any { it.id == series.id }) {
              currentFavorites.filter { it.id != series.id }
            } else {
              currentFavorites + updatedSeries
            }

    _favorites.value = newFavorites
    saveFavorites(newFavorites)

    // Update the series in the main list as well
    _series.value = _series.value?.map { if (it.id == series.id) updatedSeries else it }
  }

  fun isFavorite(series: Series): Boolean {
    return _favorites.value?.any { it.id == series.id } == true
  }

  fun fetchSeriesRecommendations() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request =
                Request.Builder()
                        .url(
                                "https://api.themoviedb.org/4/account/62cf1c5afcf907004dbdae6e/tv/recommendations?page=1&language=en-US"
                        )
                        .get()
                        .addHeader("accept", "application/json")
                        .addHeader("Authorization", apiKey)
                        .build()

        client.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            val resultsArray = jsonObject.getJSONArray("results")

            val seriesList = mutableListOf<Series>()
            for (i in 0 until resultsArray.length()) {
              val seriesObj = resultsArray.getJSONObject(i)
              val series =
                      Series(
                              id = seriesObj.getInt("id"),
                              title = seriesObj.getString("name"),
                              overview = seriesObj.getString("overview"),
                              posterPath =
                                      "https://image.tmdb.org/t/p/w500${seriesObj.getString("poster_path")}",
                              thumbsUp = 0,
                              voteAverage = seriesObj.getDouble("vote_average"),
                              voteCount = seriesObj.getInt("vote_count"),
                              popularity = seriesObj.getDouble("popularity"),
                              isFavorite =
                                      _favorites.value?.any { it.id == seriesObj.getInt("id") } ==
                                              true
                      )
              seriesList.add(series)
            }

            _series.postValue(seriesList)
          }
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Error fetching recommendations: ${e.message}", e)
      }
    }
  }

  fun searchSeries(query: String) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")

        val request =
                Request.Builder()
                        .url(
                                "https://api.themoviedb.org/3/search/tv?query=$encodedQuery&include_adult=false&language=en-US&page=1"
                        )
                        .get()
                        .addHeader("accept", "application/json")
                        .addHeader("Authorization", apiKey)
                        .build()

        client.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            val resultsArray = jsonObject.getJSONArray("results")

            val seriesList = mutableListOf<Series>()
            for (i in 0 until resultsArray.length()) {
              val seriesObj = resultsArray.getJSONObject(i)
              val posterPath = seriesObj.optString("poster_path")
              val series =
                      Series(
                              id = seriesObj.getInt("id"),
                              title = seriesObj.getString("name"),
                              overview = seriesObj.getString("overview"),
                              posterPath =
                                      if (posterPath.isNotEmpty())
                                              "https://image.tmdb.org/t/p/w500$posterPath"
                                      else "https://via.placeholder.com/500x750.png?text=No+Poster",
                              thumbsUp = 0,
                              voteAverage = seriesObj.getDouble("vote_average"),
                              voteCount = seriesObj.getInt("vote_count"),
                              popularity = seriesObj.getDouble("popularity"),
                              isFavorite = _favorites.value?.any { it.id == seriesObj.getInt("id") } == true
                      )
              seriesList.add(series)
            }

            _searchResults.postValue(seriesList)
          }
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Error searching series: ${e.message}", e)
      }
    }
  }
}
