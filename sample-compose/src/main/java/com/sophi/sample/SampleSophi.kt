package com.sophi.sample

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
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
            externalUserId = SignedInUser.load(appContext),
            metadata = mapOf("appVersion" to BuildConfig.VERSION_NAME),
            userName = SampleSettings.userName,
            locale = SampleSettings.locale,
            persistIdentity = SampleSettings.persistIdentity,
            enableDebugLogs = BuildConfig.DEBUG,
        )
    }
}

private class SampleListener(private val context: Context) : SophiListener {

    override fun onReady(session: SophiSessionInfo) {
        EventLog.add("onReady", "chat=${session.chatSessionId ?: "-"}")
    }

    override fun onError(error: SophiError) {
        EventLog.add("onError", "${error.javaClass.simpleName}: ${error.message}")
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

    // Return true only after the app navigated itself; false lets the SDK open the page.
    override fun onOpenProductPage(url: String, product: SophiProductDetail?): Boolean {
        if (!SampleSettings.hostOpensProductPage) {
            EventLog.add("onOpenProductPage", "SDK açıyor: $url")
            return false
        }
        // A real app opens its own product screen here; the sample hands the URL to the browser.
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            EventLog.add("onOpenProductPage", "uygulama açtı: $url")
            true
        } catch (expected: ActivityNotFoundException) {
            EventLog.add("onOpenProductPage", "açılamadı, SDK açıyor: $url")
            false
        }
    }

    override fun onClose() {
        EventLog.add("onClose")
    }
}
