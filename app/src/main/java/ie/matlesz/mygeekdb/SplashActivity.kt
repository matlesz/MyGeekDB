package ie.matlesz.mygeekdb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import ie.matlesz.mygeekdb.viewmodel.SplashViewModel

class SplashActivity : ComponentActivity() {

  private val splashViewModel: SplashViewModel by viewModels()
  private val auth = FirebaseAuth.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Install the splash screen
    installSplashScreen()

    super.onCreate(savedInstanceState)

    // Sign out any existing user to ensure login flow
    auth.signOut()

    // Observe the initialization state
    splashViewModel.isInitialized.observe(this) { isInitialized ->
      if (isInitialized) {
        // Always redirect to LoginActivity first
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
      }
    }

    // Set Compose content for splash screen
    setContent {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    // Start initialization
    splashViewModel.initializeApp()
  }
}
