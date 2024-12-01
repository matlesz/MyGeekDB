package ie.matlesz.mygeekdb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ie.matlesz.mygeekdb.viewmodel.SplashViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import HomePage
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : ComponentActivity() {

  private val splashViewModel: SplashViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Install the splash screen
    installSplashScreen()

    super.onCreate(savedInstanceState)

    // Observe the initialization state
    splashViewModel.isInitialized.observe(this) { isInitialized ->
      if (isInitialized) {
        navigateToMainActivity()
      }
    }

    // Set Compose content for splash screen
    setContent {
      SplashScreenContent(onSplashFinished = {
        // Logic to navigate to login/signup or main activity
        navigateToMainActivity()
      })
    }

    // Simulate initialization tasks
    splashViewModel.initializeApp()
  }

  private fun navigateToMainActivity() {
    startActivity(Intent(this, MainActivity::class.java))
    finish() // Ensure SplashActivity is removed from the back stack
  }
}

@Composable
fun SplashScreenContent(onSplashFinished: () -> Unit) {
  // Simulate a delay for splash screen (e.g., initialization tasks)
  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(2000) // 2 seconds delay
    onSplashFinished()
  }

  // Splash screen UI
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black), // Ensure background is explicitly black
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "MyGeekDB",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White // Ensure the text is visible on black background
      )
      Image(
        painter = painterResource(id = R.drawable.ic_splash),
        contentDescription = "App Logo",
        modifier = Modifier.size(128.dp) // Adjust the size as needed
      )
    }
  }
}


@Composable
fun LoginSignupScreen(onLoginSuccess: () -> Unit) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isLoginMode by remember { mutableStateOf(true) } // Toggle between login and signup
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = if (isLoginMode) "Login" else "Signup",
      style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(16.dp))

    TextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Email") },
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    TextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      modifier = Modifier.fillMaxWidth(),
      visualTransformation = PasswordVisualTransformation()
    )
    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        if (isLoginMode) {
          loginUser(email, password, context, onLoginSuccess, { errorMessage = it })
        } else {
          signUpUser(email, password, context, onLoginSuccess, { errorMessage = it })
        }
      },
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(if (isLoginMode) "Login" else "Signup")
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = { isLoginMode = !isLoginMode }) {
      Text(if (isLoginMode) "Switch to Signup" else "Switch to Login")
    }

    errorMessage?.let {
      Text(
        text = it,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 16.dp)
      )
    }
  }
}
fun loginUser(
  email: String,
  password: String,
  context: Context,
  onSuccess: () -> Unit,
  onError: (String) -> Unit
) {
  val auth = FirebaseAuth.getInstance()
  auth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onError(task.exception?.message ?: "Login failed")
      }
    }
}

fun signUpUser(
  email: String,
  password: String,
  context: Context,
  onSuccess: () -> Unit,
  onError: (String) -> Unit
) {
  val auth = FirebaseAuth.getInstance()
  auth.createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
      if (task.isSuccessful) {
        onSuccess()
      } else {
        onError(task.exception?.message ?: "Signup failed")
      }
    }
}

@Composable
fun AppNavigators(navController: NavHostController) {
  NavHost(navController = navController, startDestination = "splash") {
    composable("splash") {
      SplashScreenContent(onSplashFinished = {
        navController.navigate("login") {
          popUpTo("splash") { inclusive = true }
        }
      })
    }
    composable("login") {
      LoginSignupScreen(onLoginSuccess = {
        navController.navigate("home") {
          popUpTo("login") { inclusive = true }
        }
      })
    }
    composable("home") {
      HomePage()
    }
  }
}