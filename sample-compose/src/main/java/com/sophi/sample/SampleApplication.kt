package com.sophi.sample

import android.app.Application
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sophi.android.Sophi
import com.sophi.android.config.SophiAssets
import com.sophi.android.config.SophiColors
import com.sophi.android.config.SophiConfig
import com.sophi.android.config.SophiEnvironment
import com.sophi.android.config.SophiListener
import com.sophi.android.config.SophiTexts
import com.sophi.android.data.model.SophiCartItem
import com.sophi.android.data.model.SophiProductDetail

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
            listener = object : SophiListener {
                // A real app writes the items to its own cart here and returns the result.
                override suspend fun onAddToCart(items: List<SophiCartItem>): Result<Unit> {
                    toast("Sepete eklendi: ${items.joinToString { it.product?.displayName ?: it.externalId }}")
                    return Result.success(Unit)
                }

                override fun onAddToFavorite(product: SophiProductDetail, isFavorite: Boolean) {
                    toast(if (isFavorite) "Favorilere eklendi" else "Favorilerden çıkarıldı")
                }

                override fun onCheckout() {
                    toast("Ödeme sayfasına gidiliyor")
                }

                override fun onClose() {
                    toast("Sophi kapatıldı")
                }
            },
            enableDebugLogs = true,
            externalUserId = "sample-user-id",
        )
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
