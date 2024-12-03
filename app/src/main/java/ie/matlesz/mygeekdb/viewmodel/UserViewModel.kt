package ie.matlesz.mygeekdb.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.matlesz.mygeekdb.model.User

class UserViewModel : ViewModel() {
  private val auth = FirebaseAuth.getInstance()
  private val db = FirebaseFirestore.getInstance()

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
    db.collection("users").document(uid)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          _currentUser.value = document.toObject(User::class.java)
        } else {
          // Create new user document if it doesn't exist
          val newUser = User(
            uid = uid,
            email = auth.currentUser?.email ?: "",
            displayName = auth.currentUser?.displayName
          )
          createNewUser(newUser)
        }
      }
  }

  private fun createNewUser(user: User) {
    db.collection("users").document(user.uid)
      .set(user)
      .addOnSuccessListener {
        _currentUser.value = user
      }
  }

  fun getCurrentUserId(): String? {
    return auth.currentUser?.uid
  }
}