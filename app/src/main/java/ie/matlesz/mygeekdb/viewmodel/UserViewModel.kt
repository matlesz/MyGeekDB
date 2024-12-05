package ie.matlesz.mygeekdb.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import ie.matlesz.mygeekdb.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class UserViewModel(application: Application) : AndroidViewModel(application) {
  private val auth = FirebaseAuth.getInstance()
  private val db = FirebaseFirestore.getInstance()
  private val storage = FirebaseStorage.getInstance()
  private val _currentUser = MutableLiveData<User?>()
  val currentUser: LiveData<User?> = _currentUser

  init {
    auth.currentUser?.let { firebaseUser ->
      loadUserData(firebaseUser.uid)
    }
  }

  fun loadUserData(uid: String) {
    viewModelScope.launch {
      try {
        val documentSnapshot = db.collection("users").document(uid).get().await()
        if (documentSnapshot.exists()) {
          val user = documentSnapshot.toObject(User::class.java)
          _currentUser.postValue(user)
          Log.d("UserViewModel", "Loaded user data: $user")
        } else {
          // Create new user document if it doesn't exist
          val newUser = User(
            uid = uid,
            email = auth.currentUser?.email ?: "",
            displayName = auth.currentUser?.displayName ?: "",
            photoUrl = auth.currentUser?.photoUrl?.toString()
          )
          createNewUser(newUser)
        }
      } catch (e: Exception) {
        Log.e("UserViewModel", "Error loading user data", e)
      }
    }
  }

  private fun createNewUser(user: User) {
    viewModelScope.launch {
      try {
        db.collection("users").document(user.uid).set(user).await()
        _currentUser.postValue(user)
      } catch (e: Exception) {
        Log.e("UserViewModel", "Error creating user", e)
      }
    }
  }

  fun updateUserProfile(displayName: String?, photoUrl: Uri?) {
    val currentUser = auth.currentUser ?: return
    Log.d("UserViewModel", "Updating profile - Name: $displayName, Photo: $photoUrl")

    viewModelScope.launch {
      try {
        // Step 1: Update Firebase Auth Profile
        val profileUpdates = UserProfileChangeRequest.Builder().apply {
          displayName?.let { setDisplayName(it) }
          photoUrl?.let { setPhotoUri(it) }
        }.build()
        
        currentUser.updateProfile(profileUpdates).await()
        Log.d("UserViewModel", "Firebase Auth profile updated")
        
        // Step 2: Update Firestore
        val userRef = db.collection("users").document(currentUser.uid)
        val updates = hashMapOf<String, Any>()
        
        displayName?.let { updates["displayName"] = it }
        photoUrl?.toString()?.let { updates["photoUrl"] = it }
        
        userRef.update(updates).await()
        Log.d("UserViewModel", "Firestore updated")
        
        // Step 3: Update Local State
        val updatedUser = User(
          uid = currentUser.uid,
          email = currentUser.email ?: "",
          displayName = displayName ?: currentUser.displayName ?: "",
          photoUrl = photoUrl?.toString()
        )
        _currentUser.postValue(updatedUser)
        Log.d("UserViewModel", "Local state updated: $updatedUser")
      } catch (e: Exception) {
        Log.e("UserViewModel", "Error updating profile", e)
        throw e
      }
    }
  }

  fun deleteUser(password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    viewModelScope.launch {
      try {
        val currentUser = auth.currentUser ?: run {
          onError(Exception("No user logged in"))
          return@launch
        }

        val email = currentUser.email ?: run {
          onError(Exception("User email not found"))
          return@launch
        }

        try {
          // Step 1: Re-authenticate
          val credential = EmailAuthProvider.getCredential(email, password)
          currentUser.reauthenticate(credential).await()
          
          // Step 2: Delete user data from Firestore
          val userRef = db.collection("users").document(currentUser.uid)
          userRef.delete().await()
          
          // Step 3: Delete profile image if exists
          val storageRef = FirebaseStorage.getInstance().reference
            .child("profile_images/${currentUser.uid}/profile.jpg")
          try {
            storageRef.delete().await()
          } catch (e: Exception) {
            Log.w("UserViewModel", "No profile image to delete or deletion failed", e)
          }

          // Step 4: Delete Auth user
          currentUser.delete().await()

          // Step 5: Clear local state and sign out
          auth.signOut()
          _currentUser.postValue(null)

          onSuccess()
        } catch (e: Exception) {
          Log.e("UserViewModel", "Error during user deletion", e)
          onError(e)
        }
      } catch (e: Exception) {
        Log.e("UserViewModel", "Unexpected error", e)
        onError(e)
      }
    }
  }
}