package com.sophi.sample

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.sophi.event.external.ExternalEventType
import kotlinx.coroutines.*

/** One Events screen shared by both demo UIs; all SDK operations remain in DemoTracking. */
class DemoEventsActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var poll: Job? = null
    private var rendering = false
    private var busy = false
    private val controls = mutableListOf<android.view.View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val padding = (16 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(padding, padding, padding, padding) }
        val scroll = ScrollView(this).apply { addView(content); setBackgroundColor(0xfff7f9f7.toInt()) }
        setContentView(scroll)
        WindowCompat.getInsetsController(window, scroll).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        fun text(value: String, size: Float = 16f) = TextView(this).apply { text = value; textSize = size; setPadding(0, 12, 0, 12); content.addView(this) }
        fun button(label: String, block: suspend () -> Unit) = Button(this).apply {
            text = label; isAllCaps = false; contentDescription = label; content.addView(this)
            controls.add(this)
            setOnClickListener { perform(block) }
        }
        text("Events & delivery", 24f)
        text("Chat and events share one native shopper. Collection requires explicit permission.")
        val reject = Switch(this).apply { text = "Reject next cart write"; contentDescription = text; content.addView(this) }
        reject.setOnCheckedChangeListener { _, checked -> if (!rendering) MerchantCart.failNextWrite.value = checked }
        scope.launch { MerchantCart.failNextWrite.collect { value -> rendering = true; reject.isChecked = value; rendering = false } }
        val permission = Switch(this).apply { text = "Analytics enabled"; contentDescription = "Analytics enabled"; content.addView(this) }
        val counts = text("Pending on device: 0")
        val status = text("")
        val outage = Switch(this).apply { text = "Simulate event delivery outage"; contentDescription = text; content.addView(this) }
        controls.add(permission); controls.add(outage)
        permission.setOnCheckedChangeListener { _, checked -> if (!rendering) perform { DemoTracking.permission(checked) } }
        outage.setOnCheckedChangeListener { _, checked -> if (!rendering) perform { DemoTracking.setOutage(checked) } }
        button("Flush due events") { DemoTracking.flush() }
        if (DemoTracking.state.value.local) {
            text("Local sample catalogue", 20f)
            text("Synthetic products and orders. These actions use the same merchant cart as chat.")
            button("View home page") { DemoTracking.sampleEvent(ExternalEventType.PAGE_VIEW) }
            button("View shirt listing") { DemoTracking.sampleEvent(ExternalEventType.LISTING_VIEW) }
            button("View cotton shirt") { DemoTracking.sampleEvent(ExternalEventType.PRODUCT_VIEW) }
            button("Add demo shirt to cart") { MerchantCart.add(listOf(DemoTracking.sampleItem())).getOrThrow() }
            button("View basket") { DemoTracking.cartEvent(ExternalEventType.BASKET_VIEW) }
            button("Remove last cart line") { MerchantCart.lines.value.lastOrNull()?.let { MerchantCart.remove(it.key) } }
            button("Start demo checkout") { DemoTracking.checkout() }
            button("Complete demo order") { if (!DemoTracking.placeDemoOrder()) DemoTracking.log("Your demo cart is empty") }
            button("Send anonymous identify") { DemoTracking.sampleEvent(ExternalEventType.IDENTIFY) }
            button("Send custom event") { DemoTracking.sampleEvent(ExternalEventType.CUSTOM) }
        }
        text("Shared shopper", 20f)
        val shopper = EditText(this).apply { hint = "Demo shopper ID"; setText("demo-customer"); content.addView(this) }
        button("Sign in shopper") { val id = shopper.text.toString().trim(); require(id.isNotEmpty()); DemoTracking.shopper(id) }
        button("Sign out shopper") { DemoTracking.shopper(null) }
        text("Recent event results", 20f)
        val log = text("", 13f)
        button("Back to chat demo") { finish() }
        scope.launch {
            DemoTracking.state.collect { value ->
                rendering = true
                controls.forEach { it.isEnabled = value.ready && !busy }
                permission.isChecked = value.enabled; permission.isEnabled = value.ready && !busy
                outage.isChecked = value.outage; outage.visibility = if (value.local) android.view.View.VISIBLE else android.view.View.GONE
                outage.isEnabled = value.ready && !busy
                counts.text = "Pending on device: ${value.pending}" + if (value.local) "\nReceived by local server: ${value.received} · Auth sessions: ${value.authCount}" else ""
                status.text = value.status; log.text = value.log.joinToString("\n")
                rendering = false
            }
        }
    }
    private fun perform(block: suspend () -> Unit) {
        if (busy || !DemoTracking.state.value.ready) return
        busy = true
        controls.forEach { it.isEnabled = false }
        scope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { DemoTracking.log("Action failed; check cart, configuration or connectivity") }
            finally {
                busy = false
                controls.forEach { it.isEnabled = DemoTracking.state.value.ready }
                DemoTracking.refresh()
            }
        }
    }
    override fun onResume() { super.onResume(); poll = scope.launch { while (isActive) { DemoTracking.refresh(); delay(2000) } } }
    override fun onPause() { poll?.cancel(); super.onPause() }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
