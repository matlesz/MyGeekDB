import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun SearchView(
        searchQuery: String,
        onBackPressed: () -> Unit,
        searchResults: List<Any>, // Accepts both Movie and Series
        onSearchTypeChange: (String) -> Unit, // Callback to change search type
        currentSearchType: String, // Current search type ("Movie" or "Series")
        onItemClick: (Any) -> Unit, // Callback for item click
        onFavoriteClick: (Any) -> Unit // Callback for favorite click
) {
  Scaffold(
          topBar = {
            TopAppBar(
                    navigationIcon = {
                      IconButton(onClick = { onBackPressed() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                      }
                    },
                    title = { Text("Search Results") },
                    colors =
                            TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                            )
            )
          }
  ) { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
      // Toggle buttons for Movie and Series search
      Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
                onClick = { onSearchTypeChange("Movie") },
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (currentSearchType == "Movie")
                                                MaterialTheme.colorScheme.primary
                                        else Color.Gray
                        )
        ) { Text("Movies") }
        Button(
                onClick = { onSearchTypeChange("Series") },
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (currentSearchType == "Series")
                                                MaterialTheme.colorScheme.primary
                                        else Color.Gray
                        )
        ) { Text("Series") }
      }

      if (searchResults.isEmpty()) {
        Text(
                text = "No results found",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
        )
      } else {
        LazyColumn(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(
                  items = searchResults,
                  key = { result ->
                    when (result) {
                      is Movie -> "movie-${result.id}"
                      is Series -> "series-${result.id}"
                      else -> result.hashCode().toString()
                    }
                  }
          ) { result ->
            when (result) {
              is Movie -> {
                MediaItem(
                        title = result.title,
                        overview = result.overview,
                        posterPath = result.posterPath,
                        voteAverage = result.voteAverage,
                        isFavorite = result.isFavorite,
                        onClick = { onItemClick(result) },
                        onFavoriteClick = { onFavoriteClick(result) }
                )
              }
              is Series -> {
                MediaItem(
                        title = result.title,
                        overview = result.overview,
                        posterPath = result.posterPath,
                        voteAverage = result.voteAverage,
                        isFavorite = result.isFavorite,
                        onClick = { onItemClick(result) },
                        onFavoriteClick = { onFavoriteClick(result) }
                )
              }
            }
          }
        }
      }
    }
  }
}
