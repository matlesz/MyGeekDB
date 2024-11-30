import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import ie.matlesz.mygeekdb.R
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
  onHamburgerClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onLogoClick: () -> Unit,
  onSearchFocused: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  TopAppBar(
    title = { /* Empty since we use a custom search bar */ },
    navigationIcon = {
      Icon(
        imageVector = Icons.Default.Menu,
        contentDescription = "Menu",
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
          .padding(horizontal = 20.dp)
          .clickable { // Dismiss keyboard and clear focus on outside click
            focusManager.clearFocus()
            searchQuery = "" // Clear the text field
          }
      ) {
        TextField(
          value = searchQuery,
          onValueChange = { query ->
            searchQuery = query // Update query state immediately
          },
          placeholder = { Text(text = "Search...", fontSize = 12.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Icon"
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = {
                  searchQuery = "" // Clear the text field
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear Search",
                  tint = Color.Gray // Optional: Adjust color
                )
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(24.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.LightGray,
            unfocusedContainerColor = Color.LightGray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          ),
          keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = androidx.compose.ui.text.input.ImeAction.Search
          ),
          keyboardActions = KeyboardActions(
            onSearch = {
              onSearchQueryChange(searchQuery.trim()) // Trigger search
            }
          ),
          maxLines = 1
        )
      }

      Image(
        painter = rememberAsyncImagePainter(model = R.drawable.logo),
        contentDescription = "App Logo",
        modifier = Modifier
          .size(40.dp)
          .padding(end = 16.dp)
          .clickable { onLogoClick() }
      )
    },
    modifier = Modifier.fillMaxWidth(),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color.White,
      titleContentColor = Color.Black,
      navigationIconContentColor = Color.Black,
      actionIconContentColor = Color.Black
    )
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
  movieViewModel: MovieViewModel = viewModel(),
  seriesViewModel: SeriesViewModel = viewModel()
) {
  // Observables for Movies and Series
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val series by seriesViewModel.series.observeAsState(emptyList())
  val movieSearchResults by movieViewModel.searchResults.observeAsState(emptyList())
  val seriesSearchResults by seriesViewModel.searchResults.observeAsState(emptyList())

  // State variables
  var searchQuery by remember { mutableStateOf("") }
  var selectedTabIndex by remember { mutableStateOf(0) }
  var isSearchFocused by remember { mutableStateOf(false) }
  var currentSearchType by remember { mutableStateOf("Movie") } // Default search type
  var selectedItem by remember { mutableStateOf<Any?>(null) }

  val searchResults = if (currentSearchType == "Movie") movieSearchResults else seriesSearchResults
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  if (selectedItem != null) {
    // Show Detailed Media View
    DetailedMediaView(
      item = selectedItem!!,
      onBack = { selectedItem = null }
    )
  } else {
    // Main Page Content
    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        DrawerContent(
          onCloseDrawer = { scope.launch { drawerState.close() } },
          onHomeClick = {
            isSearchFocused = false
            searchQuery = ""
            selectedTabIndex = 0
            scope.launch { drawerState.close() }
          }
        )
      }
    ) {
      Scaffold(
        topBar = {
          MyTopBar(
            onHamburgerClick = { scope.launch { drawerState.open() } },
            onSearchQueryChange = { query ->
              searchQuery = query
              isSearchFocused = true
              if (currentSearchType == "Movie") {
                movieViewModel.searchMovies(query)
              } else {
                seriesViewModel.searchSeries(query)
              }
            },
            onLogoClick = {
              isSearchFocused = false
              searchQuery = ""
            },
            onSearchFocused = {
              isSearchFocused = true
              if (searchQuery.isNotEmpty()) {
                if (currentSearchType == "Movie") {
                  movieViewModel.searchMovies(searchQuery)
                } else {
                  seriesViewModel.searchSeries(searchQuery)
                }
              }
            }
          )
        }
      ) { paddingValues ->
        if (isSearchFocused) {
          // Search Results View
          SearchView(
            searchQuery = searchQuery,
            onBackPressed = { isSearchFocused = false },
            searchResults = searchResults,
            onSearchTypeChange = { type ->
              currentSearchType = type
              if (searchQuery.isNotEmpty()) {
                if (type == "Movie") {
                  movieViewModel.searchMovies(searchQuery)
                } else {
                  seriesViewModel.searchSeries(searchQuery)
                }
              }
            },
            currentSearchType = currentSearchType,
            onItemClick = { item -> selectedItem = item }
          )
        } else {
          // Recommended Movies/Series View
          Column(
            modifier = Modifier
              .padding(paddingValues)
              .fillMaxSize()
          ) {
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

            when (selectedTabIndex) {
              0 -> MediaItemList(
                items = movies,
                type = "Movie",
                onItemClick = { movie -> selectedItem = movie }
              )
              1 -> MediaItemList(
                items = series,
                type = "Series",
                onItemClick = { series -> selectedItem = series }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun DrawerContent(onCloseDrawer: () -> Unit, onHomeClick: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxHeight()
      .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f)
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC5))
        )
      )
  ) {
    IconButton(
      onClick = { onCloseDrawer() },
      modifier = Modifier
        .padding(8.dp)
        .align(Alignment.Start)
    ) {
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close Drawer",
        tint = Color.White
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "Navigation",
      style = MaterialTheme.typography.headlineMedium,
      color = Color.White,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Divider(color = Color.White)

    DrawerMenuItem(text = "Home") { onHomeClick() }
    DrawerMenuItem(text = "Favourite") { /* Add Favourite Logic */ }
    DrawerMenuItem(text = "About") { /* Add About Logic */ }
    DrawerMenuItem(text = "Logout") { /* Add Logout Logic */ }
  }
}

@Composable
fun DrawerMenuItem(text: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
  searchQuery: String,
  onBackPressed: () -> Unit,
  searchResults: List<Any>, // Accepts both Movie and Series
  onSearchTypeChange: (String) -> Unit, // Callback to change search type
  currentSearchType: String, // Current search type ("Movie" or "Series")
  onItemClick: (Any) -> Unit // Callback for item click
) {
  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = { onBackPressed() }) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        title = { Text("Search Results") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      // Toggle buttons for Movie and Series search
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
          onClick = { onSearchTypeChange("Movie") },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (currentSearchType == "Movie") MaterialTheme.colorScheme.primary else Color.Gray
          )
        ) {
          Text("Movies")
        }
        Button(
          onClick = { onSearchTypeChange("Series") },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (currentSearchType == "Series") MaterialTheme.colorScheme.primary else Color.Gray
          )
        ) {
          Text("Series")
        }
      }

      // Display search results
      if (searchResults.isEmpty()) {
        Text(
          text = "No results found",
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(searchResults) { result ->
            if (currentSearchType == "Movie") {
              val movie = result as Movie
              MediaItem(
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage,
                onClick = { onItemClick(movie) } // Pass the click action
              )
            } else {
              val series = result as Series
              MediaItem(
                title = series.title,
                overview = series.overview,
                posterPath = series.posterPath,
                voteAverage = series.voteAverage,
                onClick = { onItemClick(series) } // Pass the click action
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun <T> MediaItemList(
  items: List<T>,
  type: String,
  onItemClick: (T) -> Unit
) {
  LazyColumn {
    items(items) { item ->
      when {
        type == "Movie" && item is Movie -> {
          MediaItem(
            title = item.title,
            overview = item.overview,
            posterPath = item.posterPath,
            voteAverage = item.voteAverage,
            onClick = { onItemClick(item) } // Pass the movie to the click handler
          )
        }
        type == "Series" && item is Series -> {
          MediaItem(
            title = item.title,
            overview = item.overview,
            posterPath = item.posterPath,
            voteAverage = item.voteAverage,
            onClick = { onItemClick(item) } // Pass the series to the click handler
          )
        }
        else -> {
          // Handle unexpected item type
          throw IllegalArgumentException("Unsupported item type")
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
  voteAverage: Double?,
  onClick: () -> Unit // Add onClick callback
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
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 8.dp)
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
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedMediaView(item: Any, onBack: () -> Unit) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Details") },
        navigationIcon = {
          IconButton(onClick = { onBack() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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

      Image(
        painter = rememberAsyncImagePainter(posterPath ?: ""),
        contentDescription = "Poster of $title",
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(12.dp))
      )

      Text(text = title ?: "No Title", style = MaterialTheme.typography.headlineMedium)
      Text(text = overview ?: "No Overview", style = MaterialTheme.typography.bodyLarge)
    }
  }
}
