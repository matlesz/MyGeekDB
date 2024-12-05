import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ie.matlesz.mygeekdb.viewmodel.MovieViewModel
import ie.matlesz.mygeekdb.viewmodel.SeriesViewModel
import ie.matlesz.mygeekdb.viewmodel.UserViewModel
import ie.matlesz.mygeekdb.views.AboutView
import ie.matlesz.mygeekdb.views.EditProfileView
import ie.matlesz.mygeekdb.views.FavoritesView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
        movieViewModel: MovieViewModel = viewModel(),
        seriesViewModel: SeriesViewModel = viewModel(),
        userViewModel: UserViewModel = viewModel(),
        onNavigateToLogin: () -> Unit
) {
  // Observables for Movies and Series
  val movies by movieViewModel.movies.observeAsState(emptyList())
  val series by seriesViewModel.series.observeAsState(emptyList())

  val favoriteMovies by movieViewModel.favorites.observeAsState(emptyList())
  val favoriteSeries by seriesViewModel.favorites.observeAsState(emptyList())

  // State variables
  var currentFavoriteType by remember { mutableStateOf("Movie") }
  var currentSearchType by remember { mutableStateOf("Movie") }
  var searchQuery by remember { mutableStateOf("") }
  var isSearchFocused by remember { mutableStateOf(false) }
  var selectedItem by remember { mutableStateOf<Any?>(null) }
  var selectedTabIndex by remember { mutableStateOf(0) }
  var showEditProfile by remember { mutableStateOf(false) }
  var showAboutPage by remember { mutableStateOf(false) }

  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  var isSearchLoading by remember { mutableStateOf(false) }

  val imagePickerLauncher =
          rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                  uri: Uri? ->
            if (uri != null) {
              Log.d("HomePage", "Image selected: $uri")
              selectedImageUri = uri
            }
          }

  val movieSearchResults by movieViewModel.searchResults.observeAsState(initial = emptyList())
  val seriesSearchResults by seriesViewModel.searchResults.observeAsState(initial = emptyList())

  if (showAboutPage) {
    AboutView(onNavigateBack = { showAboutPage = false })
  } else if (showEditProfile) {
    EditProfileView(
            onNavigateBack = {
              showEditProfile = false
              selectedImageUri = null
            },
            onSaveSuccess = {
              showEditProfile = false
              selectedImageUri = null
              scope.launch { drawerState.close() }
            },
            launcher = imagePickerLauncher,
            initialImageUri = selectedImageUri,
            userViewModel = userViewModel,
            onNavigateToLogin = onNavigateToLogin
    )
  } else if (selectedItem != null) {
    DetailedMediaView(item = selectedItem!!, onBack = { selectedItem = null })
  } else {
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
                      },
                      onEditProfileClick = { showEditProfile = true },
                      onAboutClick = { showAboutPage = true },
                      userViewModel = userViewModel
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
                          if (query.isNotEmpty()) {
                            isSearchLoading = true
                            if (currentSearchType == "Movie") {
                              movieViewModel.searchResults(query)
                            } else {
                              seriesViewModel.searchSeries(query)
                            }
                            isSearchLoading = false
                          }
                        },
                        onLogoClick = {
                          isSearchFocused = false
                          searchQuery = ""
                        },
                        onSearchFocused = {
                          isSearchFocused = true
                          if (searchQuery.isNotEmpty()) {
                            isSearchLoading = true
                            if (currentSearchType == "Movie") {
                              movieViewModel.searchResults(searchQuery)
                            } else {
                              seriesViewModel.searchSeries(searchQuery)
                            }
                          }
                        },
                        isSearchLoading = isSearchLoading,
                        searchQuery = searchQuery,
                        onSearchQueryUpdated = { query -> searchQuery = query }
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
                    isSearchLoading = false
                  },
                  searchResults =
                          if (currentSearchType == "Movie") {
                            movieSearchResults.map { movie ->
                              movie.copy(isFavorite = movieViewModel.isFavorite(movie))
                            }
                          } else {
                            seriesSearchResults.map { series ->
                              series.copy(isFavorite = seriesViewModel.isFavorite(series))
                            }
                          },
                  onSearchTypeChange = { type ->
                    currentSearchType = type
                    if (searchQuery.isNotEmpty()) {
                      isSearchLoading = true
                      if (type == "Movie") {
                        movieViewModel.searchResults(searchQuery)
                      } else {
                        seriesViewModel.searchSeries(searchQuery)
                      }
                      isSearchLoading = false
                    }
                  },
                  currentSearchType = currentSearchType,
                  onItemClick = { item -> selectedItem = item },
                  onFavoriteClick = { item ->
                    when (item) {
                      is Movie -> movieViewModel.toggleFavorite(item)
                      is Series -> seriesViewModel.toggleFavorite(item)
                    }
                  },
                  isLoading = isSearchLoading
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
                              },
                              onDeleteFavorite = { item ->
                                when (item) {
                                  is Movie -> movieViewModel.removeFavorite(item)
                                  is Series -> seriesViewModel.removeFavorite(item)
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
