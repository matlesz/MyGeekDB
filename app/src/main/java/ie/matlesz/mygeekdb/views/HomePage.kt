import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import ie.matlesz.mygeekdb.viewmodel.MovieViewModel
import ie.matlesz.mygeekdb.viewmodel.SeriesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
        movieViewModel: MovieViewModel = viewModel(),
        seriesViewModel: SeriesViewModel = viewModel()
) {
  // Observables for Movies and Series
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val series by seriesViewModel.series.observeAsState(emptyList())

  val favoriteMovies by movieViewModel.favorites.observeAsState(emptyList())
  val favoriteSeries by seriesViewModel.favorites.observeAsState(emptyList())
  val movieSearchResults by movieViewModel.searchResults.observeAsState(emptyList())
  val seriesSearchResults by seriesViewModel.searchResults.observeAsState(emptyList())

  // State variables
  var currentFavoriteType by remember { mutableStateOf("Movie") }
  var currentSearchType by remember { mutableStateOf("Movie") }
  var searchQuery by remember { mutableStateOf("") }
  var isSearchFocused by remember { mutableStateOf(false) }
  var selectedItem by remember { mutableStateOf<Any?>(null) }
  var selectedTabIndex by remember { mutableStateOf(0) }

  val searchResults = if (currentSearchType == "Movie") movieSearchResults else seriesSearchResults
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  if (selectedItem != null) {
    // Show Detailed Media View
    DetailedMediaView(item = selectedItem!!, onBack = { selectedItem = null })
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
                      //          onFavoritesClick = { navController.navigate("favorites") }
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
                            movieViewModel.searchResults(query)
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
                              movieViewModel.searchResults(searchQuery)
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
                  onBackPressed = {
                    isSearchFocused = false
                    searchQuery = ""
                  },
                  searchResults =
                          if (currentSearchType == "Movie") {
                            movieViewModel.searchResults.value?.map { movie ->
                              movie.copy(isFavorite = movieViewModel.isFavorite(movie))
                            }
                                    ?: emptyList()
                          } else {
                            seriesViewModel.searchResults.value?.map { series ->
                              series.copy(isFavorite = seriesViewModel.isFavorite(series))
                            }
                                    ?: emptyList()
                          },
                  onSearchTypeChange = { type ->
                    currentSearchType = type
                    if (searchQuery.isNotEmpty()) {
                      if (type == "Movie") {
                        movieViewModel.searchResults(searchQuery)
                      } else {
                        seriesViewModel.searchSeries(searchQuery)
                      }
                    }
                  },
                  currentSearchType = currentSearchType,
                  onItemClick = { item -> selectedItem = item },
                  onFavoriteClick = { item ->
                    when (item) {
                      is Movie -> movieViewModel.toggleFavorite(item)
                      is Series -> seriesViewModel.toggleFavorite(item)
                    }
                  }
          )
        } else {
          // Recommended Movies/Series View
          Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
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
              Tab(
                      selected = selectedTabIndex == 2,
                      onClick = { selectedTabIndex = 2 },
                      text = { Text("Favorites") }
              )
            }

            when (selectedTabIndex) {
              0 ->
                      MediaItemList(
                              items = movies,
                              type = "Movie",
                              onItemClick = { movie -> selectedItem = movie },
                              onFavoriteClick = { movie -> movieViewModel.toggleFavorite(movie) },
                              //     isFavorite = item.isFavorite // Provide the isFavorite lambda

                              )
              1 ->
                      MediaItemList(
                              items = series,
                              type = "Series",
                              onItemClick = { series -> selectedItem = series },
                              onFavoriteClick = { series ->
                                seriesViewModel.toggleFavorite(series)
                              },
                      )
              2 ->
                      FavoritesView(
                              favoriteItems =
                                      if (currentFavoriteType == "Movie") {
                                        favoriteMovies
                                      } else {
                                        favoriteSeries
                                      },
                              currentFavoriteType = currentFavoriteType,
                              onFavoriteTypeChange = { newType -> currentFavoriteType = newType },
                              onItemClick = { item ->
                                when (item) {
                                  is Movie -> selectedItem = item
                                  is Series -> selectedItem = item
                                }
                              },
                              onFavoriteClick = { item ->
                                when (item) {
                                  is Movie -> movieViewModel.toggleFavorite(item)
                                  is Series -> seriesViewModel.toggleFavorite(item)
                                }
                              }
                      )
            }
          }
        }
      }
    }
  }
}

// add toggle stage state
// add permanent storage for the favourite list
// add firebase database
// add about
// add readme
// add profile image storage on firebase
