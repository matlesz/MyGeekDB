package ie.matlesz.mygeekdb.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ie.matlesz.mygeekdb.R

@Composable
fun MediaItem(
        title: String?,
        overview: String?,
        posterPath: String?,
        voteAverage: Double?,
        onClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        isFavorite: Boolean
) {
  Card(
          modifier =
                  Modifier.fillMaxWidth().wrapContentHeight().padding(vertical = 4.dp).clickable {
                    onClick()
                  }, // Make the Card clickable
          elevation = CardDefaults.cardElevation(4.dp)
  ) {
    Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AsyncImage(
              model = posterPath?.takeIf { it.isNotBlank() },
              contentDescription = "Poster of $title",
              modifier = Modifier.size(60.dp).clip(RoundedCornerShape(4.dp)),
              contentScale = ContentScale.Crop,
              error = painterResource(R.drawable.placeholder_image)
      )

      Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
        Text(
                text = title ?: "No Title",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
        )
        Text(
                text = overview ?: "No Overview",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
        )
      }

      IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
        Icon(
                imageVector =
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Unmark as Favorite" else "Mark as Favorite"
        )
      }
    }
  }
}
