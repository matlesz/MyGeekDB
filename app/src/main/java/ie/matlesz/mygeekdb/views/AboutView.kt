package ie.matlesz.mygeekdb.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutView(onNavigateBack: () -> Unit) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("About") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "MyGeekDB", style = MaterialTheme.typography.headlineLarge)

            Text(text = "Version 2.0.0", style = MaterialTheme.typography.bodyLarge)

            Text(
                    text =
                            "MyGeekDB is your personal media tracking application. Keep track of your favorite movies and TV series, mark them as favorites, and discover new content.",
                    style = MaterialTheme.typography.bodyMedium
            )

            Text(text = "Features:", style = MaterialTheme.typography.titleMedium)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Browse movies and TV series")
                Text("• Search for specific titles")
                Text("• Mark favorites")
                Text("• Detailed view of each title")
                Text("• Personal profile management")
            }
        }
    }
}
