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

  init {
    fetchSeriesRecommendations()
  }

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
                val title = seriesJson.optString("name", "N/A") // "name" is used for TV series
                val overview = seriesJson.optString("overview", "N/A")
                val baseImageUrl = "https://image.tmdb.org/t/p/w500"
                val posterPath = seriesJson.optString("poster_path", "").let {
                  if (it.isNotEmpty()) "$baseImageUrl$it" else "android.resource://ie.matlesz.mygeekdb/drawable/placeholder_image"
                }
                val voteAverage = seriesJson.optDouble("vote_average", 0.0)
                seriesList.add(Series(title, overview, posterPath, voteAverage))
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
}