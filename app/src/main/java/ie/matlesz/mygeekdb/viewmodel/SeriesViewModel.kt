package ie.matlesz.mygeekdb.viewmodel

import Series
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import ie.matlesz.mygeekdb.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

  init {
    loadFavoritesFromFirestore()
    fetchSeriesRecommendations()
  }

  private val _searchResults = MutableLiveData<List<Series>>(emptyList())
  val searchResults: LiveData<List<Series>> = _searchResults

  private fun loadFavoritesFromFirestore() {
    val userId = auth.currentUser?.uid ?: return

    db.collection("users").document(userId).collection("favoriteSeries").addSnapshotListener {
            snapshot,
            e ->
      if (e != null) {
        Log.e("Firestore", "Error loading favorites", e)
        return@addSnapshotListener
      }

      snapshot?.let { documents ->
        val seriesList = documents.mapNotNull { doc -> doc.toObject(Series::class.java) }
        _favorites.value = seriesList
      }
    }
  }

  private fun saveFavoritesToFirestore(favorites: List<Series>) {
    val userId = auth.currentUser?.uid
    Log.d("SeriesViewModel", "Saving ${favorites.size} favorites to Firestore for user: $userId")

    if (userId == null) {
      Log.e("SeriesViewModel", "No user ID available for saving favorites")
      return
    }

    val userFavoritesRef = db.collection("users").document(userId).collection("favoriteSeries")

    userFavoritesRef
            .get()
            .addOnSuccessListener { snapshot ->
              Log.d("SeriesViewModel", "Current Firestore documents count: ${snapshot.size()}")
              val batch = db.batch()

              // Delete existing documents
              snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
                Log.d("SeriesViewModel", "Queued delete for document: ${doc.id}")
              }

              // Add new favorites
              favorites.forEach { series ->
                val docRef = userFavoritesRef.document(series.id.toString())
                batch.set(docRef, series)
                Log.d("SeriesViewModel", "Queued save for series: ${series.title}")
              }

              // Commit the batch
              batch.commit()
                      .addOnSuccessListener {
                        Log.d("SeriesViewModel", "Successfully saved all favorites to Firestore")
                      }
                      .addOnFailureListener { e ->
                        Log.e("SeriesViewModel", "Error saving favorites to Firestore", e)
                      }
            }
            .addOnFailureListener { e ->
              Log.e("SeriesViewModel", "Error getting current favorites from Firestore", e)
            }
  }

  fun toggleFavorite(series: Series) {
    val userId = auth.currentUser?.uid
    Log.d("SeriesViewModel", "Toggling favorite for series: ${series.title}, userId: $userId")

    if (userId == null) {
      Log.e("SeriesViewModel", "No user ID available")
      return
    }

    val currentFavorites = _favorites.value.orEmpty()
    val updatedSeries = series.copy(isFavorite = !series.isFavorite)

    val newFavorites =
            if (currentFavorites.any { it.id == series.id }) {
              Log.d("SeriesViewModel", "Removing series from favorites: ${series.title}")
              currentFavorites.filter { it.id != series.id }
            } else {
              Log.d("SeriesViewModel", "Adding series to favorites: ${series.title}")
              currentFavorites + updatedSeries
            }

    _favorites.value = newFavorites
    Log.d("SeriesViewModel", "Current favorites count: ${newFavorites.size}")

    // Save to Firestore
    saveFavoritesToFirestore(newFavorites)

    // Update individual item in Firestore
    val seriesRef =
            db.collection("users")
                    .document(userId)
                    .collection("favoriteSeries")
                    .document(series.id.toString())

    if (updatedSeries.isFavorite) {
      seriesRef
              .set(updatedSeries)
              .addOnSuccessListener {
                Log.d("SeriesViewModel", "Successfully saved series to Firestore: ${series.title}")
              }
              .addOnFailureListener { e ->
                Log.e("SeriesViewModel", "Error saving series to Firestore: ${series.title}", e)
              }
    } else {
      seriesRef
              .delete()
              .addOnSuccessListener {
                Log.d(
                        "SeriesViewModel",
                        "Successfully removed series from Firestore: ${series.title}"
                )
              }
              .addOnFailureListener { e ->
                Log.e("SeriesViewModel", "Error removing series from Firestore: ${series.title}", e)
              }
    }

    // Update local lists
    _series.value = _series.value?.map { if (it.id == series.id) updatedSeries else it }
    _searchResults.value =
            _searchResults.value?.map { if (it.id == series.id) updatedSeries else it }
  }

  fun isFavorite(series: Series): Boolean {
    return _favorites.value?.any { it.id == series.id } ?: false
  }

  fun removeFavorite(item: Any) {
    viewModelScope.launch {
      try {
        val userId = auth.currentUser?.uid ?: return@launch

        when (item) {
          is Series -> {
            db.collection("users")
                    .document(userId)
                    .collection("favoriteSeries")
                    .document(item.id.toString())
                    .delete()
                    .await()

            // Update local favorites list
            _favorites.value = _favorites.value?.filter { it.id != item.id }
            // Update series' favorite status in other lists
            _series.value =
                    _series.value?.map { if (it.id == item.id) it.copy(isFavorite = false) else it }
            _searchResults.value =
                    _searchResults.value?.map {
                      if (it.id == item.id) it.copy(isFavorite = false) else it
                    }
          }
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Error removing favorite", e)
      }
    }
  }

  fun fetchSeriesRecommendations() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request =
                Request.Builder()
                        .url("https://api.themoviedb.org/3/tv/popular?language=en-US&page=1")
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
                              voteAverage = seriesObj.getDouble("vote_average"),
                              voteCount = seriesObj.getInt("vote_count"),
                              popularity = seriesObj.getDouble("popularity"),
                              isFavorite =
                                      isFavorite(
                                              Series(
                                                      id = seriesObj.getInt("id"),
                                                      title = seriesObj.getString("name"),
                                                      overview = seriesObj.getString("overview"),
                                                      posterPath = posterPath
                                              )
                                      )
                      )
              seriesList.add(series)
            }
            withContext(Dispatchers.Main) { _series.value = seriesList }
          }
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Error fetching series recommendations", e)
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
                              isFavorite =
                                      _favorites.value?.any { it.id == seriesObj.getInt("id") } ==
                                              true
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
