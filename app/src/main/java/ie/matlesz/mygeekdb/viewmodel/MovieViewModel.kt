package ie.matlesz.mygeekdb.viewmodel

import Movie
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MovieViewModel(application: Application) : AndroidViewModel(application) {
  private val _movies = MutableLiveData<List<Movie>>(emptyList())
  val movies: LiveData<List<Movie>> = _movies

  private val _favorites = MutableLiveData<List<Movie>>(emptyList())
  val favorites: LiveData<List<Movie>> = _favorites

  private val _searchResults = MutableLiveData<List<Movie>>(emptyList())
  val searchResults: LiveData<List<Movie>> = _searchResults

  private val sharedPreferences = application.getSharedPreferences("movie_preferences", Context.MODE_PRIVATE)
  private val gson = Gson()
  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

  init {
    loadFavoritesFromFirestore()
    fetchMovieRecommendations()
  }

  private fun loadFavoritesFromFirestore() {
    auth.currentUser?.let { user ->
      db.collection("users")
        .document(user.uid)
        .collection("favoriteMovies")
        .get()
        .addOnSuccessListener { documents ->
          val movieList = documents.mapNotNull { doc ->
            doc.toObject(Movie::class.java)
          }
          _favorites.value = movieList
        }
        .addOnFailureListener { e ->
          Log.e("MovieViewModel", "Error loading favorites: ", e)
        }
    }
  }

  private fun saveFavoritesToFirestore(favorites: List<Movie>) {
    auth.currentUser?.let { user ->
      val batch = db.batch()
      val userFavoritesRef = db.collection("users").document(user.uid)

      // Delete existing favorites
      userFavoritesRef.collection("favoriteMovies")
        .get()
        .addOnSuccessListener { documents ->
          documents.forEach { doc ->
            batch.delete(doc.reference)
          }

          // Add new favorites
          favorites.forEach { movie ->
            val docRef = userFavoritesRef.collection("favoriteMovies").document(movie.id)
            batch.set(docRef, movie)
          }

          batch.commit()
            .addOnFailureListener { e ->
              Log.e("MovieViewModel", "Error saving favorites: ", e)
            }
        }
    }
  }

  fun toggleFavorite(movie: Movie) {
    val currentFavorites = _favorites.value.orEmpty()
    val updatedMovie = movie.copy(isFavorite = !movie.isFavorite)

    val newFavorites =
            if (currentFavorites.any { it.id == movie.id }) {
              currentFavorites.filter { it.id != movie.id }
            } else {
              currentFavorites + updatedMovie
            }

    _favorites.value = newFavorites
    saveFavoritesToFirestore(newFavorites)

    // Update the movie in the main list as well
    _movies.value = _movies.value?.map { if (it.id == movie.id) updatedMovie else it }
    _searchResults.value = _searchResults.value?.map { if (it.id == movie.id) updatedMovie else it }
  }

  fun isFavorite(movie: Movie): Boolean {
    return _favorites.value?.any { it.id == movie.id } == true
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
}
