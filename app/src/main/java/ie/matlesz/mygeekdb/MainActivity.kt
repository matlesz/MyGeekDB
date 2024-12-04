package ie.matlesz.mygeekdb

import HomePage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import ie.matlesz.mygeekdb.ui.theme.MyGeekDBTheme
import ie.matlesz.mygeekdb.viewmodel.MovieViewModel
import ie.matlesz.mygeekdb.viewmodel.SeriesViewModel
import ie.matlesz.mygeekdb.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseApp.initializeApp(this)
    setContent { MyGeekDBApp() }
  }
}

@Composable
fun MyGeekDBApp() {
  val movieViewModel: MovieViewModel = viewModel()
  val seriesViewModel: SeriesViewModel = viewModel()
  val userViewModel: UserViewModel = viewModel()

  MyGeekDBTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      HomePage(
              movieViewModel = movieViewModel,
              seriesViewModel = seriesViewModel,
              userViewModel = userViewModel
      )
    }
  }
}
