package ie.matlesz.mygeekdb.components

import androidx.compose.runtime.Composable

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