package ie.matlesz.mygeekdb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


data class Series(
  val title: String,
  val overview: String,
  val posterPath: String
)

class SeriesViewModel : ViewModel() {
  private val _series = MutableLiveData<List<Series>>(emptyList())
  val series: LiveData<List<Series>> get() = _series

  init {
    fetchRecommendedSeries()
  }

  private fun fetchRecommendedSeries() {
    // Simulate fetching series
    _series.value = listOf(
      Series(
        title = "Breaking Bad",
        overview = "A high school chemistry teacher turned methamphetamine producer.",
        posterPath = "https://example.com/breaking_bad.jpg"
      ),
      Series(
        title = "Stranger Things",
        overview = "A group of kids face supernatural forces in their small town.",
        posterPath = "https://example.com/stranger_things.jpg"
      )
    )
  }
}