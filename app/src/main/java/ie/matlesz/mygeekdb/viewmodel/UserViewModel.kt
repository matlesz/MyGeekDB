package ie.matlesz.mygeekdb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ie.matlesz.mygeekdb.model.User

class UserViewModel : ViewModel() {
  private val auth = FirebaseAuth.getInstance()
  private val db = FirebaseFirestore.getInstance()
  private val storage = FirebaseStorage.getInstance()

  private val _currentUser = MutableLiveData<User?>()
  val currentUser: LiveData<User?> = _currentUser

  init {
    // Listen for auth state changes
    auth.addAuthStateListener { firebaseAuth ->
      val firebaseUser = firebaseAuth.currentUser
      if (firebaseUser != null) {
        loadUserData(firebaseUser.uid)
      } else {
        _currentUser.value = null
      }
    }
  }

  private fun loadUserData(uid: String) {
    db.collection("users").document(uid).get().addOnSuccessListener { document ->
      if (document.exists()) {
        _currentUser.value = document.toObject(User::class.java)
      } else {
        val newUser =
                User(
                        uid = uid,
                        email = auth.currentUser?.email ?: "",
                        displayName = auth.currentUser?.displayName,
                        photoUrl = auth.currentUser?.photoUrl?.toString()
                )
        createNewUser(newUser)
      }
    }
  }

  private fun createNewUser(user: User) {
    db.collection("users").document(user.uid).set(user).addOnSuccessListener {
      _currentUser.value = user
    }
  }

  fun updateUserProfile(displayName: String?, photoUrl: String?) {
    val uid = auth.currentUser?.uid ?: return

    Log.d("UserViewModel", "Starting profile update - Name: $displayName, Photo: $photoUrl")

    // Only update if we have valid data
    if (displayName == null && photoUrl == null) {
      Log.e("UserViewModel", "Both displayName and photoUrl are null, skipping update")
      return
    }

    val updatedUser =
            _currentUser.value?.copy(
                    displayName = displayName ?: _currentUser.value?.displayName,
                    photoUrl = photoUrl ?: _currentUser.value?.photoUrl
            )
                    ?: User(
                            uid = uid,
                            email = auth.currentUser?.email ?: "",
                            displayName = displayName ?: "",
                            photoUrl = photoUrl
                    )

    db.collection("users")
            .document(uid)
            .set(updatedUser)
            .addOnSuccessListener {
              _currentUser.postValue(updatedUser)
              Log.d(
                      "UserViewModel",
                      "Profile updated successfully - Photo URL: ${updatedUser.photoUrl}"
              )
            }
            .addOnFailureListener { e -> Log.e("UserViewModel", "Error updating profile", e) }
  }

}
