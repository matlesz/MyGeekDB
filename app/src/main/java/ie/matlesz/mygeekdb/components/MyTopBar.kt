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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import ie.matlesz.mygeekdb.R

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