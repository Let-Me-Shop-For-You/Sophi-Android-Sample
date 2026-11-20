package com.sophi.sample

import com.sophi.android.data.model.SophiCartItem
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MerchantCartTest {
    @Before fun prepare() { MerchantCart.onSnapshot = null; MerchantCart.onAccepted = null; MerchantCart.onRemoved = null; MerchantCart.clear(); MerchantCart.failNextWrite.value = false }
    @After fun clear() { MerchantCart.onAccepted = null; MerchantCart.onRemoved = null; MerchantCart.clear() }
    @Test fun observersSeeOnlyAcceptedWritesAndCannotRejectThem() = runBlocking {
        val item = SophiCartItem(1, "P1", variantExternalId = "SKU", quantity = 2)
        var accepted = 0; var removed = 0
        MerchantCart.onAccepted = { _, snapshot -> accepted++; assertEquals(2, snapshot.single().quantity); error("Delivery unavailable") }
        MerchantCart.onRemoved = { _, snapshot -> removed++; assertTrue(snapshot.isEmpty()) }
        MerchantCart.failNextWrite.value = true
        assertTrue(MerchantCart.add(listOf(item)).isFailure); assertEquals(0, accepted)
        assertTrue(MerchantCart.add(listOf(item)).isSuccess); assertEquals(1, accepted)
        MerchantCart.remove("P1/SKU"); assertEquals(1, removed)
        MerchantCart.remove("P1/SKU"); assertEquals(1, removed)
    }
    @Test fun shopperBoundaryCancelsDelayedMerchantWrite() = runBlocking {
        var accepted = 0; MerchantCart.onAccepted = { _, _ -> accepted++ }
        val write = async { MerchantCart.add(listOf(SophiCartItem(1, "P1", variantExternalId = "SKU", quantity = 1))) }
        delay(50); MerchantCart.clear()
        assertTrue(write.await().isFailure); assertTrue(MerchantCart.lines.value.isEmpty()); assertEquals(0, accepted)
    }
    @Test fun rejectsInvalidBatchWithoutPartialMutation() = runBlocking {
        val good = SophiCartItem(1, "P1", variantExternalId = "SKU", quantity = 2)
        for (bad in listOf(good.copy(externalId = ""), good.copy(variantExternalId = null), good.copy(quantity = 0))) {
            assertTrue(MerchantCart.add(listOf(good, bad)).isFailure)
            assertTrue(MerchantCart.lines.value.isEmpty())
            assertTrue(MerchantCart.snapshot().items.isEmpty())
        }
    }
    @Test fun mergesQuantitiesAndRejectsOverflowAtomically() = runBlocking {
        val item = SophiCartItem(1, "P1", variantExternalId = "SKU", quantity = Int.MAX_VALUE)
        assertTrue(MerchantCart.add(listOf(item)).isSuccess)
        assertTrue(MerchantCart.add(listOf(item.copy(externalId = "P2", quantity = 1), item.copy(quantity = 1))).isFailure)
        assertEquals(1, MerchantCart.lines.value.size)
        assertEquals(Int.MAX_VALUE, MerchantCart.snapshot().items.single().quantity)
    }
    @Test fun snapshotIncludesEveryAcceptedMerchantLine() = runBlocking {
        val items = listOf(SophiCartItem(1, "P1", variantExternalId = "S1", quantity = 2), SophiCartItem(2, "P2", variantExternalId = "S2", quantity = 3))
        assertTrue(MerchantCart.add(items).isSuccess)
        assertEquals(2, MerchantCart.snapshot().items.size)
        assertEquals(listOf(2, 3), MerchantCart.snapshot().items.map { it.quantity })
    }
}
