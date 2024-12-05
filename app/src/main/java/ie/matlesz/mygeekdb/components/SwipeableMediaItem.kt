package ie.matlesz.mygeekdb.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.matlesz.mygeekdb.views.FavoriteItemCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableMediaItem(
  item: Any,
  onItemClick: (Any) -> Unit,
  onFavoriteClick: (Any) -> Unit,
  onDelete: () -> Unit
) {
  val dismissState =
    rememberDismissState(
      confirmStateChange = { dismissValue ->
        when (dismissValue) {
          DismissValue.DismissedToEnd, DismissValue.DismissedToStart -> {
            onDelete()
            true
          }
          DismissValue.Default -> false
        }
      }
    )

  SwipeToDismiss(
    state = dismissState,
    background = {
      Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(32.dp)
        )
      }
    },
    dismissContent = {
      FavoriteItemCard(
        item = item,
        onItemClick = onItemClick,
        onFavoriteClick = onFavoriteClick
      )
    },
    directions = setOf(DismissDirection.EndToStart)
  )
}