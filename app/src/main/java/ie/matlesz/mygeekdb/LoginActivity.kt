package ie.matlesz.mygeekdb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import ie.matlesz.mygeekdb.ui.theme.MyGeekDBTheme
import ie.matlesz.mygeekdb.viewmodel.LoginViewModel
import ie.matlesz.mygeekdb.views.LoginSignupScreen

class LoginActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        account.idToken?.let { token -> loginViewModel.signInWithGoogle(token) }
                    } catch (e: ApiException) {
                        Log.e("LoginActivity", "Google sign in failed", e)
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginViewModel.initGoogleSignIn(this)
        setContent {
            MyGeekDBTheme {
                LoginSignupScreen(
                        loginViewModel = loginViewModel,
                        onLoginSuccess = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onGoogleSignInClick = {
                            googleSignInLauncher.launch(loginViewModel.getGoogleSignInIntent())
                        }
                )
            }
        }
    }
}
