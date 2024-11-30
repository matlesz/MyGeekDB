import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.util.Locale
import ie.matlesz.mygeekdb.R
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
  onHamburgerClick: () -> Unit,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onLogoClick: () -> Unit
) {
  TopAppBar(
    title = { Text("") }, // Empty Text for the title
    navigationIcon = {
      Icon(
        imageVector = Icons.Default.Menu,
        contentDescription = "Hamburger Menu",
        modifier = Modifier
          .size(50.dp)
          .padding(12.dp)
          .clickable { onHamburgerClick() }
      )
    },
    actions = {
      Box(
        modifier = Modifier
          .width(300.dp)
          .padding(horizontal = 8.dp)
      ) {
        TextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text(text = "Search...", style = MaterialTheme.typography.bodySmall) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Icon"
            )
          },
          shape = RoundedCornerShape(24.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.LightGray,
            unfocusedContainerColor = Color.LightGray,
            disabledContainerColor = Color.LightGray,
            errorContainerColor = Color.Red,
            cursorColor = Color.Black,
            errorCursorColor = Color.Red
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        )
      }
      Image(
        painter = rememberAsyncImagePainter(R.drawable.logo),
        contentDescription = "Logo",
        modifier = Modifier
          .size(50.dp)
          .padding(end = 16.dp)
          .clickable { onLogoClick() }
      )
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color.White,
      titleContentColor = Color.Black,
      navigationIconContentColor = Color.Black,
      actionIconContentColor = Color.Black
    )
  )
}

@Composable
fun HomePage(
  movieViewModel: MovieViewModel = viewModel()
) {
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val searchResults by movieViewModel.searchResults.observeAsState(emptyList())
  var searchQuery by remember { mutableStateOf("") }
  var selectedTabIndex by remember { mutableStateOf(0) }

  val isSearching = searchQuery.isNotEmpty()

  Scaffold(
    topBar = {
      MyTopBar(
        onHamburgerClick = { println("Hamburger clicked") },
        searchQuery = searchQuery,
        onSearchQueryChange = {
          searchQuery = it
          movieViewModel.searchMovies(it) // Trigger search on query change
        },
        onLogoClick = { println("Logo clicked") }
      )
    }
  ) { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues)) {
      if (!isSearching) {
        // Show tabs for recommended movies and series when not searching
        TabRow(selectedTabIndex = selectedTabIndex) {
          Tab(
            selected = selectedTabIndex == 0,
            onClick = { selectedTabIndex = 0 },
            text = { Text("Recommended Movies") }
          )
        }

        if (selectedTabIndex == 0) {
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
      } else {
        // Show search results
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(searchResults) { movie ->
            MediaItem(
              title = movie.title,
              overview = movie.overview,
              posterPath = movie.posterPath,
              voteAverage = movie.voteAverage
            )
          }
        }
      }
    }
  }
}

@Composable
fun MediaItem(
  title: String?,
  overview: String?,
  posterPath: String?,
  voteAverage: Double?
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
        .padding(8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Poster
      Image(
        painter = rememberAsyncImagePainter(posterPath ?: ""),
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
          text = title ?: "No Title",
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        // Overview
        Text(
          text = overview ?: "No Overview",
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
              text = String.format(Locale.US, "%.1f", voteAverage ?: 0.0),
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