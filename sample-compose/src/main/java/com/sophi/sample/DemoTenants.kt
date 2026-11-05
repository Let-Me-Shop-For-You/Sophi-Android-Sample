package com.sophi.sample

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sophi.android.config.SophiAssets
import com.sophi.android.config.SophiColors
import com.sophi.android.config.SophiConfig
import com.sophi.android.config.SophiEnvironment
import com.sophi.android.config.SophiTexts

/** A supported test tenant: its widget API key comes from the gitignored local.properties. */
data class DemoTenant(val code: String, val label: String, val apiKey: String, val config: SophiConfig = SophiConfig()) {
    val hasKey: Boolean get() = apiKey.isNotBlank()
}

object DemoTenants {
    val all: List<DemoTenant> = listOf(
        // Vakko doubles as the branding example: host assets, texts and a colour override on top of the tenant theme.
        DemoTenant(
            "vakko", "Vakko", BuildConfig.VAKKO_API_KEY,
            SophiConfig(
                assets = SophiAssets(toolbarBrandImage = R.drawable.ic_ai, assistantAvatarImage = R.drawable.ic_vakko),
                texts = SophiTexts(toolbarLabel = "Vakko Asistan"),
                colors = SophiColors(senderBubbleBackgroundColor = Color(0xFFC9A970).copy(alpha = 0.1f).toArgb()),
            ),
        ),
        DemoTenant("mns", "Marks & Spencer", BuildConfig.MNS_API_KEY),
        DemoTenant("kiko", "KIKO Milano", BuildConfig.KIKO_API_KEY),
        DemoTenant("boyner", "Boyner", BuildConfig.BOYNER_API_KEY),
        DemoTenant("beymen", "Beymen", BuildConfig.BEYMEN_API_KEY),
    )

    /** `SOPHI_ENV=dev|prod`; anything else is reported in the event log and treated as dev, never as prod. */
    val environment: SophiEnvironment
        get() = when (BuildConfig.SOPHI_ENV.lowercase()) {
            "prod" -> SophiEnvironment.Prod
            "dev", "" -> SophiEnvironment.Dev
            else -> {
                DemoEventLog.log("SOPHI_ENV='${BuildConfig.SOPHI_ENV}' is not dev|prod; using dev")
                SophiEnvironment.Dev
            }
        }

    fun byCode(code: String?): DemoTenant = all.firstOrNull { it.code == code } ?: all.first()

    fun remembered(context: Context): DemoTenant = byCode(prefs(context).getString(KEY_TENANT, null))

    fun remember(context: Context, tenant: DemoTenant) {
        if (prefs(context).getString(KEY_TENANT, null) == tenant.code) return
        prefs(context).edit().putString(KEY_TENANT, tenant.code).apply()
    }

    /** The shopper id the demo signed in with, so a relaunch configures the same identity the SDK persisted. */
    fun rememberedUser(context: Context): String? = prefs(context).getString(KEY_USER, null)?.takeIf { it.isNotBlank() }

    fun rememberUser(context: Context, externalUserId: String?) {
        prefs(context).edit().putString(KEY_USER, externalUserId?.takeIf { it.isNotBlank() }).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private const val PREFS = "sophi_demo"
    private const val KEY_TENANT = "tenant"
    private const val KEY_USER = "externalUserId"
}
