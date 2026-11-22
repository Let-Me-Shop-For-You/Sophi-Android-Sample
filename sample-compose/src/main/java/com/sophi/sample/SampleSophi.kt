package com.sophi.sample

import android.content.Context
import android.widget.Toast
import com.sophi.android.Sophi
import com.sophi.android.config.SophiAssets
import com.sophi.android.config.SophiColors
import com.sophi.android.config.SophiConfig
import com.sophi.android.config.SophiEnvironment
import com.sophi.android.config.SophiError
import com.sophi.android.config.SophiListener
import com.sophi.android.config.SophiTexts
import com.sophi.android.data.model.SophiCartItem
import com.sophi.android.data.model.SophiProductDetail
import com.sophi.android.data.model.SophiSessionInfo

enum class AppearancePreset(val title: String) {
    Default("Varsayılan"),
    Branded("Özel renk ve metin"),
    Minimal("Avatarsız, kapatma butonsuz");

    fun toConfig(): SophiConfig = when (this) {
        Default -> SophiConfig(
            texts = SophiTexts(toolbarLabel = "Demo Mağaza", assistantLabel = "Demo Mağaza"),
        )
        Branded -> SophiConfig(
            assets = SophiAssets(toolbarBrandImage = R.drawable.ic_ai),
            texts = SophiTexts(
                toolbarLabel = "Demo Mağaza",
                toolbarDescription = "Size nasıl yardımcı olabilirim?",
                assistantLabel = "Demo Asistan",
                inputPlaceholderText = "Herhangi bir şey sorun",
            ),
            colors = SophiColors(
                senderBubbleBackgroundColor = 0x1AC9A970,
                primaryColor = 0xFF8C6D3F.toInt(),
            ),
        )
        Minimal -> SophiConfig(
            texts = SophiTexts(toolbarLabel = "Demo Mağaza"),
            showAssistantAvatar = false,
            showAssistantLabel = false,
            showCloseButton = false,
        )
    }
}

object SampleSophi {

    fun configure(context: Context) {
        val appContext = context.applicationContext
        Sophi.configure(
            context = appContext,
            apiKey = BuildConfig.SOPHI_API_KEY,
            environment = SophiEnvironment.Dev,
            config = SampleSettings.preset.toConfig(),
            listener = SampleListener(appContext),
            // Keeps a signed-in shopper when the catalog reconfigures for another preset.
            externalUserId = Sophi.currentSessionInfo()?.externalUserId,
            metadata = mapOf("appVersion" to "1.0"),
            userName = SampleSettings.userName,
            locale = SampleSettings.locale,
            persistIdentity = SampleSettings.persistIdentity,
            enableDebugLogs = true,
        )
    }
}

private class SampleListener(private val context: Context) : SophiListener {

    override fun onReady(session: SophiSessionInfo) {
        EventLog.add("onReady", "chat=${session.chatSessionId ?: "-"}")
    }

    override fun onError(error: SophiError) {
        EventLog.add("onError", error.javaClass.simpleName)
    }

    // A real app writes the items to its own cart here and returns the result.
    override suspend fun onAddToCart(items: List<SophiCartItem>): Result<Unit> {
        val names = items.joinToString { it.product?.displayName ?: it.externalId }
        if (SampleSettings.cartWriteFails) {
            EventLog.add("onAddToCart", "başarısız: $names")
            return Result.failure(IllegalStateException("Demo: sepete yazılamadı"))
        }
        EventLog.add("onAddToCart", names)
        Toast.makeText(context, "Sepete eklendi: $names", Toast.LENGTH_SHORT).show()
        return Result.success(Unit)
    }

    override fun onAddToFavorite(product: SophiProductDetail, isFavorite: Boolean) {
        EventLog.add("onAddToFavorite", "${product.id} → $isFavorite")
    }

    override fun onCheckout() {
        EventLog.add("onCheckout")
    }

    override fun onOpenProductPage(url: String, product: SophiProductDetail?): Boolean {
        val handled = SampleSettings.hostOpensProductPage
        EventLog.add("onOpenProductPage", if (handled) "uygulama açtı: $url" else "SDK açıyor: $url")
        return handled
    }

    override fun onClose() {
        EventLog.add("onClose")
    }
}
