package com.sophi.sample

import android.app.Application
import android.content.Intent
import com.sophi.android.Sophi
import com.sophi.android.config.SophiEnvironment
import com.sophi.android.config.SophiListener
import com.sophi.android.config.SophiLocale
import com.sophi.android.data.model.SophiCartItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Minimal Views host. Uses a dev tenant and a local cart; never places an order. */
class SampleApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val configurationLock = Mutex()
    private var generation = 0L
    val ready = MutableStateFlow(false)
    var localDemo = true
        private set
    var configured = false
        private set

    override fun onCreate() {
        super.onCreate()
        configure(getSharedPreferences("views_demo", MODE_PRIVATE).getBoolean("local", true))
    }

    fun configure(local: Boolean) {
        if (ready.value && local == localDemo) return
        generation++; val current = generation
        configured = false; ready.value = false; DemoTracking.invalidate()
        localDemo = local
        getSharedPreferences("views_demo", MODE_PRIVATE).edit().putBoolean("local", local).apply()
        if (!local && BuildConfig.MNS_API_KEY.isBlank()) { DemoTracking.log("Add a development key to local.properties"); return }
        val key = if (local) "demo-android-views" else BuildConfig.MNS_API_KEY
        val environment = if (local) SophiEnvironment.Custom(DemoTracking.ORIGIN, DemoTracking.ORIGIN) else SophiEnvironment.Dev
        val shopper = getSharedPreferences("views_demo", MODE_PRIVATE).getString("shopper", null)
        MerchantCart.clear(); Sophi.reset()
        scope.launch { configurationLock.withLock {
          if (current != generation) return@withLock
          try {
            DemoTracking.configure(this@SampleApplication, key, "https://" + environment.baseUrl, if (local) "local" else "mns", shopper)
            if (current != generation) return@withLock
            DemoTracking.shopperChanged = { getSharedPreferences("views_demo", MODE_PRIVATE).edit().putString("shopper", it).apply() }
            Sophi.configure(
            context = this@SampleApplication,
            apiKey = key,
            environment = environment,
            locale = if (local) SophiLocale.EN else SophiLocale.TR,
            externalUserId = shopper,
            persistIdentity = true,
            enableDebugLogs = false,
            metadata = mapOf("source" to "android-views-demo", "testSource" to "mobile-parity-2026-11"),
            listener = object : SophiListener {
                override suspend fun onAddToCart(items: List<SophiCartItem>): Result<Unit> = MerchantCart.add(items)
                override fun onCheckout() {
                    DemoTracking.checkout()
                    startActivity(Intent(this@SampleApplication, CheckoutActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            },
        )
        configured = true
        MerchantCart.onSnapshot = { Sophi.updateCart(it) }
        MerchantCart.push()
        ready.value = true
          } catch (cancelled: CancellationException) { throw cancelled }
          catch (_: Exception) { DemoTracking.log("Unable to configure chat + events") }
        } }
    }
}
