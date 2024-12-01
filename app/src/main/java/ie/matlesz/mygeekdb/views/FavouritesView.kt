import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesView(
  favoriteItems: List<Any>, // Accepts both Movie and Series
  currentFavoriteType: String, // Current type ("Movie" or "Series")
  onFavoriteTypeChange: (String) -> Unit, // Callback to change favorite type
  onItemClick: (Any) -> Unit, // Callback for item click
  onFavoriteClick: (Any) -> Unit // Callback for favorite click
) {
  Scaffold()
  { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      // Toggle buttons for Movie and Series favorites
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
          onClick = { onFavoriteTypeChange("Movie") },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (currentFavoriteType == "Movie") MaterialTheme.colorScheme.primary else Color.Gray
          )
        ) {
          Text("Movies")
        }
        Button(
          onClick = { onFavoriteTypeChange("Series") },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (currentFavoriteType == "Series") MaterialTheme.colorScheme.primary else Color.Gray
          )
        ) {
          Text("Series")
        }
      }

      // Display favorite items
      if (favoriteItems.isEmpty()) {
        Text(
          text = "No favorites found",
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(favoriteItems) { item ->
            if (currentFavoriteType == "Movie") {
              val movie = item as Movie
              MediaItem(
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage,
                isFavorite = movie.isFavorite,
                onClick = { onItemClick(movie) },
                onFavoriteClick = { onFavoriteClick(movie) }
              )
            } else {
              val series = item as Series
              MediaItem(
                title = series.title,
                overview = series.overview,
                posterPath = series.posterPath,
                voteAverage = series.voteAverage,
                isFavorite = series.isFavorite,
                onClick = { onItemClick(series) },
                onFavoriteClick = { onFavoriteClick(series) }
              )
            }
          }
        }
      }
    }
  }
}