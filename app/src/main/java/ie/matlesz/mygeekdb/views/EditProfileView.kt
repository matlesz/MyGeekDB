package ie.matlesz.mygeekdb.views

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
        userViewModel: UserViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserState by userViewModel.currentUser.observeAsState()

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var selectedImageUri by remember(initialImageUri) { mutableStateOf(initialImageUri) }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                    modifier =
                            Modifier.size(120.dp).clip(CircleShape).clickable {
                                Log.d("EditProfileView", "Image picker clicked")
                                launcher.launch("image/*")
                            }
            ) {
                when {
                    selectedImageUri != null -> {
                        Log.d("EditProfileView", "Displaying selected image: $selectedImageUri")
                        Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                        )
                    }
                    !currentUserState?.photoUrl.isNullOrEmpty() -> {
                        Log.d(
                                "EditProfileView",
                                "Displaying current profile image: ${currentUserState?.photoUrl}"
                        )
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

                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))) {
                    Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center).size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Name Field
            OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            isLoading = true
                            val storageRef = FirebaseStorage.getInstance().reference
                            val imageRef =
                                    storageRef.child(
                                            "profile_images/${auth.currentUser?.uid}/profile.jpg"
                                    )

                            // Create file metadata
                            val metadata = storageMetadata { contentType = "image/jpeg" }

                            Log.d("EditProfileView", "Starting image upload process")
                            Log.d("EditProfileView", "Selected URI: $selectedImageUri")

                            imageRef.putFile(selectedImageUri!!, metadata)
                                    .addOnProgressListener { taskSnapshot ->
                                        val progress =
                                                (100.0 * taskSnapshot.bytesTransferred /
                                                        taskSnapshot.totalByteCount)
                                        Log.d("EditProfileView", "Upload progress: $progress%")
                                    }
                                    .continueWithTask { task ->
                                        if (!task.isSuccessful) {
                                            task.exception?.let { throw it }
                                        }
                                        imageRef.downloadUrl
                                    }
                                    .addOnSuccessListener { downloadUrl ->
                                        Log.d("EditProfileView", "Final Download URL: $downloadUrl")
                                        userViewModel.updateUserProfile(
                                                displayName = displayName,
                                                photoUrl = downloadUrl.toString()
                                        )
                                        isLoading = false
                                        onSaveSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                                "EditProfileView",
                                                "Failed to upload image or get URL",
                                                e
                                        )
                                        isLoading = false
                                        showError = true
                                        errorMessage = "Failed to upload image: ${e.message}"
                                    }
                        } else {
                            userViewModel.updateUserProfile(
                                    displayName = displayName,
                                    photoUrl = currentUserState?.photoUrl
                            )
                            onSaveSuccess()
                        }
                    },
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
        }
    }
}
