package com.sophi.sample.feature

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sophi.android.SophiChat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddedChatScreen(
    initialMessage: String?,
    externalProductId: String?,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uygulamanın toolbar'ı") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
    ) { innerPadding ->
        SophiChat(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            onClose = onBack,
            initialMessage = initialMessage,
            externalProductId = externalProductId,
        )
    }
}
