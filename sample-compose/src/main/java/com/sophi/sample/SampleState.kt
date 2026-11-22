package com.sophi.sample

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.sophi.android.config.SophiLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DEMO_MESSAGE = "Merhaba, bir öneri alabilir miyim?"
const val DEMO_PRODUCT_ID = "12345"

data class SampleEvent(val time: String, val name: String, val detail: String)

object EventLog {
    private val mutableEvents = MutableStateFlow<List<SampleEvent>>(emptyList())
    val events: StateFlow<List<SampleEvent>> = mutableEvents.asStateFlow()

    fun add(name: String, detail: String = "") {
        val time = SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Date())
        mutableEvents.update { listOf(SampleEvent(time, name, detail)) + it }
    }

    fun clear() {
        mutableEvents.value = emptyList()
    }
}

object SampleSettings {
    var preset by mutableStateOf(AppearancePreset.Default)
    var locale by mutableStateOf(SophiLocale.TR)
    var userName by mutableStateOf<String?>(null)
    var persistIdentity by mutableStateOf(true)
    var cartWriteFails by mutableStateOf(false)
    var hostOpensProductPage by mutableStateOf(false)
}

/** The app's own login state. A real app already keeps this; Sophi gets it on every configure. */
object SignedInUser {
    private const val PREFS = "sample_login"
    private const val KEY_ID = "external_user_id"

    fun load(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ID, null)

    fun save(context: Context, externalUserId: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_ID, externalUserId) }
    }
}
