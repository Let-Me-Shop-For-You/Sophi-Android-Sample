package com.sophi.sample

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sophi.android.Sophi
import com.sophi.android.config.SophiAssets
import com.sophi.android.config.SophiColors
import com.sophi.android.config.SophiConfig
import com.sophi.android.config.SophiEnvironment
import com.sophi.android.config.SophiTexts

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initSophi()
    }

    private fun initSophi() {
        Sophi.configure(
            context = this,
            apiKey = BuildConfig.SOPHI_API_KEY,
            environment = SophiEnvironment.Dev,
            config = SophiConfig(
                assets = SophiAssets(
                    toolbarBrandImage = R.drawable.ic_ai,
                    assistantAvatarImage = R.drawable.ic_vakko,
                ),
                texts = SophiTexts(
                    toolbarLabel = "Vakko Asistan",
                    toolbarDescription = "Size nasıl yardımcı olabilirim?",
                    assistantLabel = "Vakko Asistan",
                    inputPlaceholderText = "Herhangi bir şey sorun",
                ),
                colors = SophiColors(
                    senderBubbleBackgroundColor = Color(0xFFC9A970).copy(alpha = 0.1f).toArgb(),
                ),
                showAssistantAvatar = true,
                showAssistantLabel = true,
            ),
            enableDebugLogs = true,
            externalUserId = "sample-user-id",
        )
    }
}
