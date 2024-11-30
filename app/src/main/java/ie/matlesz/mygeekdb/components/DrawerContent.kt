import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

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