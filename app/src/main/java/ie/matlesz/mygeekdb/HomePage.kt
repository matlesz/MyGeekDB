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
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
  onHamburgerClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onLogoClick: () -> Unit,
  onSearchFocused: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  TopAppBar(
    title = { /* Empty since the search bar is custom */ },
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
      ) {
        TextField(
          value = searchQuery,
          onValueChange = { query ->
            searchQuery = query
            onSearchQueryChange(query)
          },
          placeholder = { Text(text = "Search...", fontSize = 12.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Icon"
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onSearchFocused() },
          shape = RoundedCornerShape(24.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.LightGray,
            unfocusedContainerColor = Color.LightGray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          )
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
    colors = TopAppBarDefaults.smallTopAppBarColors(
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
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val series by seriesViewModel.series.observeAsState(emptyList())
  val movieSearchResults by movieViewModel.searchResults.observeAsState(emptyList())
  val seriesSearchResults by seriesViewModel.searchResults.observeAsState(emptyList())
  var searchQuery by remember { mutableStateOf("") }
  var selectedTabIndex by remember { mutableStateOf(0) }
  var isSearchFocused by remember { mutableStateOf(false) }
  var currentSearchType by remember { mutableStateOf("Movie") } // Default to "Movie"

  val searchResults = if (currentSearchType == "Movie") movieSearchResults else seriesSearchResults

  // Drawer state
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      // Drawer content with restricted width
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f) // Set width to 3/4 of the screen
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC5))
            )
          )
          .padding(16.dp)
      ) {
        Column {
          Text(
            text = "Navigation",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
          )
          Divider(color = Color.White)
          Spacer(modifier = Modifier.height(8.dp))
          DrawerMenuItem(text = "Home", onClick = { scope.launch { drawerState.close() } })
          DrawerMenuItem(text = "Profile", onClick = { scope.launch { drawerState.close() } })
          DrawerMenuItem(text = "Settings", onClick = { scope.launch { drawerState.close() } })
          DrawerMenuItem(text = "Logout", onClick = { scope.launch { drawerState.close() } })
        }
      }
    },
    scrimColor = Color.Transparent // Ensures no semi-transparent background is visible when drawer is closed
  ) {
    Scaffold(
      topBar = {
        MyTopBar(
          onHamburgerClick = {
            scope.launch { drawerState.open() }
          },
          onSearchQueryChange = { query ->
            searchQuery = query
            if (isSearchFocused) {
              if (currentSearchType == "Movie") {
                movieViewModel.searchMovies(query)
              } else {
                seriesViewModel.searchSeries(query)
              }
            }
          },
          onLogoClick = {
            isSearchFocused = false
            searchQuery = ""
          },
          onSearchFocused = {
            isSearchFocused = true
            if (searchQuery.isEmpty()) {
              // Default to movie search on first focus
              movieViewModel.searchMovies("default") // Replace "default" with your desired query
            }
          }
        )
      }
    ) { paddingValues ->
      if (isSearchFocused) {
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
          currentSearchType = currentSearchType
        )
      } else {
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
            0 -> MediaItemList(items = movies, type = "Movie")
            1 -> MediaItemList(items = series, type = "Series")
          }
        }
      }
    }
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
  currentSearchType: String // Current search type ("Movie" or "Series")
) {
  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = { onBackPressed() }) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Back"
            )
          }
        },
        title = { Text("Search Results") },
        colors = TopAppBarDefaults.smallTopAppBarColors(
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
                voteAverage = movie.voteAverage
              )
            } else {
              val series = result as Series
              MediaItem(
                title = series.title,
                overview = series.overview,
                posterPath = series.posterPath,
                voteAverage = series.voteAverage
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun <T> MediaItemList(items: List<T>, type: String) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(items) { item ->
      when (type) {
        "Movie" -> {
          val movie = item as? Movie ?: return@items
          MediaItem(
            title = movie.title,
            overview = movie.overview,
            posterPath = movie.posterPath,
            voteAverage = movie.voteAverage
          )
        }
        "Series" -> {
          val series = item as? Series ?: return@items
          MediaItem(
            title = series.title,
            overview = series.overview,
            posterPath = series.posterPath,
            voteAverage = series.voteAverage
          )
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