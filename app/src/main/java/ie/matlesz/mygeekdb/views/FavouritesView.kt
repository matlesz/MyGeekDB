package ie.matlesz.mygeekdb.views

import Movie
import Series
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ie.matlesz.mygeekdb.components.MediaItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoritesView(
        favoriteItems: List<Any>,
        currentFavoriteType: String,
        onFavoriteTypeChange: (String) -> Unit,
        onItemClick: (Any) -> Unit,
        onFavoriteClick: (Any) -> Unit,
        onDeleteFavorite: (Any) -> Unit
) {
  Scaffold { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
      // Toggle buttons
      Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
                onClick = { onFavoriteTypeChange("Movie") },
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (currentFavoriteType == "Movie")
                                                MaterialTheme.colorScheme.primary
                                        else Color.Gray
                        )
        ) { Text("Movies") }

        Button(
                onClick = { onFavoriteTypeChange("Series") },
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (currentFavoriteType == "Series")
                                                MaterialTheme.colorScheme.primary
                                        else Color.Gray
                        )
        ) { Text("Series") }
      }

      LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(
                items = favoriteItems,
                key = { item ->
                  when (item) {
                    is Movie -> item.id
                    is Series -> item.id
                    else -> item.hashCode()
                  }
                }
        ) { item ->
          var isRemoved by remember { mutableStateOf(false) }

          AnimatedVisibility(visible = !isRemoved, exit = shrinkHorizontally() + fadeOut()) {
            SwipeableMediaItem(
                    item = item,
                    onItemClick = onItemClick,
                    onFavoriteClick = onFavoriteClick,
                    onDelete = {
                      isRemoved = true
                      onDeleteFavorite(item)
                    }
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableMediaItem(
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

@Composable
fun FavoriteItemCard(item: Any, onItemClick: (Any) -> Unit, onFavoriteClick: (Any) -> Unit) {
  when (item) {
    is Movie -> {
      MediaItem(
              title = item.title,
              overview = item.overview,
              posterPath = item.posterPath,
              voteAverage = item.voteAverage,
              onClick = { onItemClick(item) },
              onFavoriteClick = { onFavoriteClick(item) },
              isFavorite = item.isFavorite
      )
    }
    is Series -> {
      MediaItem(
              title = item.title,
              overview = item.overview,
              posterPath = item.posterPath,
              voteAverage = item.voteAverage,
              onClick = { onItemClick(item) },
              onFavoriteClick = { onFavoriteClick(item) },
              isFavorite = item.isFavorite
      )
    }
  }
}
