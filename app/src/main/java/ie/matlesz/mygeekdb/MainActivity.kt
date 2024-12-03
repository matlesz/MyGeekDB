package ie.matlesz.mygeekdb

import HomePage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import ie.matlesz.mygeekdb.ui.theme.MyGeekDBTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MyGeekDBTheme { HomePage() } }
  }
}
