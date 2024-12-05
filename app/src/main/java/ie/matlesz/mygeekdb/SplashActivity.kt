package ie.matlesz.mygeekdb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import ie.matlesz.mygeekdb.viewmodel.SplashViewModel

class SplashActivity : ComponentActivity() {

  private val splashViewModel: SplashViewModel by viewModels()
  private val auth = FirebaseAuth.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)

    // Check auth state and navigate accordingly
    if (auth.currentUser != null) {
      startActivity(Intent(this, MainActivity::class.java))
    } else {
      startActivity(Intent(this, LoginActivity::class.java))
    }
    finish()
  }
}
