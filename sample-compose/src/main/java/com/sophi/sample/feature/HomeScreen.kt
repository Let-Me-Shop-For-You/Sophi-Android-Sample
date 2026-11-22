package com.sophi.sample.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sophi.sample.DEMO_MESSAGE
import com.sophi.sample.DEMO_PRODUCT_ID
import com.sophi.sample.Route

private class HomeEntry(val title: String, val description: String, val actions: List<Pair<String, Route>>)

private val entries = listOf(
    HomeEntry("Tam ekran açılış", "Sophi.start: SDK'nın kendi ekranı, mesajla veya ürünle.", listOf("" to Route.Launch)),
    HomeEntry(
        "Compose'a gömme",
        "SophiChat: sohbet uygulamanın kendi ekranında, kendi toolbar'ının altında.",
        listOf(
            "Aç" to Route.EmbeddedChat(),
            "Mesajla" to Route.EmbeddedChat(message = DEMO_MESSAGE),
            "Ürünle" to Route.EmbeddedChat(productId = DEMO_PRODUCT_ID),
        ),
    ),
    HomeEntry("Görünüm", "SophiConfig: renk, metin, logo, avatar, kapatma butonu ve dil.", listOf("" to Route.Appearance)),
    HomeEntry("Kimlik", "Giriş, çıkış, kullanıcı adı, kimliğin cihazda tutulması ve oturum bilgisi.", listOf("" to Route.Identity)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpen: (Route) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Sophi SDK örnekleri") }) }) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding)) {
            items(entries) { entry ->
                val single = entry.actions.singleOrNull()?.second
                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(if (single != null) Modifier.clickable { onOpen(single) } else Modifier)
                        .padding(16.dp),
                ) {
                    Text(entry.title, style = MaterialTheme.typography.titleMedium)
                    Text(entry.description, style = MaterialTheme.typography.bodySmall)
                    if (single == null) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            entry.actions.forEach { (label, route) ->
                                OutlinedButton(onClick = { onOpen(route) }) { Text(label) }
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
