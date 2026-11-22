package com.sophi.sample.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sophi.android.Sophi
import com.sophi.android.data.model.SophiCart
import com.sophi.android.data.model.SophiCartLine
import com.sophi.sample.DEMO_PRODUCT_ID
import com.sophi.sample.EventLog
import com.sophi.sample.SampleSettings

@Composable
fun CommerceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var favoriteId by rememberSaveable { mutableStateOf("") }
    val rowModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)

    FeatureScreen(
        title = "Ticaret",
        description = "Sepete ekleme onAddToCart ile uygulamaya gelir ve beklenir: Result.failure dönerse SDK hatayı gösterir ve " +
            "checkout'u kilitler. updateCart uygulamanın sepetini bir sonraki mesaja ekler. Gelen her callback Olaylar ekranında görünür.",
        onBack = onBack,
    ) {
        Button(onClick = {
            Sophi.updateCart(SophiCart("demo-cart", listOf(SophiCartLine(DEMO_PRODUCT_ID, "$DEMO_PRODUCT_ID-M", quantity = 1))))
            EventLog.add("updateCart", "demo-cart: 1 satır")
        }, modifier = rowModifier) { Text("Örnek sepeti gönder (updateCart)") }

        SwitchRow("Sepete yazma başarısız olsun", SampleSettings.cartWriteFails, rowModifier) { SampleSettings.cartWriteFails = it }
        SwitchRow("Ürün sayfasını uygulama açsın", SampleSettings.hostOpensProductPage, rowModifier) {
            SampleSettings.hostOpensProductPage = it
        }

        Text("updateFavorite (Sophi'nin ürün id'si)", style = MaterialTheme.typography.titleSmall, modifier = rowModifier)
        OutlinedTextField(
            favoriteId,
            { favoriteId = it.filter(Char::isDigit) },
            label = { Text("Ürün id") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = rowModifier,
        )
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(true to "Favori yap", false to "Favoriden çıkar").forEach { (isFavorite, label) ->
                OutlinedButton(enabled = favoriteId.toIntOrNull() != null, onClick = {
                    Sophi.updateFavorite(favoriteId.toInt(), isFavorite)
                    EventLog.add("updateFavorite", "$favoriteId → $isFavorite")
                }) { Text(label) }
            }
        }

        Button(onClick = { Sophi.start(context) }, modifier = rowModifier) { Text("Sohbeti aç") }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, modifier: Modifier, onChange: (Boolean) -> Unit) {
    Row(
        modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
