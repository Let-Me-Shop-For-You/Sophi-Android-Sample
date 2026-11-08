package com.sophi.sample

import com.sophi.android.data.model.SophiCartItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MerchantCartTest {
    @Before fun prepare() { MerchantCart.onSnapshot = null; MerchantCart.clear(); MerchantCart.failNextWrite.value = false }
    @After fun clear() { MerchantCart.clear() }
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
