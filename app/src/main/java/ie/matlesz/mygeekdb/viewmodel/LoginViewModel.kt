package ie.matlesz.mygeekdb.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import ie.matlesz.mygeekdb.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ie.matlesz.mygeekdb.R

class LoginViewModel : ViewModel() {
  private val auth = FirebaseAuth.getInstance()
  private val db = FirebaseFirestore.getInstance()

  private val _loginState = MutableLiveData<LoginState>()
  val loginState: LiveData<LoginState> = _loginState

  private val _currentUser = MutableLiveData<User?>()
  val currentUser: LiveData<User?> = _currentUser

  private lateinit var googleSignInClient: GoogleSignInClient

  init {
    // Clear any existing auth state
    auth.signOut()
    _currentUser.value = null
    _loginState.value = LoginState.LoggedOut
  }

  fun clearError() {
    _loginState.value = LoginState.LoggedOut
  }

  fun login(email: String, password: String) {
    viewModelScope.launch {
      try {
        _loginState.value = LoginState.Loading
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.let { firebaseUser ->
          loadUserData(firebaseUser.uid)
          _loginState.value = LoginState.Success
        }
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Login failed", e)
        _loginState.value = LoginState.Error(e.message ?: "Login failed")
      }
    }
  }

  fun register(email: String, password: String, displayName: String) {
    viewModelScope.launch {
      try {
        _loginState.value = LoginState.Loading
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { firebaseUser ->
          // Update display name in Firebase Auth
          val profileUpdates = userProfileChangeRequest { this.displayName = displayName }
          firebaseUser.updateProfile(profileUpdates).await()

          // Create new user document in Firestore
          val newUser =
                  User(
                          uid = firebaseUser.uid,
                          email = email,
                          displayName = displayName,
                          photoUrl = firebaseUser.photoUrl?.toString()
                  )
          createNewUser(newUser)
          _loginState.value = LoginState.Success
        }
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Registration failed", e)
        _loginState.value = LoginState.Error(e.message ?: "Registration failed")
      }
    }
  }

  private fun loadUserData(uid: String) {
    viewModelScope.launch {
      try {
        val documentSnapshot = db.collection("users").document(uid).get().await()
        if (documentSnapshot.exists()) {
          val user = documentSnapshot.toObject(User::class.java)
          _currentUser.value = user
        } else {
          // Create new user document if it doesn't exist
          val newUser =
                  User(
                          uid = uid,
                          email = auth.currentUser?.email ?: "",
                          displayName = auth.currentUser?.displayName,
                          photoUrl = auth.currentUser?.photoUrl?.toString()
                  )
          createNewUser(newUser)
        }
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Error loading user data", e)
      }
    }
  }

  private fun createNewUser(user: User) {
    viewModelScope.launch {
      try {
        db.collection("users").document(user.uid).set(user).await()
        _currentUser.value = user
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Error creating user document", e)
      }
    }
  }

  fun logout() {
    auth.signOut()
    _currentUser.value = null
    _loginState.value = LoginState.LoggedOut

    // Sign out from Google if using Google Sign-In
    googleSignInClient.signOut().addOnCompleteListener {
      // Handle any post-sign-out actions if needed
    }
  }

  fun getCurrentUserId(): String? = auth.currentUser?.uid

  fun sendPasswordResetEmail(email: String) {
    viewModelScope.launch {
      try {
        _loginState.value = LoginState.Loading
        auth.sendPasswordResetEmail(email).await()
        _loginState.value = LoginState.PasswordResetSent
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Password reset failed", e)
        _loginState.value = LoginState.Error(e.message ?: "Failed to send password reset email")
      }
    }
  }

  fun initGoogleSignIn(context: Context) {
    val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

    googleSignInClient = GoogleSignIn.getClient(context, gso)
  }

  fun signInWithGoogle(idToken: String) {
    viewModelScope.launch {
      try {
        _loginState.value = LoginState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()

        result.user?.let { firebaseUser ->
          val user =
                  User(
                          uid = firebaseUser.uid,
                          email = firebaseUser.email ?: "",
                          displayName = firebaseUser.displayName ?: "",
                          photoUrl = firebaseUser.photoUrl?.toString()
                  )
          createNewUser(user)
          _loginState.value = LoginState.Success
        }
      } catch (e: Exception) {
        Log.e("LoginViewModel", "Google sign in failed", e)
        _loginState.value = LoginState.Error(e.message ?: "Google sign in failed")
      }
    }
  }

  fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

  sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    object LoggedOut : LoginState()
    object PasswordResetSent : LoginState()
    data class Error(val message: String) : LoginState()
  }
}
