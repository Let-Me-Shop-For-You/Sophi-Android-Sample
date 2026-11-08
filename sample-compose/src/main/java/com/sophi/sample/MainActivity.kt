package com.sophi.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sophi.android.Sophi
import com.sophi.sample.ui.theme.SophiAndroidTheme
import kotlinx.coroutines.launch

/**
 * Merchant demo home: pick a tenant, sign a shopper in or out, open the chat in the supported
 * ways, drive host commands, watch the merchant cart and the callback log.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SophiAndroidTheme { DemoHome() } }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DemoHome() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tenant by remember { mutableStateOf(DemoTenants.remembered(context)) }
    // Seeded from the identity the demo signed in with, which is also what the SDK persisted.
    var userId by remember { mutableStateOf(DemoTenants.rememberedUser(context).orEmpty()) }
    var message by remember { mutableStateOf("Bana bir kombin öner") }
    var productId by remember { mutableStateOf("") }
    val failNext by MerchantCart.failNextWrite.collectAsState()
    val lines by MerchantCart.lines.collectAsState()
    val events by DemoEventLog.events.collectAsState()
    val configured by DemoSophi.configured.collectAsState()
    var identityBusy by remember { mutableStateOf(false) }
    val ready = tenant.hasKey && configured && !identityBusy

    /** Identity changes reconfigure the SDK, which drops its cart snapshot (BND-03): the merchant re-pushes its own. */
    fun changeIdentity(externalUserId: String?) {
        if (identityBusy) return
        identityBusy = true
        val previous = DemoTenants.rememberedUser(context)
        DemoTenants.rememberUser(context, externalUserId)
        // Clear the old account cart before awaiting authentication; a failure can still leave
        // the SDK configured for the replacement identity. Guest-to-account keeps its cart.
        if (previous != null && previous != externalUserId) MerchantCart.clear()
        scope.launch {
            try {
                val result = Sophi.setExternalUserId(externalUserId)
                DemoEventLog.log("setExternalUserId → ${result.fold({ "ok" }, { "failed: ${it::class.simpleName}" })}")
                if (result.isSuccess) MerchantCart.push()
            } finally { identityBusy = false }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Sophi merchant demo", style = MaterialTheme.typography.headlineSmall)
        Text("Environment: ${DemoTenants.environment.baseUrl}", style = MaterialTheme.typography.bodySmall)

        Section("Tenant")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            DemoTenants.all.forEach { t ->
                FilterChip(
                    selected = t.code == tenant.code,
                    enabled = !identityBusy,
                    onClick = {
                        if (t.code == tenant.code) return@FilterChip
                        tenant = t
                        DemoSophi.configure(context, t, userId.ifBlank { null })
                    },
                    label = { Text(if (t.hasKey) t.label else "${t.label} (no key)") },
                )
            }
        }
        if (!tenant.hasKey) Text("Add ${tenant.code.uppercase()}_API_KEY to local.properties to use this tenant.", style = MaterialTheme.typography.bodySmall)

        Section("Shopper identity")
        OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("externalUserId") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { changeIdentity(userId.trim()) }, enabled = ready && userId.isNotBlank()) { Text("Sign in") }
            OutlinedButton(onClick = { userId = ""; changeIdentity(null) }, enabled = ready) { Text("Sign out") }
            OutlinedButton(
                enabled = ready,
                onClick = {
                    Sophi.reset()
                    userId = ""
                    DemoTenants.rememberUser(context, null)
                    MerchantCart.clear()
                    DemoEventLog.log("reset() — identity and merchant cart cleared")
                },
            ) { Text("Reset") }
        }

        Section("Open chat")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { Sophi.start(context) }, enabled = ready) { Text("Open") }
            Button(onClick = { Sophi.start(context, initialMessage = message) }, enabled = ready) { Text("With message") }
            Button(onClick = { Sophi.start(context, externalProductId = productId.ifBlank { null }) }, enabled = ready && productId.isNotBlank()) { Text("With product") }
        }
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("initial message / host text") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = productId, onValueChange = { productId = it }, label = { Text("merchant product id") }, modifier = Modifier.fillMaxWidth())

        Section("Host commands (while the chat is open)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { log("sendTextMessage", Sophi.sendTextMessage(message)) }, enabled = ready) { Text("Send text") }
            OutlinedButton(onClick = { log("sendProductReply", Sophi.sendProductReply(productId, message)) }, enabled = ready && productId.isNotBlank()) { Text("Product reply") }
            OutlinedButton(onClick = { log("toggleHistory", Sophi.toggleHistory()) }, enabled = ready) { Text("History") }
        }

        Section("Merchant cart (${lines.size} lines)")
        lines.forEach { line ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(line.label, modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    MerchantCart.remove(line.key)
                    DemoEventLog.log("removed ${line.key}, snapshot pushed")
                }) { Text("Remove") }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = failNext, onCheckedChange = { MerchantCart.failNextWrite.value = it })
            Text("Reject the next cart write")
            Spacer(Modifier.weight(1f))
            Button(onClick = { context.startActivity(Intent(context, DemoCheckoutActivity::class.java)) }, enabled = lines.isNotEmpty()) { Text("Checkout") }
        }

        Section("Event log")
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            events.take(MAX_VISIBLE_EVENTS).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (events.isEmpty()) Text("No events yet.", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String) {
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
}

private fun log(command: String, result: Result<Unit>) {
    DemoEventLog.log("$command → ${result.fold({ "queued/sent" }, { "rejected: ${it::class.simpleName}" })}")
}

private const val MAX_VISIBLE_EVENTS = 40
