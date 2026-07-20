package dev.catandbunny.cloudbuddy.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CloudTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.headlineSmall) }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(start = if (onBack == null) 12.dp else 0.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        action?.invoke()
    }
}
