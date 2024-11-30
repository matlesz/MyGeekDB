import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(
  movieViewModel: MovieViewModel = viewModel(),
  seriesViewModel: SeriesViewModel = viewModel()
) {
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val series by seriesViewModel.series.observeAsState(emptyList())

  var selectedTabIndex by remember { mutableStateOf(0) }

  Scaffold(
    topBar = {
      TopAppBar(title = { Text("MyGeekDB") }) // Only this title will remain
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      // TabRow for movies and series
      TabRow(selectedTabIndex = selectedTabIndex) {
        Tab(
          selected = selectedTabIndex == 0,
          onClick = { selectedTabIndex = 0 },
          text = { Text("Recommended Movies") } // No "MyGeekDB" here
        )
        Tab(
          selected = selectedTabIndex == 1,
          onClick = { selectedTabIndex = 1 },
          text = { Text("Recommended Series") } // No "MyGeekDB" here
        )
      }

      when (selectedTabIndex) {
        0 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(movies) { movie ->
              MediaItem(
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage
              )
            }
          }
        }
        1 -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(series) { seriesItem ->
              MediaItem(
                title = seriesItem.title,
                overview = seriesItem.overview,
                posterPath = seriesItem.posterPath,
                voteAverage = seriesItem.voteAverage
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MediaItem(
  title: String,
  overview: String,
  posterPath: String,
  voteAverage: Double
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(vertical = 4.dp),
    elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Row(
      modifier = Modifier
        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Poster
      Image(
        painter = rememberAsyncImagePainter(posterPath),
        contentDescription = "Poster of $title",
        modifier = Modifier
          .size(100.dp)
          .clip(RoundedCornerShape(12.dp))
      )

      // Details Column
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        // Title
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Overview
        Text(
          text = overview,
          style = MaterialTheme.typography.bodySmall,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vote Average and Favorite Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Vote Average with Thumbs Up Icon
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ThumbUp,
              contentDescription = "Vote Average",
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = String.format(Locale.US, "%.1f", voteAverage),
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(start = 4.dp)
            )
          }

          // Favorite Button
          var isFavorite by remember { mutableStateOf(false) }
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) Color.Red else Color.Gray,
            modifier = Modifier
              .size(20.dp)
              .clickable {
                isFavorite = !isFavorite
              }
          )
        }
      }
    }
  }
}