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

// Movie Data Class
data class Movie(
  val title: String,
  val overview: String,
  val posterPath: String,
  var thumbsUp: Int, // Allow dynamic updates
  val voteAverage: Double
)

class MovieViewModel : ViewModel() {

  private val _movies = MutableLiveData<List<Movie>>()
  val movies: LiveData<List<Movie>> = _movies

  private val _searchResults = MutableLiveData<List<Movie>>()
  val searchResults: LiveData<List<Movie>> = _searchResults

  private val _favorites = MutableLiveData<List<Movie>>()
  val favorites: LiveData<List<Movie>> = _favorites

  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean> = _isLoading

  private val _errorMessage = MutableLiveData<String?>()
  val errorMessage: LiveData<String?> = _errorMessage

  private val client = OkHttpClient()
  private val baseImageUrl = "https://image.tmdb.org/t/p/w500"

  init {
    fetchMovieRecommendations(1)
  }

  /**
   * Fetch search results from TMDb for the given query.
   * @param query The search term to query for.
   */
  fun searchMovies(query: String) {
    if (query.isBlank()) {
      _searchResults.postValue(emptyList()) // Clear search results for empty query
      return
    }

    _isLoading.postValue(true)
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"
        val request = Request.Builder()
          .url("https://api.themoviedb.org/3/search/movie?query=$query&language=en-US")
          .get()
          .addHeader("accept", "application/json")
          .addHeader("Authorization", apiKey)
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          response.body?.let { responseBody ->
            val json = JSONObject(responseBody.string())
            val moviesJsonArray = json.optJSONArray("results")
            val movieList = mutableListOf<Movie>()

            moviesJsonArray?.let {
              for (i in 0 until it.length()) {
                val movieJson = it.getJSONObject(i)
                val title = movieJson.optString("title", "N/A")
                val overview = movieJson.optString("overview", "N/A")
                val posterPath = movieJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = movieJson.optDouble("vote_average", 0.0)
                movieList.add(Movie(title, overview, posterPath, thumbsUp = 0, voteAverage = voteAverage))
              }
            }

            _searchResults.postValue(movieList)
            _errorMessage.postValue(null)
          }
        } else {
          Log.e("MovieViewModel", "Search request failed: ${response.code}, ${response.message}")
          _errorMessage.postValue("Error: ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Exception during search: ${e.message}", e)
        _errorMessage.postValue("Exception: ${e.message}")
      } finally {
        _isLoading.postValue(false)
      }
    }
  }
  fun fetchMovieRecommendations(page: Int) {
    _isLoading.postValue(true)
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"
        val request = Request.Builder()
          .url("https://api.themoviedb.org/4/account/62cf1c5afcf907004dbdae6e/movie/recommendations?page=$page&language=en-US")
          .get()
          .addHeader("accept", "application/json")
          .addHeader("Authorization", apiKey)
          .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
          response.body?.let { responseBody ->
            val json = JSONObject(responseBody.string())
            val moviesJsonArray = json.optJSONArray("results")
            val movieList = mutableListOf<Movie>()

            moviesJsonArray?.let {
              for (i in 0 until it.length()) {
                val movieJson = it.getJSONObject(i)
                val title = movieJson.optString("title", "N/A")
                val overview = movieJson.optString("overview", "N/A")
                val posterPath = movieJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = movieJson.optDouble("vote_average", 0.0)
                movieList.add(Movie(title, overview, posterPath, thumbsUp = 0, voteAverage = voteAverage))
              }
            }

            _movies.postValue(movieList)
          }
        } else {
          _errorMessage.postValue("Failed to fetch recommendations: ${response.message}")
        }
      } catch (e: Exception) {
        _errorMessage.postValue("Error: ${e.message}")
      } finally {
        _isLoading.postValue(false)
      }
    }
  }
}

//  /**
//   * Load the next page of movies.
//   */
//  fun loadNextPage() {
//    currentPage++
//    fetchMovieRecommendations(currentPage)
//  }
//
//  /**
//   * Update thumbs up count for a specific movie.
//   * @param movie The movie to update.
//   * @param increment If true, increment thumbs up. Otherwise, decrement.
//   */
//  fun updateThumbsUp(movie: Movie, increment: Boolean) {
//    viewModelScope.launch(Dispatchers.IO) {
//      val currentMovies = _movies.value?.toMutableList() ?: return@launch
//      val movieIndex = currentMovies.indexOfFirst { it.title == movie.title }
//      if (movieIndex != -1) {
//        val updatedMovie = currentMovies[movieIndex].copy(
//          thumbsUp = if (increment) currentMovies[movieIndex].thumbsUp + 1 else currentMovies[movieIndex].thumbsUp - 1
//        )
//        currentMovies[movieIndex] = updatedMovie
//        _movies.postValue(currentMovies)
//      }
//    }
//  }
//
//  /**
//   * Toggle favorite status for a movie.
//   */
//  fun toggleFavorite(movie: Movie) {
//    val currentFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
//    if (currentFavorites.contains(movie)) {
//      currentFavorites.remove(movie)
//    } else {
//      currentFavorites.add(movie)
//    }
//    _favorites.postValue(currentFavorites)
//  }
//}