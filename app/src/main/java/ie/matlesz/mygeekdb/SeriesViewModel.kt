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

data class Series(
  val title: String,
  val overview: String,
  val posterPath: String,
  val voteAverage: Double
)

class SeriesViewModel : ViewModel() {

  private val _series = MutableLiveData<List<Series>>()
  val series: LiveData<List<Series>> = _series

  private val _searchResults = MutableLiveData<List<Series>>()
  val searchResults: LiveData<List<Series>> = _searchResults

  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean> = _isLoading

  private val _errorMessage = MutableLiveData<String?>()
  val errorMessage: LiveData<String?> = _errorMessage

  private val client = OkHttpClient()
  private val baseImageUrl = "https://image.tmdb.org/t/p/w500"
  private var currentPage = 1
  private var isLoadingMore = false

  init {
    fetchSeriesRecommendations(1)
  }

  /**
   * Fetch recommended TV series.
   * @param page Page number for pagination.
   */
  fun fetchSeriesRecommendations(page: Int) {
    if (isLoadingMore) return
    isLoadingMore = true
    _isLoading.postValue(true)

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"
        val request = Request.Builder()
          .url("https://api.themoviedb.org/4/account/62cf1c5afcf907004dbdae6e/tv/recommendations?page=$page&language=en-US")
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
                val title = seriesJson.optString("name", "N/A") // TV series use "name"
                val overview = seriesJson.optString("overview", "N/A")
                val posterPath = seriesJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = seriesJson.optDouble("vote_average", 0.0)
                seriesList.add(Series(title, overview, posterPath, voteAverage))
              }
            }

            Log.d("SeriesViewModel", "Fetched recommended series: ${seriesList.size}")
            _series.postValue((_series.value ?: emptyList()) + seriesList)
            _errorMessage.postValue(null)
          }
        } else {
          Log.e("SeriesViewModel", "Recommendations request failed: ${response.code}, ${response.message}")
          _errorMessage.postValue("Error: ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Exception during recommendations fetch: ${e.message}", e)
        _errorMessage.postValue("Exception: ${e.message}")
      } finally {
        _isLoading.postValue(false)
        isLoadingMore = false
      }
    }
  }

  /**
   * Load the next page of series recommendations.
   */
  fun loadNextPage() {
    currentPage++
    fetchSeriesRecommendations(currentPage)
  }

  /**
   * Search TV series by query.
   * @param query Search term.
   */
  fun searchSeries(query: String) {
    if (query.isBlank()) {
      _searchResults.postValue(emptyList())
      return
    }

    _isLoading.postValue(true)
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val apiKey = "Bearer ${BuildConfig.TMDB_API_KEY}"
        val request = Request.Builder()
          .url("https://api.themoviedb.org/3/search/tv?query=$query&language=en-US")
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
                val title = seriesJson.optString("name", "N/A")
                val overview = seriesJson.optString("overview", "N/A")
                val posterPath = seriesJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = seriesJson.optDouble("vote_average", 0.0)
                seriesList.add(Series(title, overview, posterPath, voteAverage))
              }
            }

            _searchResults.postValue(seriesList)
            _errorMessage.postValue(null)
          }
        } else {
          Log.e("SeriesViewModel", "Search request failed: ${response.code}, ${response.message}")
          _errorMessage.postValue("Error: ${response.message}")
        }
      } catch (e: Exception) {
        Log.e("SeriesViewModel", "Exception during search: ${e.message}", e)
        _errorMessage.postValue("Exception: ${e.message}")
      } finally {
        _isLoading.postValue(false)
      }
    }
  }
}