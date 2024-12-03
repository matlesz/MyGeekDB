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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesView(
        favoriteItems: List<Any>,
        currentFavoriteType: String,
        onFavoriteTypeChange: (String) -> Unit,
        onItemClick: (Any) -> Unit,
        onFavoriteClick: (Any) -> Unit
) {
  Scaffold() { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
      // Toggle buttons for Movie and Series favorites
      Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
                onClick = { onFavoriteTypeChange("Movie") },
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (currentFavoriteType == "Movie")
                                                MaterialTheme.colorScheme.primary
                                        else Color.Gray
                        )
        ) { Text("Movies") }
        Button(
                onClick = { onFavoriteTypeChange("Series") },
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (currentFavoriteType == "Series")
                                                MaterialTheme.colorScheme.primary
                                        else Color.Gray
                        )
        ) { Text("Series") }
      }

      // Display favorite items
      LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(favoriteItems) { item ->
          when (item) {
            is Movie -> {
              MediaItem(
                      title = item.title,
                      overview = item.overview,
                      posterPath = item.posterPath,
                      voteAverage = item.voteAverage,
                      isFavorite = true,
                      onClick = { onItemClick(item) },
                      onFavoriteClick = { onFavoriteClick(item) }
              )
            }
            is Series -> {
              MediaItem(
                      title = item.title,
                      overview = item.overview,
                      posterPath = item.posterPath,
                      voteAverage = item.voteAverage,
                      isFavorite = true,
                      onClick = { onItemClick(item) },
                      onFavoriteClick = { onFavoriteClick(item) }
              )
            }
          }
        }
      }
    }
  }
}
