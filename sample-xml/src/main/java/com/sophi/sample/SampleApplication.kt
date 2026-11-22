package com.sophi.sample

import android.app.Application
import android.widget.Toast
import androidx.core.graphics.toColorInt
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
                assets = SophiAssets(toolbarBrandImage = R.drawable.ic_ai),
                texts = SophiTexts(
                    toolbarLabel = "Demo Mağaza",
                    toolbarDescription = "Size nasıl yardımcı olabilirim?",
                    assistantLabel = "Demo Asistan",
                    inputPlaceholderText = "Herhangi bir şey sorun",
                ),
                colors = SophiColors(
                    senderBubbleBackgroundColor = "#1AC9A970".toColorInt(),
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
        )
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
