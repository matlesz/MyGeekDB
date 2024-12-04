package ie.matlesz.mygeekdb.viewmodel

import Movie
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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MovieViewModel(application: Application) : AndroidViewModel(application) {
  private val _movies = MutableLiveData<List<Movie>>(emptyList())
  val movies: LiveData<List<Movie>> = _movies

  private val _favorites = MutableLiveData<List<Movie>>(emptyList())
  val favorites: LiveData<List<Movie>> = _favorites

  private val _searchResults = MutableLiveData<List<Movie>>(emptyList())
  val searchResults: LiveData<List<Movie>> = _searchResults

  private val sharedPreferences =
          application.getSharedPreferences("movie_preferences", Context.MODE_PRIVATE)
  private val gson = Gson()
  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

  init {
    loadFavoritesFromFirestore()
    fetchMovieRecommendations()
  }

  private fun loadFavoritesFromFirestore() {
    val userId = auth.currentUser?.uid ?: return

    db.collection("users").document(userId).collection("favoriteMovies").addSnapshotListener {
            snapshot,
            e ->
      if (e != null) {
        Log.e("Firestore", "Error loading favorites", e)
        return@addSnapshotListener
      }

      snapshot?.let { documents ->
        val movieList = documents.mapNotNull { doc -> doc.toObject(Movie::class.java) }
        _favorites.value = movieList
      }
    }
  }

  private fun saveFavoritesToFirestore(favorites: List<Movie>) {
    val userId = auth.currentUser?.uid
    Log.d("MovieViewModel", "Saving ${favorites.size} favorites to Firestore for user: $userId")

    if (userId == null) {
      Log.e("MovieViewModel", "No user ID available for saving favorites")
      return
    }

    val userFavoritesRef = db.collection("users").document(userId).collection("favoriteMovies")

    userFavoritesRef
            .get()
            .addOnSuccessListener { snapshot ->
              Log.d("MovieViewModel", "Current Firestore documents count: ${snapshot.size()}")
              val batch = db.batch()

              // Delete existing documents
              snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
                Log.d("MovieViewModel", "Queued delete for document: ${doc.id}")
              }

              // Add new favorites
              favorites.forEach { movie ->
                val docRef = userFavoritesRef.document(movie.id.toString())
                batch.set(docRef, movie)
                Log.d("MovieViewModel", "Queued save for movie: ${movie.title}")
              }

              // Commit the batch
              batch.commit()
                      .addOnSuccessListener {
                        Log.d("MovieViewModel", "Successfully saved all favorites to Firestore")
                      }
                      .addOnFailureListener { e ->
                        Log.e("MovieViewModel", "Error saving favorites to Firestore", e)
                      }
            }
            .addOnFailureListener { e ->
              Log.e("MovieViewModel", "Error getting current favorites from Firestore", e)
            }
  }

  fun toggleFavorite(movie: Movie) {
    val userId = auth.currentUser?.uid
    Log.d("MovieViewModel", "Toggling favorite for movie: ${movie.title}, userId: $userId")

    if (userId == null) {
      Log.e("MovieViewModel", "No user ID available")
      return
    }

    val currentFavorites = _favorites.value.orEmpty()
    val updatedMovie = movie.copy(isFavorite = !movie.isFavorite)

    val newFavorites =
            if (currentFavorites.any { it.id == movie.id }) {
              Log.d("MovieViewModel", "Removing movie from favorites: ${movie.title}")
              currentFavorites.filter { it.id != movie.id }
            } else {
              Log.d("MovieViewModel", "Adding movie to favorites: ${movie.title}")
              currentFavorites + updatedMovie
            }

    _favorites.value = newFavorites

    // Update individual item in Firestore
    val movieRef =
            db.collection("users")
                    .document(userId)
                    .collection("favoriteMovies")
                    .document(movie.id.toString())

    if (!movie.isFavorite) { // Changed condition to match the intended behavior
      movieRef.set(updatedMovie)
              .addOnSuccessListener {
                Log.d("MovieViewModel", "Successfully saved movie to Firestore: ${movie.title}")
              }
              .addOnFailureListener { e ->
                Log.e("MovieViewModel", "Error saving movie to Firestore: ${movie.title}", e)
              }
    } else {
      movieRef.delete()
              .addOnSuccessListener {
                Log.d("MovieViewModel", "Successfully removed movie from Firestore: ${movie.title}")
              }
              .addOnFailureListener { e ->
                Log.e("MovieViewModel", "Error removing movie from Firestore: ${movie.title}", e)
              }
    }

    // Update local lists
    _movies.value = _movies.value?.map { if (it.id == movie.id) updatedMovie else it }
    _searchResults.value = _searchResults.value?.map { if (it.id == movie.id) updatedMovie else it }
  }

  fun isFavorite(movie: Movie): Boolean {
    return _favorites.value?.any { it.id == movie.id } == true
  }

  fun removeFavorite(item: Any) {
    viewModelScope.launch {
      try {
        val userId = auth.currentUser?.uid ?: return@launch

        when (item) {
          is Movie -> {
            db.collection("users")
                    .document(userId)
                    .collection("favoriteMovies")
                    .document(item.id.toString())
                    .delete()
                    .await()

            // Update local favorites list
            _favorites.value = _favorites.value?.filter { it.id != item.id }
            // Update movie's favorite status in other lists
            _movies.value =
                    _movies.value?.map { if (it.id == item.id) it.copy(isFavorite = false) else it }
            _searchResults.value =
                    _searchResults.value?.map {
                      if (it.id == item.id) it.copy(isFavorite = false) else it
                    }
          }
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Error removing favorite", e)
      }
    }
  }

  fun fetchMovieRecommendations() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request =
                Request.Builder()
                        .url(
                                "https://api.themoviedb.org/4/account/62cf1c5afcf907004dbdae6e/movie/recommendations?page=1&language=en-US"
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

            val moviesList = mutableListOf<Movie>()
            for (i in 0 until resultsArray.length()) {
              val movieObj = resultsArray.getJSONObject(i)
              val movie =
                      Movie(
                              id = movieObj.getString("id"),
                              title = movieObj.getString("title"),
                              overview = movieObj.getString("overview"),
                              posterPath =
                                      "https://image.tmdb.org/t/p/w500${movieObj.getString("poster_path")}",
                              thumbsUp = 0,
                              voteAverage = movieObj.getDouble("vote_average"),
                              voteCount = movieObj.getInt("vote_count"),
                              popularity = movieObj.getDouble("popularity"),
                              isFavorite =
                                      _favorites.value?.any { it.id == movieObj.getString("id") } ==
                                              true
                      )
              moviesList.add(movie)
            }

            _movies.postValue(moviesList)
          }
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Error fetching recommendations: ${e.message}", e)
      }
    }
  }

  fun searchResults(query: String) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")

        val request =
                Request.Builder()
                        .url(
                                "https://api.themoviedb.org/3/search/movie?query=$encodedQuery&include_adult=false&language=en-US&page=1"
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

            val moviesList = mutableListOf<Movie>()
            for (i in 0 until resultsArray.length()) {
              val movieObj = resultsArray.getJSONObject(i)
              val posterPath = movieObj.optString("poster_path")
              val movie =
                      Movie(
                              id = movieObj.getString("id"),
                              title = movieObj.getString("title"),
                              overview = movieObj.getString("overview"),
                              posterPath =
                                      if (posterPath.isNotEmpty())
                                              "https://image.tmdb.org/t/p/w500$posterPath"
                                      else "https://via.placeholder.com/500x750.png?text=No+Poster",
                              thumbsUp = 0,
                              voteAverage = movieObj.getDouble("vote_average"),
                              voteCount = movieObj.getInt("vote_count"),
                              popularity = movieObj.getDouble("popularity"),
                              isFavorite =
                                      _favorites.value?.any { it.id == movieObj.getString("id") } ==
                                              true
                      )
              moviesList.add(movie)
            }

            _searchResults.postValue(moviesList)
          }
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Error searching movies: ${e.message}", e)
      }
    }
  }

  private fun loadFavorites() {
    viewModelScope.launch {
      try {
        val userId = auth.currentUser?.uid ?: return@launch
        val snapshot =
                db.collection("users").document(userId).collection("favoriteMovies").get().await()

        val movieList = snapshot.documents.mapNotNull { doc -> doc.toObject(Movie::class.java) }
        _favorites.value = movieList
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Error loading favorites", e)
      }
    }
  }

  fun getCurrentUserId(): String? = auth.currentUser?.uid
}
