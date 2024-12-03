package ie.matlesz.mygeekdb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ie.matlesz.mygeekdb.MainActivity
import ie.matlesz.mygeekdb.ui.theme.MyGeekDBTheme
import ie.matlesz.mygeekdb.viewmodel.LoginViewModel
import ie.matlesz.mygeekdb.views.LoginSignupScreen

class LoginActivity : ComponentActivity() {
  private val loginViewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyGeekDBTheme {
        LoginSignupScreen(
          loginViewModel = loginViewModel,
          onLoginSuccess = {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
          }
        )
      }
    }
  }
}