import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ie.matlesz.mygeekdb.views.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
        searchQuery: String,
        onBackPressed: () -> Unit,
        searchResults: List<Any>,
        onSearchTypeChange: (String) -> Unit,
        currentSearchType: String,
        onItemClick: (Any) -> Unit,
        onFavoriteClick: (Any) -> Unit,
        isLoading: Boolean
) {
  Log.d(
          "SearchView",
          "Rendering SearchView with ${searchResults.size} results, isLoading: $isLoading"
  )
  Scaffold(
          topBar = {
            TopAppBar(
                    title = { Text("Search Results") },
                    navigationIcon = {
                      IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                      }
                    },
                    colors =
                            TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
            )
          }
  ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      // Search type toggle
      Row(
              modifier = Modifier.fillMaxWidth().padding(8.dp),
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

      if (isLoading) {
        LoadingScreen()
      } else if (searchResults.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
                  text = "No results found",
                  style = MaterialTheme.typography.bodyLarge,
                  color = Color.Gray
          )
        }
      } else {
        LazyColumn(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(searchResults) { result ->
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
