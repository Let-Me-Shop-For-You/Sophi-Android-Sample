package com.sophi.sample

import android.app.Application
import androidx.core.graphics.toColorInt
import com.sophi.android.Sophi
import com.sophi.android.config.SophiAssets
import com.sophi.android.config.SophiColors
import com.sophi.android.config.SophiConfig
import com.sophi.android.config.SophiTexts

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initSophi()
    }

    private fun initSophi() {
        Sophi.init(
            baseUrl = "your base url",
            apiKey = "your api key",
            clientId = "your client id",
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
                    senderBubbleBackgroundColor = "#C9A970".toColorInt(),
                ),
                showAssistantAvatar = true,
                showAssistantLabel = true,
            ),
            enableDebugLogs = true,
            externalUserId = "sample-user-id",
        )
    }
}
