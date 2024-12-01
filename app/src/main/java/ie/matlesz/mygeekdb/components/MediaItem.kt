import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.util.Locale

@Composable
fun MediaItem(
  title: String?,
  overview: String?,
  posterPath: String?,
  voteAverage: Double?,
  onClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  isFavorite: Boolean // Pass the favorite state as a plain Boolean
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(vertical = 4.dp)
      .clickable { onClick() }, // Make the Card clickable
    elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Row(
      modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Image(
        painter = rememberAsyncImagePainter(posterPath ?: ""),
        contentDescription = "Poster of $title",
        modifier = Modifier
          .size(100.dp)
          .clip(RoundedCornerShape(12.dp))
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        Text(
          text = title ?: "No Title",
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = overview ?: "No Overview",
          style = MaterialTheme.typography.bodySmall,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "Vote Average",
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = String.format(Locale.US, "%.1f", voteAverage ?: 0.0),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
          )

          // Spacer between vote average and favorite button
          Spacer(modifier = Modifier.weight(1f))

          // Favorite button
          IconButton(onClick = onFavoriteClick) {
            Icon(
              imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Favorite"
            )
          }
        }
      }
    }
  }
}