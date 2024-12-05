import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ie.matlesz.mygeekdb.viewmodel.UserViewModel

@Composable
fun DeleteAccountDialog(
  onDismiss: () -> Unit,
  onSuccess: () -> Unit,
  viewModel: UserViewModel
) {
  var password by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = { onDismiss() },
    title = { Text("Delete Account") },
    text = {
      Column {
        Text("Please enter your password to confirm account deletion.")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
          isError = error != null
        )
        if (error != null) {
          Text(
            text = error!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          isLoading = true
          error = null
          viewModel.deleteUser(
            password = password,
            onSuccess = {
              isLoading = false
              onSuccess()
            },
            onError = { e ->
              isLoading = false
              error = e.message
            }
          )
        },
        enabled = password.isNotBlank() && !isLoading,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        )
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary
          )
        } else {
          Text("Delete Account")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}