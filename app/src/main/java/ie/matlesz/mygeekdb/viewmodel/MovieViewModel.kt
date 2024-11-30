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

            moviesJsonArray?.let {
              for (i in 0 until it.length()) {
                val movieJson = it.getJSONObject(i)
                val title = movieJson.optString("title", "N/A")
                val overview = movieJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = movieJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = movieJson.optDouble("vote_average", 0.0)
                movieList.add(Movie(title, overview, posterPath, thumbsUp = 0, voteAverage = voteAverage))
              }
            }

            Log.d("MovieViewModel", "Fetched recommended movies: ${movieList.size}")
            _movies.postValue(movieList)
          }
        } else {
          Log.e("MovieViewModel", "Recommendations request failed: ${response.code}, ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("MovieViewModel", "Exception during recommendations fetch: ${e.message}", e)
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
                val title = movieJson.optString("title", "N/A")
                val overview = movieJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = movieJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = movieJson.optDouble("vote_average", 0.0)
                movieList.add(Movie(title, overview, posterPath, thumbsUp = 0, voteAverage = voteAverage))
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