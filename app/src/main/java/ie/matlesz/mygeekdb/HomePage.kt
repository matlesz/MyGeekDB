package ie.matlesz.mygeekdb

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
        title = { Text("Favorite Movies") }
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

@Composable
fun MovieItem(movie: Movie) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = movie.title,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = movie.overview,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}