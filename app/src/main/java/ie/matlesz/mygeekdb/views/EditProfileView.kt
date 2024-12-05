package ie.matlesz.mygeekdb.views

import DeleteAccountDialog
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import ie.matlesz.mygeekdb.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileView(
  onNavigateBack: () -> Unit,
  onSaveSuccess: () -> Unit,
  launcher: ManagedActivityResultLauncher<String, Uri?>,
  initialImageUri: Uri? = null,
  userViewModel: UserViewModel,
  onNavigateToLogin: () -> Unit
) {
  val currentUserState by userViewModel.currentUser.observeAsState()
  val storageRef = FirebaseStorage.getInstance().reference

  var displayName by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var showError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var showPasswordDialog by remember { mutableStateOf(false) }

  // Update state when currentUserState changes
  LaunchedEffect(currentUserState) {
    currentUserState?.let { user ->
      displayName = user.displayName.orEmpty()
      selectedImageUri = user.photoUrl?.let { Uri.parse(it) }
      Log.d("EditProfileView", "Loaded user data: $user")
    }
  }

  // Handle save changes
  fun handleSaveChanges() {
    isLoading = true
    try {
      if (selectedImageUri != null && selectedImageUri != Uri.parse(currentUserState?.photoUrl)) {
        // Upload new image
        val imageRef = storageRef.child("profile_images/${currentUserState?.uid}/profile.jpg")
        val uploadTask = imageRef.putFile(selectedImageUri!!)

        uploadTask.continueWithTask { task ->
          if (!task.isSuccessful) {
            throw task.exception!!
          }
          imageRef.downloadUrl
        }.addOnSuccessListener { downloadUrl ->
          userViewModel.updateUserProfile(
            displayName = displayName,
            photoUrl = downloadUrl
          )
          isLoading = false
          onSaveSuccess()
        }.addOnFailureListener { e ->
          isLoading = false
          showError = true
          errorMessage = e.message ?: "Failed to upload image"
        }
      } else {
        // Update profile without new image
        userViewModel.updateUserProfile(
          displayName = displayName,
          photoUrl = selectedImageUri
        )
        isLoading = false
        onSaveSuccess()
      }
    } catch (e: Exception) {
      isLoading = false
      showError = true
      errorMessage = e.message ?: "Failed to update profile"
    }
  }

  if (showPasswordDialog) {
    DeleteAccountDialog(
      onDismiss = { showPasswordDialog = false },
      onSuccess = {
        showPasswordDialog = false
        onNavigateToLogin()
      },
      viewModel = userViewModel
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Edit Profile") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Profile Image Section
      Box(
        modifier = Modifier
          .size(120.dp)
          .clip(CircleShape)
          .clickable {
            launcher.launch("image/*")
          }
      ) {
        when {
          selectedImageUri != null -> {
            Image(
              painter = rememberAsyncImagePainter(selectedImageUri),
              contentDescription = "Selected profile picture",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }
          !currentUserState?.photoUrl.isNullOrEmpty() -> {
            Image(
              painter = rememberAsyncImagePainter(currentUserState?.photoUrl),
              contentDescription = "Current profile picture",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }
          else -> {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Default profile picture",
              modifier = Modifier.fillMaxSize(),
              tint = Color.Gray
            )
          }
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit photo",
            tint = Color.White,
            modifier = Modifier
              .align(Alignment.Center)
              .size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it },
        label = { Text("Display Name") },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = { handleSaveChanges() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary
          )
        } else {
          Text("Save Changes")
        }
      }

      if (showError) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = errorMessage,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyMedium
        )
      }

      Button(
        onClick = { showPasswordDialog = true },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp)
      ) {
        Text("Delete Account")
      }
    }
  }
}
