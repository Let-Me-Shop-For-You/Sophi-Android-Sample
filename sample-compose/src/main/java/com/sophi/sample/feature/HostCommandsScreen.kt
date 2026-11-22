package com.sophi.sample.feature

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sophi.android.Sophi
import com.sophi.android.SophiChat
import com.sophi.sample.DEMO_MESSAGE
import com.sophi.sample.DEMO_PRODUCT_ID
import kotlinx.coroutines.launch

private const val OUTFIT_REPLY_EXAMPLE =
    "// outfit: Sophi'nin gösterdiği bir SophiOutfit (recommendationId > 0)\n" +
        "Sophi.sendOutfitReply(\"Bu kombine ayakkabı öner\", outfit)"

@Composable
fun HostCommandsScreen(onBack: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val run: (String, () -> Result<Unit>) -> Unit = { label, command ->
        val message = command().fold({ "$label: gönderildi" }, { "$label: ${it.javaClass.simpleName}" })
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    FeatureScreen(
        title = "Host komutları",
        description = "Komutlar sohbet hazır olana kadar sırada bekler ve Result döner. Yanıt akarken gelen ikinci komut " +
            "SophiError.Busy ile reddedilir; mevcut yanıt kesilmez.",
        onBack = onBack,
        snackbarHostState = snackbar,
        scrollable = false,
    ) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { run("sendTextMessage") { Sophi.sendTextMessage(DEMO_MESSAGE) } }) { Text("Mesaj gönder") }
            OutlinedButton(onClick = {
                run("sendProductReply") { Sophi.sendProductReply(DEMO_PRODUCT_ID, "Bu ürüne benzer var mı?") }
            }) { Text("Ürünle sor") }
            OutlinedButton(onClick = { run("sendProductReply") { Sophi.sendProductReply(DEMO_PRODUCT_ID) } }) {
                Text("Ürünü iliştir")
            }
            OutlinedButton(onClick = { run("openProduct") { Sophi.openProduct(DEMO_PRODUCT_ID) } }) { Text("Ürünü aç") }
            OutlinedButton(onClick = { run("openHistory") { Sophi.openHistory() } }) { Text("Geçmiş") }
            OutlinedButton(onClick = { run("toggleHistory") { Sophi.toggleHistory() } }) { Text("Geçmişi aç/kapat") }
            OutlinedButton(enabled = false, onClick = {}) { Text("sendOutfitReply") }
        }
        Text(
            OUTFIT_REPLY_EXAMPLE,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        SophiChat(modifier = Modifier.fillMaxWidth().weight(1f), onClose = onBack)
    }
}
