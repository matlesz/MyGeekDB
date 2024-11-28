package ie.matlesz.mygeekdb

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.filled.ThumbDown


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(movieViewModel: MovieViewModel = viewModel()) {
  val movies by movieViewModel.movies.observeAsState(emptyList())

  LaunchedEffect(movies) {
    Log.d("MovieScreen", "Movies received: ${movies.size}")
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Recommended Movies") }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(movies) { movie ->
        MovieItem(movie)
      }
    }
  }
}

//@Composable
//fun MovieItem(movie: Movie) {
//  Card(
//    modifier = Modifier
//      .fillMaxWidth()
//      .wrapContentHeight(),
//    elevation = CardDefaults.cardElevation(4.dp)
//  ) {
//    Column(modifier = Modifier.padding(16.dp)) {
//      Text(
//        text = movie.title,
//        style = MaterialTheme.typography.titleLarge
//      )
//      Spacer(modifier = Modifier.height(4.dp))
//      Text(
//        text = movie.overview,
//        style = MaterialTheme.typography.bodyMedium
//      )
//    }
//  }

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
        .padding(16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Movie Poster
      Image(
        painter = rememberAsyncImagePainter(movie.posterPath),
        contentDescription = "Poster of ${movie.title}",
        modifier = Modifier
          .size(100.dp) // Adjust size as needed
          .clip(MaterialTheme.shapes.medium)
      )

      // Movie Details
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        // Title
        Text(
          text = movie.title,
          style = MaterialTheme.typography.titleLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Overview (Restricted to 3 lines)
        Text(
          text = movie.overview,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Thumbs up and down
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "Thumbs Up",
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "${movie.thumbsUp}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
          )

          Spacer(modifier = Modifier.width(16.dp))

          Icon(
            imageVector = Icons.Default.ThumbDown,
            contentDescription = "Thumbs Down",
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "${movie.thumbsDown}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
          )
        }
      }
    }
  }
}
