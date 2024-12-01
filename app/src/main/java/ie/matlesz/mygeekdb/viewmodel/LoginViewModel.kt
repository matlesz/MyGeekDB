import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


//@HiltViewModel
class LoginViewModel @Inject constructor(
  private val auth: FirebaseAuth // Or your authentication logic
) : ViewModel() {

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState

  fun login(email: String, password: String, callback: (Result<Unit>) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          callback(Result.success(Unit))
        } else {
          callback(Result.failure(task.exception ?: Exception("Unknown error")))
        }
      }
  }
}

data class LoginUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null
)