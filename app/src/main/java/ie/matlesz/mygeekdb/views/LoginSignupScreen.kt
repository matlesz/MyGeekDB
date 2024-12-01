import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import ie.matlesz.mygeekdb.SplashScreenContent

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
    // Title
    Text(
      text = if (isLoginMode) "Login" else "Signup",
      style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Email Field
    TextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Email") },
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Password Field
    TextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      modifier = Modifier.fillMaxWidth(),
      visualTransformation = PasswordVisualTransformation()
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Login/Signup Button
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

    // Toggle Login/Signup Mode Button
    TextButton(onClick = { isLoginMode = !isLoginMode }) {
      Text(if (isLoginMode) "Switch to Signup" else "Switch to Login")
    }

    // Display Error Message
    errorMessage?.let {
      Text(
        text = it,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 16.dp)
      )
    }
  }
}

// Firebase Login Logic
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
        val error = task.exception?.localizedMessage ?: "Login failed"
        onError(error)
      }
    }
}

// Firebase Signup Logic
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
        val error = task.exception?.localizedMessage ?: "Signup failed"
        onError(error)
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