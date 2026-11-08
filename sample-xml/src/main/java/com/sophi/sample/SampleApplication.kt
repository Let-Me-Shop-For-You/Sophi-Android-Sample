package com.sophi.sample

import android.app.Application
import android.content.Intent
import com.sophi.android.Sophi
import com.sophi.android.config.SophiEnvironment
import com.sophi.android.config.SophiListener
import com.sophi.android.config.SophiLocale
import com.sophi.android.data.model.SophiCartItem

/** Minimal Views host. Uses a dev tenant and a local cart; never places an order. */
class SampleApplication : Application() {
    var configured = false
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.MNS_API_KEY.isBlank()) return
        Sophi.configure(
            context = this,
            apiKey = BuildConfig.MNS_API_KEY,
            environment = SophiEnvironment.Dev,
            locale = SophiLocale.TR,
            persistIdentity = false,
            enableDebugLogs = false,
            metadata = mapOf("source" to "android-views-demo", "testSource" to "mobile-parity-2026-11"),
            listener = object : SophiListener {
                override suspend fun onAddToCart(items: List<SophiCartItem>): Result<Unit> = MerchantCart.add(items)
                override fun onCheckout() {
                    startActivity(Intent(this@SampleApplication, CheckoutActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            },
        )
        configured = true
        MerchantCart.onSnapshot = { Sophi.updateCart(it) }
        MerchantCart.push()
    }
}
