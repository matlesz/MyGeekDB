package ie.matlesz.mygeekdb.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DrawerMenuItem(text: String, onClick: () -> Unit) {
  Box(
          modifier =
                  Modifier.fillMaxWidth()
                          .clickable { onClick() }
                          .padding(horizontal = 16.dp, vertical = 12.dp),
          contentAlignment = Alignment.Center
  ) {
    Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
    )
  }
}
