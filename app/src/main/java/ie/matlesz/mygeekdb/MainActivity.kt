package ie.matlesz.mygeekdb

import HomePage
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import ie.matlesz.mygeekdb.ui.theme.MyGeekDBTheme
import ie.matlesz.mygeekdb.viewmodel.LoginViewModel
import ie.matlesz.mygeekdb.viewmodel.MovieViewModel
import ie.matlesz.mygeekdb.viewmodel.SeriesViewModel
import ie.matlesz.mygeekdb.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
  private val loginViewModel: LoginViewModel by viewModels()
  private val userViewModel: UserViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseApp.initializeApp(this)

    // Load user data
    FirebaseAuth.getInstance().currentUser?.let { user -> userViewModel.loadUserData(user.uid) }

    setContent { MyGeekDBApp(userViewModel) }
  }
}

@Composable
fun MyGeekDBApp(userViewModel: UserViewModel) {
  val movieViewModel: MovieViewModel = viewModel()
  val seriesViewModel: SeriesViewModel = viewModel()
  val loginViewModel: LoginViewModel = viewModel()
  val context = LocalContext.current

  MyGeekDBTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      HomePage(
              movieViewModel = movieViewModel,
              seriesViewModel = seriesViewModel,
              userViewModel = userViewModel,
              onNavigateToLogin = {
                loginViewModel.logout()
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as? Activity)?.finish()
              }
      )
    }
  }
}
