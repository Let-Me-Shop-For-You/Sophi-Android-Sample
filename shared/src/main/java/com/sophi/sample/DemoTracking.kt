package com.sophi.sample

import android.content.Context
import com.sophi.android.Sophi
import com.sophi.android.data.model.SophiCartItem
import com.sophi.event.*
import com.sophi.event.external.*
import com.sophi.event.external.model.*
import java.net.URL
import java.util.UUID
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.*
import org.json.JSONObject

data class TrackingState(
    val ready: Boolean = false, val enabled: Boolean = false, val local: Boolean = false,
    val pending: Int = 0, val received: Int = 0, val authCount: Int = 0, val outage: Boolean = false,
    val status: String = "Configure chat + events to start.", val log: List<String> = emptyList(),
)

/** Shared by the Compose and Views hosts. The native SDK is the only queue/auth owner. */
object DemoTracking {
    const val ORIGIN = "https://127.0.0.1:19443"
    val state = MutableStateFlow(TrackingState())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val actions = Channel<Pair<Long?, suspend () -> Unit>>(256)
    private var generation = 0L
    private var boundary = 0L
    private var app: Context? = null
    private var consentKey = ""
    private var fixtureKey = ""
    var shopperChanged: ((String?) -> Unit)? = null

    init {
        scope.launch {
            for ((captured, action) in actions) {
                if (captured != null && captured != generation) continue
                try { action() }
                catch (cancelled: CancellationException) { if (!isActive) throw cancelled }
                catch (_: Exception) { log("Event rejected; check values, storage or connectivity") }
                if (captured == generation) refresh()
            }
        }
    }

    fun invalidate() { generation++; boundary++; state.update { it.copy(ready = false) } }

    suspend fun configure(context: Context, key: String, origin: String, tenant: String, shopper: String?) {
        app = context.applicationContext; generation++
        val current = generation
        consentKey = "analytics.$tenant.$origin"
        fixtureKey = if (origin == ORIGIN) key else ""
        val enabled = preferences().getBoolean(consentKey, false)
        preferences().edit().putString("tracking.shopper", shopper).apply()
        state.value = TrackingState(enabled = enabled, local = origin == ORIGIN, status = "Configuring…")
        SophiEvent.configure(context, EventConfiguration(key, origin, enabled, externalUserId = shopper), EventDiagnostics { diagnostic ->
            scope.launch { if (current == generation) log("${diagnostic.code} · ${diagnostic.count}") }
        })
        if (current != generation) return
        MerchantCart.onAccepted = { items, snapshot ->
            if (current == generation) added(items, snapshot)
        }
        MerchantCart.onRemoved = { item, snapshot ->
            if (current == generation) removed(item, snapshot)
        }
        state.update { it.copy(ready = true, status = if (enabled) "Collection enabled" else "Collection disabled — chat still works") }
        refresh()
    }

    suspend fun permission(allowed: Boolean) {
        if (!state.value.ready) return
        val current = generation; val key = consentKey; val prefs = preferences()
        if (!allowed) {
            boundary++
            // Revocation must not restore an older opt-in after a process crash.
            check(withContext(Dispatchers.IO) { prefs.edit().putBoolean(key, false).commit() })
            if (current != generation) return
        }
        SophiEvent.setAnalyticsEnabled(allowed)
        if (current != generation) return
        check(withContext(Dispatchers.IO) { prefs.edit().putBoolean(key, allowed).commit() })
        if (current != generation) return
        state.update { it.copy(enabled = allowed) }
        log(if (allowed) "Collection enabled" else "Collection disabled; pending events cleared")
        refresh()
    }

    fun record(event: SophiEventData) {
        if (!state.value.ready) { log("Event not recorded: configuration unavailable"); return }
        if (!state.value.enabled) { log("${event.type.name.lowercase()} · disabled"); return }
        val current = generation; val ownerBoundary = boundary
        val accepted = actions.trySend(current to {
            if (ownerBoundary == boundary) {
                val receipt = SophiEvent.track(event)
                if (current == generation && ownerBoundary == boundary) log("${event.type.name.lowercase()} · ${receipt.status.name.lowercase()}${receipt.id?.let { " · " + it.take(8) } ?: ""}")
            }
        })
        if (accepted.isFailure) log("Demo action buffer full; event was not submitted")
    }

    suspend fun flush() {
        val current = generation
        val barrier = CompletableDeferred<Unit>()
        actions.send(null to { barrier.complete(Unit) })
        barrier.await()
        if (current != generation) return
        val result = SophiEvent.flush()
        if (current != generation) return
        log("Flush: ${result.delivered} delivered, ${result.pending} pending${result.pausedReason?.let { " · $it" } ?: ""}")
        refresh()
    }

    suspend fun refresh() {
        if (!state.value.ready) return
        val current = generation
        try {
            val pending = SophiEvent.pendingCount()
            if (current != generation) return
            state.update { it.copy(pending = pending) }
            if (!state.value.local) return
            val server = fixture("/demo/state?key=$fixtureKey")
            if (current != generation) return
            state.update { it.copy(received = server.getInt("acceptedCount"), authCount = server.getInt("authCount"), outage = server.getString("mode") == "fail") }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { if (current == generation) state.update { it.copy(status = "Local server unavailable. Run the demo launcher.") } }
    }

    suspend fun setOutage(fail: Boolean) {
        check(state.value.local)
        fixture("/demo/mode", JSONObject().put("key", fixtureKey).put("mode", if (fail) "fail" else "accept"))
        log(if (fail) "Event delivery outage simulated; chat stays available" else "Server restored; scheduled retry delays still apply")
        refresh()
    }

    suspend fun shopper(id: String?) {
        if (!state.value.ready) return
        val current = generation
        // Drop only host actions which have not yet been submitted; the SDK handles queued ownership.
        boundary++
        val wasRegistered = preferences().getString("tracking.shopper", null)
        if (id == null || (wasRegistered != null && wasRegistered != id)) MerchantCart.clear()
        if (id == null) SophiEvent.logout() else SophiEvent.updateUser(id)
        if (current != generation) return
        preferences().edit().putString("tracking.shopper", id).apply()
        shopperChanged?.invoke(id)
        MerchantCart.push()
        log(if (id == null) "Shopper signed out; pending events cleared" else "Shared shopper updated")
        refresh()
    }

    fun log(text: String) {
        state.update { it.copy(status = text, log = (listOf(text) + it.log).take(40)) }
        DemoEventLog.log(text)
    }

    private fun preferences() = requireNotNull(app).getSharedPreferences("sophi_demo_events", Context.MODE_PRIVATE)

    private suspend fun fixture(path: String, body: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(ORIGIN + path).openConnection() as HttpsURLConnection
        connection.connectTimeout = 4000; connection.readTimeout = 4000
        try {
            if (body != null) {
                connection.requestMethod = "POST"; connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            check(connection.responseCode == 200)
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally { connection.disconnect() }
    }

    fun sampleItem() = SophiCartItem(1, "P-1", 11, "V-1-S", "S", 1, 39.90, "GBP")
    private fun product(line: MerchantLine): ProductObject {
        val price = requireNotNull(line.unitPrice) { "Missing merchant price" }
        val currency = requireNotNull(line.currency) { "Missing merchant currency" }
        return ProductObject(line.productId, emptyList(), line.name, "demo://product/${line.productId}",
            "$ORIGIN/image.jpg", emptyList(), currency, price, price, 1, variantId = line.variantId, size = line.size)
    }
    private fun line(item: SophiCartItem) = MerchantLine(item.externalId, item.variantExternalId.orEmpty(),
        item.product?.displayName ?: item.externalId, item.size, item.quantity, item.unitPrice, item.currency)
    private fun basket(lines: List<MerchantLine>, emptyCurrency: String = "GBP"): BasketObject {
        val currency = lines.firstOrNull()?.currency ?: emptyCurrency
        val items = lines.map {
            val p = product(it); require(p.currency == currency)
            LineItem(p.id, it.quantity, currency, p.unitPrice, p.unitSalePrice, p.unitSalePrice * it.quantity, variantId = it.variantId)
        }
        return BasketObject(currency, items.sumOf { it.lineTotal }, items)
    }
    private fun added(items: List<SophiCartItem>, snapshot: List<MerchantLine>) {
        try {
            val basket = basket(snapshot)
            // One action per product line. The basket carries the resulting quantities.
            for (item in items) {
                record(SophiEventData(ExternalEventType.ADD_TO_CART, PageObject("Product", "demo://product/${item.externalId}"), product = product(line(item)), basket = basket))
            }
        } catch (_: IllegalArgumentException) { log("Cart accepted; analytics skipped because price/currency metadata is incomplete") }
    }
    private fun removed(item: MerchantLine, snapshot: List<MerchantLine>) {
        try { record(SophiEventData(ExternalEventType.REMOVE_FROM_CART, PageObject("Basket", "demo://basket"), product = product(item), basket = basket(snapshot, item.currency ?: "GBP"))) }
        catch (_: IllegalArgumentException) { log("Cart updated; analytics metadata incomplete") }
    }
    fun checkout() {
        if (MerchantCart.lines.value.isEmpty()) { log("Your demo cart is empty"); return }
        cartEvent(ExternalEventType.CHECKOUT_STARTED)
    }
    fun cartEvent(type: ExternalEventType) {
        try { record(SophiEventData(type, PageObject(if (type == ExternalEventType.CHECKOUT_STARTED) "Checkout" else "Basket", "demo://basket"), basket = basket(MerchantCart.lines.value))) }
        catch (_: IllegalArgumentException) { log("Cart analytics metadata incomplete") }
    }
    fun placeDemoOrder(): Boolean {
        val snapshot = MerchantCart.lines.value
        if (snapshot.isEmpty()) return false
        val id = "demo-" + UUID.randomUUID()
        MerchantCart.clear() // The simulated order is confirmed once; analytics cannot submit the order again.
        try {
            val basket = basket(snapshot)
            record(SophiEventData(ExternalEventType.PURCHASE, PageObject("Confirmation", "demo://confirmation"),
                transaction = TransactionObject(id, basket.currency, basket.total, basket.lineItems)))
        } catch (_: IllegalArgumentException) { log("Demo order completed; analytics metadata incomplete") }
        return true
    }
    fun sampleEvent(type: ExternalEventType) {
        check(state.value.local)
        val page = PageObject("Home", "demo://home")
        record(when (type) {
            ExternalEventType.PAGE_VIEW -> SophiEventData(type, page)
            ExternalEventType.PRODUCT_VIEW -> SophiEventData(type, PageObject("Product", "demo://product/P-1"), product = product(line(sampleItem())))
            ExternalEventType.LISTING_VIEW -> SophiEventData(type, PageObject("Category", "demo://shirts"), listing = ListingObject(categoryId = "shirts", name = "Shirts"))
            ExternalEventType.IDENTIFY -> SophiEventData(type, PageObject("Account", "demo://account"), user = UserObject(ConsentObject(false, "demo", System.currentTimeMillis() / 1000)))
            ExternalEventType.CUSTOM -> SophiEventData(type, PageObject("Custom", "demo://custom"), custom = buildJsonObject { put("action", "demo_button") })
            else -> error("Use the merchant cart for cart/checkout/purchase")
        })
    }
}
