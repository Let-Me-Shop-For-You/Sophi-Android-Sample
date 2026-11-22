package com.sophi.sample.feature

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sophi.android.Sophi
import com.sophi.sample.DEMO_MESSAGE
import com.sophi.sample.DEMO_PRODUCT_ID

@Composable
fun LaunchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    FeatureScreen(
        title = "Tam ekran açılış",
        description = "Sophi.start sohbeti SDK'nın kendi ekranında açar. initialMessage ekran hazır olunca gönderilir; " +
            "externalProductId ürünü çözüp mesaja bağlam olarak iliştirir.",
        onBack = onBack,
    ) {
        val modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        Button(onClick = { Sophi.start(context) }, modifier = modifier) { Text("Sophi.start(context)") }
        Button(onClick = { Sophi.start(context, initialMessage = DEMO_MESSAGE) }, modifier = modifier) {
            Text("initialMessage ile")
        }
        Button(onClick = { Sophi.start(context, externalProductId = DEMO_PRODUCT_ID) }, modifier = modifier) {
            Text("externalProductId ile")
        }
        Button(
            onClick = { Sophi.start(context, initialMessage = "Bu ürüne benzer var mı?", externalProductId = DEMO_PRODUCT_ID) },
            modifier = modifier,
        ) { Text("İkisi birlikte") }
    }
}
