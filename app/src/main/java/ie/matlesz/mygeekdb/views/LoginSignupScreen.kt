package ie.matlesz.mygeekdb.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ie.matlesz.mygeekdb.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignupScreen(loginViewModel: LoginViewModel = viewModel(), onLoginSuccess: () -> Unit) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isLogin by remember { mutableStateOf(true) }

  val loginState by loginViewModel.loginState.observeAsState()
  val currentUser by loginViewModel.currentUser.observeAsState()

  // Navigate when user is logged in
  LaunchedEffect(currentUser) {
    if (currentUser != null) {
      onLoginSuccess()
    }
  }

  // Show loading or error states
  loginState?.let { state ->
    when (state) {
      is LoginViewModel.LoginState.Loading -> {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
      }
      is LoginViewModel.LoginState.Error -> {
        AlertDialog(
                onDismissRequest = {},
                title = { Text("Error") },
                text = { Text(state.message) },
                confirmButton = { TextButton(onClick = {}) { Text("OK") } }
        )
      }
      else -> {}
    }
  }

  Column(
          modifier = Modifier.fillMaxSize().padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
  ) {
    Text(
            text = if (isLogin) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
            onClick = {
              if (isLogin) {
                loginViewModel.login(email, password)
              } else {
                loginViewModel.register(email, password)
              }
            },
            modifier = Modifier.fillMaxWidth()
    ) { Text(if (isLogin) "Login" else "Sign Up") }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = { isLogin = !isLogin }) {
      Text(if (isLogin) "Need an account? Sign Up" else "Have an account? Login")
    }
  }
}

@Composable
fun LoadingScreen() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}

@Composable
fun ErrorScreen(message: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(message) }
}
