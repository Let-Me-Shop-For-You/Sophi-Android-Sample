package com.sophi.sample

import android.content.Context
import android.content.Intent
import com.sophi.android.Sophi
import com.sophi.android.config.SophiError
import com.sophi.android.config.SophiListener
import com.sophi.android.config.SophiLocale
import com.sophi.android.data.model.SophiCartItem
import com.sophi.android.data.model.SophiProductDetail
import com.sophi.android.data.model.SophiSessionInfo
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * The merchant side of the integration: one configure call per tenant, one listener that owns
 * the cart acknowledgement, pushes cart snapshots back and routes checkout to the demo screen.
 */
object DemoSophi {

    /** True once a tenant with a key has been configured; the UI gates every SDK call on it. */
    val configured = MutableStateFlow(false)

    /** Returns false when the tenant has no key: nothing is configured and the UI keeps the chat closed. */
    fun configure(context: Context, tenant: DemoTenant, externalUserId: String? = null): Boolean {
        val app = context.applicationContext
        val previous = DemoTenants.remembered(app)
        DemoTenants.remember(app, tenant)
        // A merchant cart belongs to one store: switching tenant starts from an empty one.
        if (previous.code != tenant.code) MerchantCart.clear()
        if (!tenant.hasKey) {
            configured.value = false
            DemoEventLog.log("no API key for ${tenant.code} in local.properties; SDK not configured")
            return false
        }
        val environment = DemoTenants.environment
        Sophi.configure(
            context = app,
            apiKey = tenant.apiKey,
            environment = environment,
            config = tenant.config,
            listener = DemoListener(app),
            externalUserId = externalUserId,
            metadata = mapOf("source" to "android-demo", "tenant" to tenant.code),
            locale = SophiLocale.TR,
            enableDebugLogs = true,
        )
        configured.value = true
        MerchantCart.push()
        DemoEventLog.log("configure(${tenant.code}, $environment, user=${externalUserId ?: "guest"})")
        return true
    }

    private class DemoListener(private val app: Context) : SophiListener {
        override fun onReady(session: SophiSessionInfo) {
            DemoEventLog.log("onReady tenant=${session.companyCode} layout=${session.layoutId} chat=${session.chatSessionId}")
        }

        override fun onError(error: SophiError) {
            DemoEventLog.log("onError ${error::class.simpleName}: ${error.message}")
        }

        /** Awaited: the SDK only reports success (and allows checkout) once the merchant cart accepted the items. */
        override suspend fun onAddToCart(items: List<SophiCartItem>): Result<Unit> {
            DemoEventLog.log("onAddToCart ${items.joinToString { "${it.externalId}/${it.variantExternalId ?: "?"} x${it.quantity}" }}")
            val result = MerchantCart.add(items)
            result.onSuccess { DemoEventLog.log("cart acknowledged, snapshot pushed (${MerchantCart.lines.value.size} lines)") }
                .onFailure { DemoEventLog.log("cart write FAILED: ${it.message}") }
            return result
        }

        override fun onAddToFavorite(product: SophiProductDetail, isFavorite: Boolean) {
            DemoEventLog.log("onAddToFavorite ${product.externalId ?: product.id} → $isFavorite")
        }

        override fun onCheckout() {
            DemoEventLog.log("onCheckout → demo checkout screen")
            app.startActivity(Intent(app, DemoCheckoutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        override fun onOpenProductPage(url: String, product: SophiProductDetail?): Boolean {
            DemoEventLog.log("onOpenProductPage ${product?.externalId ?: "?"} $url (left to the SDK browser handoff)")
            return false
        }

        override fun onClose() {
            DemoEventLog.log("onClose")
        }
    }
}
