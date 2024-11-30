package ie.matlesz.mygeekdb

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(
  movieViewModel: MovieViewModel = viewModel(),
  seriesViewModel: SeriesViewModel = viewModel() // Add a new ViewModel for series
) {
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val series by seriesViewModel.series.observeAsState(emptyList())

  var selectedTabIndex by remember { mutableStateOf(0) } // State to manage selected tab

  LaunchedEffect(movies, series) {
    Log.d("MovieScreen", "Movies received: ${movies.size}")
    Log.d("SeriesScreen", "Series received: ${series.size}")
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("MyGeekDB") }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      // Tabs for "Movies" and "Series"
      TabRow(selectedTabIndex = selectedTabIndex) {
        Tab(
          selected = selectedTabIndex == 0,
          onClick = { selectedTabIndex = 0 },
          text = { Text("Recommended Movies") }
        )
        Tab(
          selected = selectedTabIndex == 1,
          onClick = { selectedTabIndex = 1 },
          text = { Text("Recommended Series") }
        )
      }

      // Show content based on selected tab
      when (selectedTabIndex) {
        0 -> {
          // Movies
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(movies) { movie ->
              MovieItem(movie)
            }
          }
        }
        1 -> {
          // Series
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(series) { seriesItem ->
              SeriesItem(seriesItem)
            }
          }
        }
      }
    }
  }
}

@Composable
fun MovieItem(movie: Movie) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Row(
      modifier = Modifier
        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      // Movie Poster
      Box(
        modifier = Modifier
          .weight(0.3f)
          .fillMaxHeight(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = rememberAsyncImagePainter(movie.posterPath),
          contentDescription = "Poster of ${movie.title}",
          modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
        )
      }

      // Movie Details
      Column(
        modifier = Modifier
          .weight(0.7f)
          .fillMaxHeight()
      ) {
        // Title
        Text(
          text = movie.title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Overview (Restricted to 3 lines)
        Text(
          text = movie.overview,
          style = MaterialTheme.typography.bodySmall,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vote Average with Thumbs Up Icon
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ThumbUp,
              contentDescription = "Vote Average",
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = String.format(Locale.US, "%.1f", movie.voteAverage),
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(start = 4.dp)
            )
          }

          // Heart Icon (Clickable)
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

@Composable
fun SeriesItem(series: Series) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Row(
      modifier = Modifier
        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      // Series Poster
      Box(
        modifier = Modifier
          .weight(0.3f)
          .fillMaxHeight(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = rememberAsyncImagePainter(series.posterPath),
          contentDescription = "Poster of ${series.title}",
          modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
        )
      }

      // Series Details
      Column(
        modifier = Modifier
          .weight(0.7f)
          .fillMaxHeight()
      ) {
        // Title
        Text(
          text = series.title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Overview (Restricted to 3 lines)
        Text(
          text = series.overview,
          style = MaterialTheme.typography.bodySmall,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}