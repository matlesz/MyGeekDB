import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.util.Log
import ie.matlesz.mygeekdb.BuildConfig


class SeriesViewModel : ViewModel() {

  private val _series = MutableLiveData<List<Series>>() // Holds recommended series
  val series: LiveData<List<Series>> = _series

  private val _searchResults = MutableLiveData<List<Series>>() // Holds search results
  val searchResults: LiveData<List<Series>> = _searchResults

  init {
    fetchSeriesRecommendations()
  }

  /**
   * Fetch recommended TV series.
   */
  fun fetchSeriesRecommendations() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request = Request.Builder()
          .url("https://api.themoviedb.org/4/account/62cf1c5afcf907004dbdae6e/tv/recommendations?page=1&language=en-US")
          .get()
          .addHeader("accept", "application/json")
          .addHeader("Authorization", apiKey)
          .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
          response.body?.let { responseBody ->
            val json = JSONObject(responseBody.string())
            val seriesJsonArray = json.optJSONArray("results")
            val seriesList = mutableListOf<Series>()

            seriesJsonArray?.let {
              for (i in 0 until it.length()) {
                val seriesJson = it.getJSONObject(i)
                val id = seriesJson.optInt("id", -1)
                val title = seriesJson.optString("name", "N/A") // "name" is used for TV series
                val overview = seriesJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = seriesJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = seriesJson.optDouble("vote_average", 0.0)
                val voteCount = seriesJson.optInt("vote_count", 0)
                val popularity = seriesJson.optDouble("popularity", 0.0)

                seriesList.add(
                  Series(
                    id = id,
                    title = title,
                    overview = overview,
                    posterPath = posterPath,
                    thumbsUp = 0,
                    voteAverage = voteAverage,
                    voteCount = voteCount,
                    popularity = popularity
                  )
                )
              }
            }

            Log.d("SeriesViewModel", "Fetched recommended series: ${seriesList.size}")
            _series.postValue(seriesList)
          }
        } else {
          Log.e("SeriesViewModel", "Recommendations request failed: ${response.code}, ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Exception during recommendations fetch: ${e.message}", e)
      }
    }
  }
  fun toggleFavorite(series: Series) {
    // Update the favorite status directly in the LiveData list
    _series.value = _series.value?.map {
      if (it.id == series.id) {
        it.copy(isFavorite = !it.isFavorite)
      } else {
        it
      }
    }
  }
  /**
   * Search TV series by query.
   * @param query The search query string.
   */
  fun searchSeries(query: String) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request = Request.Builder()
          .url("https://api.themoviedb.org/3/search/tv?query=$query&language=en-US&page=1&include_adult=false")
          .get()
          .addHeader("accept", "application/json")
          .addHeader("Authorization", apiKey)
          .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
          response.body?.let { responseBody ->
            val json = JSONObject(responseBody.string())
            val seriesJsonArray = json.optJSONArray("results")
            val seriesList = mutableListOf<Series>()

            seriesJsonArray?.let {
              for (i in 0 until it.length()) {
                val seriesJson = it.getJSONObject(i)
                val id = seriesJson.optInt("id", -1)
                val title = seriesJson.optString("name", "N/A")
                val overview = seriesJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = seriesJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = seriesJson.optDouble("vote_average", 0.0)
                val voteCount = seriesJson.optInt("vote_count", 0)
                val popularity = seriesJson.optDouble("popularity", 0.0)

                seriesList.add(
                  Series(
                    id = id,
                    title = title,
                    overview = overview,
                    posterPath = posterPath,
                    thumbsUp = 0,
                    voteAverage = voteAverage,
                    voteCount = voteCount,
                    popularity = popularity
                  )
                )
              }
            }

            Log.d("SeriesViewModel", "Fetched search results: ${seriesList.size}")
            _searchResults.postValue(seriesList)
          }
        } else {
          Log.e("SeriesViewModel", "Search request failed: ${response.code}, ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Exception during search: ${e.message}", e)
      }
    }
  }
}