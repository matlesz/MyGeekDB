import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedMediaView(item: Any, onBack: () -> Unit) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Details") },
        navigationIcon = {
          IconButton(onClick = { onBack() }) {
            Icon(
              imageVector = Icons.Filled.ArrowBack, // Updated arrow
              contentDescription = "Back"
            )
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      val title = when (item) {
        is Movie -> item.title
        is Series -> item.title
        else -> "Unknown"
      }
      val overview = when (item) {
        is Movie -> item.overview
        is Series -> item.overview
        else -> "No description available"
      }
      val posterPath = when (item) {
        is Movie -> item.posterPath
        is Series -> item.posterPath
        else -> null
      }
      val voteAverage = when (item) {
        is Movie -> item.voteAverage
        is Series -> item.voteAverage
        else -> null
      }
      val voteCount = when (item) {
        is Movie -> item.voteCount
        is Series -> item.voteCount
        else -> null
      }
      val popularity = when (item) {
        is Movie -> item.popularity
        is Series -> item.popularity
        else -> null
      }

      // Display poster
      Image(
        painter = rememberAsyncImagePainter(posterPath),
        contentDescription = "Poster of $title",
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(12.dp))
      )

      // Display title
      Text(text = title, style = MaterialTheme.typography.headlineMedium)

      // Display overview
      Text(text = overview, style = MaterialTheme.typography.bodyLarge)

      // Display vote average
      if (voteAverage != null) {
        Text(
          text = "Vote Average: $voteAverage",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      // Display vote count
      if (voteCount != null) {
        Text(
          text = "Vote Count: $voteCount",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      // Display popularity
      if (popularity != null) {
        Text(
          text = "Popularity: $popularity",
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}