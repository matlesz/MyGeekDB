import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

@Composable
fun <T> MediaItemList(
  items: List<T>,
  type: String,
  onItemClick: (T) -> Unit,
  onFavoriteClick: (T) -> Unit // Add this new parameter
) {
  LazyColumn {
    items(items) { item ->
      when {
        type == "Movie" && item is Movie -> {
          MediaItem(
            title = item.title,
            overview = item.overview,
            posterPath = item.posterPath,
            voteAverage = item.voteAverage,
            isFavorite = item.isFavorite,
            onClick = { onItemClick(item) }, // Pass the movie to the click handler
            onFavoriteClick = { onFavoriteClick(item) } // Pass the movie to the favorite handler
          )
        }
        type == "Series" && item is Series -> {
          MediaItem(
            title = item.title,
            overview = item.overview,
            posterPath = item.posterPath,
            voteAverage = item.voteAverage,
            isFavorite = item.isFavorite,
            onClick = { onItemClick(item) }, // Pass the series to the click handler
            onFavoriteClick = { onFavoriteClick(item) } // Pass the series to the favorite handler
          )
        }
        else -> {
          // Handle unexpected item type
          throw IllegalArgumentException("Unsupported item type")
        }
      }
    }
  }
}