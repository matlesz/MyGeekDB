import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.matlesz.mygeekdb.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MovieViewModel : ViewModel() {

  // LiveData to hold recommended movies
  private val _movies = MutableLiveData<List<Movie>>()
  val movies: LiveData<List<Movie>> = _movies

  // LiveData to hold search results
  private val _searchResults = MutableLiveData<List<Movie>>()
  val searchResults: LiveData<List<Movie>> = _searchResults

  init {
    fetchMovieRecommendations() // Load recommendations on initialization
  }

  /**
   * Fetch recommended movies.
   */
  fun fetchMovieRecommendations() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request = Request.Builder()
          .url("https://api.themoviedb.org/4/account/62cf1c5afcf907004dbdae6e/movie/recommendations?page=1&language=en-US")
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

            if (moviesJsonArray != null) {
              for (i in 0 until moviesJsonArray.length()) {
                val movieJson = moviesJsonArray.getJSONObject(i)
                val id = movieJson.optInt("id", -1).toString()
                val title = movieJson.optString("title", "N/A")
                val overview = movieJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = movieJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = movieJson.optDouble("vote_average", 0.0)
                val voteCount = movieJson.optInt("vote_count", 0)
                val popularity = movieJson.optDouble("popularity", 0.0)

                movieList.add(
                  Movie(
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
            } else {
              Log.e("MovieViewModel", "No 'results' array in API response.")
            }

            Log.d("MovieViewModel", "Fetched ${movieList.size} recommended movies.")
            _movies.postValue(movieList)
          }
        } else {
          Log.e("MovieViewModel", "Failed to fetch recommendations: ${response.code}, ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Exception during fetchMovieRecommendations: ${e.message}", e)
      }
    }
  }
  fun toggleFavorite(movie: Movie) {
    // Update the favorite status directly in the LiveData list
    _movies.value = _movies.value?.map {
      if (it.id == movie.id) {
        it.copy(isFavorite = !it.isFavorite)
      } else {
        it
      }
    }
  }
  /**
   * Search movies by query.
   * @param query The search query string.
   */
  fun searchMovies(query: String) {
    if (query.isBlank()) {
      _searchResults.postValue(emptyList())
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val client = OkHttpClient()
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"

        val request = Request.Builder()
          .url("https://api.themoviedb.org/3/search/movie?query=$query&language=en-US&page=1&include_adult=false")
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
                val id = movieJson.optInt("id", -1).toString() // Convert Int to String
                val title = movieJson.optString("title", "N/A")
                val overview = movieJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = movieJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = movieJson.optDouble("vote_average", 0.0)
                val voteCount = movieJson.optInt("vote_count", 0)
                val popularity = movieJson.optDouble("popularity", 0.0)

                movieList.add(
                  Movie(
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

            Log.d("MovieViewModel", "Fetched search results: ${movieList.size}")
            _searchResults.postValue(movieList)
          }
        } else {
          Log.e("MovieViewModel", "Search request failed: ${response.code}, ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Exception during search: ${e.message}", e)
      }
    }
  }
}