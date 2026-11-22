package com.sophi.sample.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sophi.android.Sophi
import com.sophi.android.data.model.SophiSessionInfo
import com.sophi.sample.SampleSettings
import com.sophi.sample.SignedInUser
import kotlinx.coroutines.launch

@Composable
fun IdentityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var customerId by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf(SampleSettings.userName.orEmpty()) }
    var status by rememberSaveable { mutableStateOf("") }
    var session by remember { mutableStateOf(Sophi.currentSessionInfo()) }
    val refresh = { session = Sophi.currentSessionInfo() }
    val report: (String, Result<Unit>) -> Unit = { action, result ->
        status = result.fold({ "$action: tamam" }, { "$action: ${it.javaClass.simpleName} (${it.message})" })
        refresh()
    }
    val forgetName = {
        SampleSettings.userName = null
        name = ""
    }
    val fieldModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)

    FeatureScreen(
        title = "Kimlik",
        description = "Uygulamaya giriş yapan müşteriyi setExternalUserId ile bağlayın, çıkışta reset çağırın. " +
            "endSession yalnızca mevcut sohbeti bitirir, çıkış değildir.",
        onBack = onBack,
    ) {
        OutlinedTextField(customerId, { customerId = it }, label = { Text("Müşteri id") }, singleLine = true, modifier = fieldModifier)
        Row(fieldModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = customerId.isNotBlank(),
                onClick = {
                    val id = customerId.trim()
                    scope.launch {
                        val result = Sophi.setExternalUserId(id)
                        if (result.isSuccess) {
                            // Another account: the SDK drops the previous shopper's name, so does the app.
                            val previous = SignedInUser.load(context)
                            if (previous != null && previous != id) forgetName()
                            SignedInUser.save(context, id)
                        }
                        report("Giriş", result)
                    }
                },
            ) { Text("Giriş yap") }
            OutlinedButton(onClick = {
                Sophi.reset()
                SignedInUser.save(context, null)
                forgetName()
                status = "Çıkış: kimlik temizlendi"
                refresh()
            }) { Text("Çıkış yap (reset)") }
        }

        OutlinedTextField(name, { name = it }, label = { Text("Karşılamada görünen ad") }, singleLine = true, modifier = fieldModifier)
        OutlinedButton(onClick = {
            SampleSettings.userName = name.takeIf { it.isNotBlank() }
            Sophi.setUserName(SampleSettings.userName)
            status = "Ad uygulandı"
        }, modifier = fieldModifier) { Text("setUserName") }

        Row(
            fieldModifier.toggleable(value = SampleSettings.persistIdentity, role = Role.Switch) {
                SampleSettings.persistIdentity = it
                Sophi.setIdentityPersistenceEnabled(it)
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Kimliği cihazda tut", modifier = Modifier.weight(1f))
            Switch(checked = SampleSettings.persistIdentity, onCheckedChange = null)
        }

        Row(fieldModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { scope.launch { report("Sohbeti bitir", Sophi.endSession()) } }) { Text("endSession") }
            Button(onClick = { Sophi.start(context) }) { Text("Sohbeti aç") }
        }

        if (status.isNotEmpty()) Text(status, style = MaterialTheme.typography.bodyMedium, modifier = fieldModifier)

        SessionCard(session, onRefresh = refresh, modifier = fieldModifier)
    }
}

@Composable
private fun SessionCard(session: SophiSessionInfo?, onRefresh: () -> Unit, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("currentSessionInfo()", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = onRefresh) { Text("Yenile") }
            }
            if (session == null) {
                Text("Henüz oturum yok: sohbeti bir kez açın.")
            } else {
                Text("clientId: ${session.clientId}")
                Text("companyCode: ${session.companyCode}")
                Text("layoutId: ${session.layoutId}")
                Text("chatSessionId: ${session.chatSessionId ?: "-"}")
                Text("externalUserId: ${session.externalUserId ?: "misafir"}")
                Text("userName: ${session.userName ?: "-"}")
            }
        }
    }
}
