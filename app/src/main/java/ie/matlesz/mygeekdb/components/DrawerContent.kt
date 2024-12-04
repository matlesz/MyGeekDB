import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import ie.matlesz.mygeekdb.components.DrawerMenuItem

@Composable
fun DrawerContent(
        onCloseDrawer: () -> Unit,
        onHomeClick: () -> Unit,
) {
  val activity = LocalContext.current as? Activity
  val auth = FirebaseAuth.getInstance()
  val currentUser = auth.currentUser

  Column(
          modifier =
                  Modifier.fillMaxHeight()
                          .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f)
                          .background(
                                  Brush.verticalGradient(
                                          colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC5))
                                  )
                          )
  ) {
    IconButton(onClick = { onCloseDrawer() }, modifier = Modifier.padding(8.dp)) {
      Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Drawer",
              tint = Color.White
      )
    }

    // Profile Section
    Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Profile Image
      if (currentUser?.photoUrl != null) {
        Image(
          painter = rememberAsyncImagePainter(currentUser.photoUrl),
          contentDescription = "Profile picture",
          modifier = Modifier.size(100.dp).clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      } else {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Default profile picture",
          modifier = Modifier.size(100.dp).clip(CircleShape),
          tint = Color.White
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // User Info
      Text(
        text = currentUser?.displayName ?: "Guest",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White
      )
      Text(
        text = currentUser?.email ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.7f)
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Edit Profile Button
      OutlinedButton(
        onClick = { /* TODO: Add edit profile navigation */ },
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = BorderStroke(1.dp, Color.White)
      ) { Text("Edit Profile") }
    }

    HorizontalDivider(color = Color.White.copy(alpha = 0.5f))

    DrawerMenuItem(text = "Home") { onHomeClick() }
    DrawerMenuItem(text = "About") { /* Add About Logic */}
    DrawerMenuItem(text = "Logout") {
      auth.signOut()
      activity?.finishAffinity()
    }
  }
}
