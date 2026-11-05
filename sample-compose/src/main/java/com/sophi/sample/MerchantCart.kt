package com.sophi.sample

import com.sophi.android.Sophi
import com.sophi.android.data.model.SophiCart
import com.sophi.android.data.model.SophiCartItem
import com.sophi.android.data.model.SophiCartLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

/** One merchant cart line, keyed by the merchant's product + variant ids (variant may be unknown). */
data class MerchantLine(
    val productId: String,
    val variantId: String,
    val name: String,
    val size: String?,
    val quantity: Int,
    val unitPrice: Double?,
    val currency: String?,
) {
    val key: String get() = "$productId/$variantId"
    val label: String get() = "$name${size?.let { " · $it" } ?: ""} ×$quantity"
    val lineTotal: String get() = unitPrice?.let { formatAmount(it * quantity, currency) } ?: "-"
}

/**
 * The merchant's own cart: purely in-memory demo state (no Saleor, no payment). Every SDK write
 * is acknowledged only after it lands here, and every mutation pushes a fresh snapshot to the SDK
 * (the SDK drops its copy on reset / account switch, so the merchant re-pushes after those too).
 */
object MerchantCart {
    private val state = MutableStateFlow<List<MerchantLine>>(emptyList())
    val lines: StateFlow<List<MerchantLine>> = state

    /** Armed by the operator to rehearse a rejected write; consumed by the next add. */
    val failNextWrite = MutableStateFlow(false)

    val total: Double get() = state.value.sumOf { (it.unitPrice ?: 0.0) * it.quantity }
    val currency: String? get() = state.value.firstNotNullOfOrNull { it.currency }

    /** Simulated merchant write with latency. A missing variant id is accepted (the SDK can deliver none). */
    suspend fun add(items: List<SophiCartItem>): Result<Unit> {
        delay(WRITE_LATENCY_MILLIS)
        if (failNextWrite.compareAndSet(expect = true, update = false)) {
            return Result.failure(IllegalStateException("Demo merchant rejected the cart write"))
        }
        state.update { current ->
            val merged = current.associateBy { it.key }.toMutableMap()
            items.forEach { item ->
                val line = MerchantLine(
                    productId = item.externalId,
                    variantId = item.variantExternalId.orEmpty(),
                    name = item.product?.displayName ?: item.externalId,
                    size = item.size,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    currency = item.currency,
                )
                merged[line.key] = merged[line.key]?.let { it.copy(quantity = it.quantity + line.quantity) } ?: line
            }
            merged.values.toList()
        }
        push()
        return Result.success(Unit)
    }

    fun remove(key: String) {
        state.update { current -> current.filterNot { it.key == key } }
        push()
    }

    fun clear() {
        state.update { emptyList() }
        push()
    }

    /** Hands the whole cart to the SDK for `meta.cartInfo`; lines without a merchant variant id are not sendable. */
    fun push() {
        if (!DemoSophi.configured.value) return
        Sophi.updateCart(snapshot())
    }

    fun snapshot(): SophiCart = SophiCart(
        cartId = CART_ID,
        items = state.value
            .filter { it.variantId.isNotBlank() }
            .map { SophiCartLine(productId = it.productId, variantId = it.variantId, quantity = it.quantity) },
    )

    private const val WRITE_LATENCY_MILLIS = 400L
    private const val CART_ID = "demo-cart"
}

/** `₺1.234,56` style amount for the demo screens (the SDK's own formatter is internal). */
fun formatAmount(amount: Double, currency: String?): String {
    val code = currency?.uppercase()?.takeIf { it.isNotBlank() } ?: "TRY"
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))
    runCatching { format.currency = Currency.getInstance(code) }
    return format.format(amount)
}

/** Timestamped log of every SDK callback and host action, newest first. */
object DemoEventLog {
    private val state = MutableStateFlow<List<String>>(emptyList())
    val events: StateFlow<List<String>> = state

    fun log(message: String) {
        // Formatted once, outside update(): SimpleDateFormat is not thread-safe and update() may retry.
        val stamped = "${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())}  $message"
        state.update { (listOf(stamped) + it).take(MAX_EVENTS) }
    }

    private const val MAX_EVENTS = 100
}
