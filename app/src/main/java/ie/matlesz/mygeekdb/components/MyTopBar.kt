import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.matlesz.mygeekdb.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
        onHamburgerClick: () -> Unit,
        onSearchQueryChange: (String) -> Unit,
        onLogoClick: () -> Unit,
        onSearchFocused: () -> Unit,
        isSearchLoading: Boolean,
        searchQuery: String,
        onSearchQueryUpdated: (String) -> Unit
) {
  val focusManager = LocalFocusManager.current

  TopAppBar(
          navigationIcon = {
            Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(50.dp).padding(12.dp).clickable { onHamburgerClick() }
            )
          },
          title = {
            Box(modifier = Modifier.width(300.dp).padding(horizontal = 20.dp)) {
              TextField(
                      value = searchQuery,
                      onValueChange = { query -> onSearchQueryUpdated(query) },
                      placeholder = { Text(text = "Search...", fontSize = 12.sp) },
                      leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                      },
                      trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                          IconButton(onClick = { onSearchQueryUpdated("") }) {
                            Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = Color.Gray
                            )
                          }
                        }
                      },
                      modifier = Modifier.fillMaxWidth().height(48.dp),
                      shape = RoundedCornerShape(24.dp),
                      colors =
                              TextFieldDefaults.colors(
                                      focusedContainerColor = Color.LightGray,
                                      unfocusedContainerColor = Color.LightGray,
                                      focusedIndicatorColor = Color.Transparent,
                                      unfocusedIndicatorColor = Color.Transparent
                              ),
                      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                      keyboardActions =
                              KeyboardActions(
                                      onSearch = {
                                        if (searchQuery.isNotEmpty()) {
                                          onSearchQueryChange(searchQuery.trim())
                                        }
                                        focusManager.clearFocus()
                                      }
                              ),
                      maxLines = 1
              )
            }
          },
          actions = {
            Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(40.dp).clickable { onLogoClick() }
            )
          }
  )
}
