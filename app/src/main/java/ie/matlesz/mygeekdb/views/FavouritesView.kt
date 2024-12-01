import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesView(
  favoriteItems: List<Any>, // Replace `Any` with your actual type (e.g., Movie or Series)
  onItemClick: (Any) -> Unit,
  onFavoriteClick: (Any) -> Unit, // Add this parameter for favorite toggle
  isFavorite: (Any) -> Boolean // Add this parameter to check if the item is a favorite
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Text(
      text = "Favorites",
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    if (favoriteItems.isEmpty()) {
      Text(
        text = "No favorites yet.",
        style = MaterialTheme.typography.bodyLarge
      )
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(favoriteItems) { item ->
          MediaItem(
            title = when (item) {
              is Movie -> item.title
              is Series -> item.title
              else -> "Unknown"
            },
            overview = when (item) {
              is Movie -> item.overview
              is Series -> item.overview
              else -> ""
            },
            posterPath = when (item) {
              is Movie -> item.posterPath
              is Series -> item.posterPath
              else -> null
            },
            voteAverage = when (item) {
              is Movie -> item.voteAverage
              is Series -> item.voteAverage
              else -> null
            },
            isFavorite = isFavorite(item), // Pass the favorite status
            onFavoriteClick = { onFavoriteClick(item) }, // Handle favorite toggle
            onClick = { onItemClick(item) } // Handle item click
          )
        }
      }
    }
  }
}