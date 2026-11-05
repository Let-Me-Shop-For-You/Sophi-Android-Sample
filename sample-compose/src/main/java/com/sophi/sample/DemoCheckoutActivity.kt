package com.sophi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sophi.sample.ui.theme.SophiAndroidTheme

/** Merchant-owned checkout: shows the demo cart and "places" an order locally. No payment, no backend. */
class DemoCheckoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SophiAndroidTheme {
                val lines by MerchantCart.lines.collectAsState()
                val total = lines.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
                val currency = lines.firstNotNullOfOrNull { it.currency }
                Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
                    Text("Demo checkout", style = MaterialTheme.typography.headlineSmall)
                    Text("Merchant-owned screen opened from SophiListener.onCheckout()", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(lines, key = { it.key }) { line ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(line.label)
                                Text(line.lineTotal)
                            }
                            HorizontalDivider()
                        }
                        if (lines.isEmpty()) item { Text("Cart is empty.") }
                    }
                    Text("Total: ${formatAmount(total, currency)}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = lines.isNotEmpty(),
                        onClick = {
                            DemoEventLog.log("demo order placed (${lines.size} lines, total ${formatAmount(total, currency)}) — no payment")
                            MerchantCart.clear()
                            finish()
                        },
                    ) { Text("Place demo order") }
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { finish() }) { Text("Back") }
                }
            }
        }
    }
}
