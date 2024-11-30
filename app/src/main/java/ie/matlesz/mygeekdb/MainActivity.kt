package ie.matlesz.mygeekdb

import HomePage
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ie.matlesz.mygeekdb.ui.theme.MyGeekDBTheme

class MainActivity : ComponentActivity() {
  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyGeekDBTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          content = {
            HomePage()
          }
        )
      }
    }
  }
}
