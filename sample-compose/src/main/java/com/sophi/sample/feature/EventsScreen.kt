package com.sophi.sample.feature

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sophi.sample.EventLog

@Composable
fun EventsScreen(onBack: () -> Unit) {
    val events by EventLog.events.collectAsState()
    FeatureScreen(
        title = "Olaylar",
        description = "SampleSophi'deki listener her callback'i buraya yazar: onReady, onError, onAddToCart, onAddToFavorite, " +
            "onCheckout, onOpenProductPage, onClose. En yeni olay en üstte.",
        onBack = onBack,
        scrollable = false,
    ) {
        OutlinedButton(onClick = EventLog::clear, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Temizle") }
        if (events.isEmpty()) {
            Text("Henüz olay yok. Sohbeti açıp bir şey deneyin.", modifier = Modifier.padding(horizontal = 16.dp))
        }
        LazyColumn {
            items(events) { event ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("${event.time}  ${event.name}", style = MaterialTheme.typography.titleSmall)
                    if (event.detail.isNotEmpty()) Text(event.detail, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
            }
        }
    }
}
