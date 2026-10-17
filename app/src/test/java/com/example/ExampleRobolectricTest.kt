package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.easycom.data.EasyComRepository
import com.example.easycom.model.POStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EasyCom ERP", appName)
  }

  @Test
  fun `test ean13 checksum generation`() {
    val repo = EasyComRepository()
    val ean = repo.generateEan13("890123456789")
    assertEquals(13, ean.length)
    assertTrue(ean.startsWith("890123456789"))
  }

  @Test
  fun `test po approval workflow`() {
    val repo = EasyComRepository()
    val poList = repo.purchaseOrders.value
    val pendingPO = poList.find { it.status == POStatus.PENDING_APPROVAL }
    assertTrue(pendingPO != null)

    repo.approvePurchaseOrder(pendingPO!!.id, "Test Approver", "Budget cleared")
    val updatedPO = repo.purchaseOrders.value.find { it.id == pendingPO.id }
    assertEquals(POStatus.APPROVED, updatedPO?.status)
    assertEquals("Test Approver", updatedPO?.approvedBy)
  }

  @Test
  fun `test 360 degree inventory ledger and csv export`() {
    val repo = EasyComRepository()
    val ledger = repo.generateFullLedger()
    assertTrue("Ledger should have records", ledger.isNotEmpty())

    val csv = repo.exportLedgerToCSV()
    assertTrue(csv.contains("SKU,Item Name,EAN-13 Barcode"))
    assertTrue(csv.contains("BAT-AMUL-2601"))
  }
}
