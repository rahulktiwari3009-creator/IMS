package com.example.easycom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycom.model.GRN
import com.example.easycom.model.InventoryBatch
import com.example.easycom.model.POStatus
import com.example.easycom.model.PurchaseOrder
import com.example.easycom.ui.EasyComViewModel
import com.example.easycom.ui.components.BarcodeVisual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreGRNScreen(viewModel: EasyComViewModel) {
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val grnList by viewModel.grnList.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val items by viewModel.items.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Awaiting GRN, 1 = GRN History, 2 = Store Batches

    val approvedPOsAwaitingGRN = purchaseOrders.filter {
        it.status == POStatus.APPROVED || it.status == POStatus.PARTIALLY_RECEIVED
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("store_grn_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header with Scanner CTA
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EAN Barcode Inward Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan vendor EAN barcodes to verify goods against PO & inward stock directly into bins.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.openBarcodeScanner() },
                        modifier = Modifier.testTag("open_scanner_button")
                    ) {
                        Icon(Icons.Default.CenterFocusWeak, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan EAN", fontSize = 12.sp)
                    }
                }
            }
        }

        // Sub Tabs: Awaiting GRN, History, Current Batches
        item {
            TabRow(
                selectedTabIndex = activeSubTab,
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Awaiting Inward (${approvedPOsAwaitingGRN.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("GRN History (${grnList.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Active Batches (${batches.filter { !it.inKitchen }.size})", fontSize = 12.sp) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (activeSubTab) {
            0 -> {
                // Awaiting Inward POs
                if (approvedPOsAwaitingGRN.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No pending POs waiting for GRN", fontWeight = FontWeight.Bold)
                                Text("All approved purchase orders have been received into store.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                } else {
                    items(approvedPOsAwaitingGRN, key = { it.id }) { po ->
                        ApprovedPOInwardCard(
                            po = po,
                            onTakeGRN = { viewModel.openCreateGRN(po) },
                            onScanBarcode = { viewModel.openBarcodeScanner() }
                        )
                    }
                }
            }
            1 -> {
                // GRN History
                if (grnList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No GRN recorded yet", color = Color.Gray)
                        }
                    }
                } else {
                    items(grnList, key = { it.id }) { grn ->
                        GRNHistoryCard(
                            grn = grn,
                            onPrintBatchLabels = {
                                val item = grn.items.firstOrNull()
                                if (item != null) {
                                    val dummyBatch = InventoryBatch(
                                        batchNumber = item.batchNumber,
                                        itemId = item.itemId,
                                        itemName = item.itemName,
                                        eanCode = item.eanCode,
                                        quantity = item.receivedQty,
                                        unit = item.unit,
                                        location = "Store: ${item.putAwayLocation}",
                                        mfgDate = item.mfgDate,
                                        expiryDate = item.expiryDate,
                                        grnNumber = grn.grnNumber,
                                        poNumber = grn.poNumber
                                    )
                                    viewModel.openBarcodeLabel(dummyBatch)
                                }
                            }
                        )
                    }
                }
            }
            2 -> {
                // Active Store Batches with Barcode Tags
                val storeBatches = batches.filter { !it.inKitchen }
                items(storeBatches, key = { it.id }) { batch ->
                    StoreBatchCard(
                        batch = batch,
                        onViewTag = { viewModel.openBarcodeLabel(batch) },
                        onIssueToKitchen = { viewModel.openKitchenIssue() }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovedPOInwardCard(
    po: PurchaseOrder,
    onTakeGRN: () -> Unit,
    onScanBarcode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("awaiting_grn_${po.poNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E7D32).copy(alpha = 0.4f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = po.poNumber,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = po.vendorName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "READY FOR GRN",
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Approved by: ${po.approvedBy ?: "Authorized Signatory"} on ${po.approvalDate ?: "Today"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Items breakdown
            po.items.forEach { line ->
                val remaining = line.orderedQty - line.receivedQty
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = line.itemName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = "EAN: ${line.eanCode} | SKU: ${line.sku}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.outline)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Order: ${line.orderedQty} ${line.unit.symbol}", fontSize = 11.sp)
                        Text(
                            text = "Remaining: $remaining ${line.unit.symbol}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onScanBarcode,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify EAN", fontSize = 12.sp)
                }

                Button(
                    onClick = onTakeGRN,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Take GRN Now", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GRNHistoryCard(grn: GRN, onPrintBatchLabels: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = grn.grnNumber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }
                Text(text = grn.receivedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Vendor: ${grn.vendorName} | Against PO: ${grn.poNumber}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "Received by: ${grn.receivedBy} | Invoice: ${grn.invoiceNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            grn.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = item.itemName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Batch: ${item.batchNumber} | Location: ${item.putAwayLocation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Text(text = "${item.receivedQty} ${item.unit.symbol}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onPrintBatchLabels,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print EAN Barcode Labels", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StoreBatchCard(
    batch: InventoryBatch,
    onViewTag: () -> Unit,
    onIssueToKitchen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = batch.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Batch: ${batch.batchNumber} | EAN: ${batch.eanCode}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = batch.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Exp: ${batch.expiryDate}", fontSize = 11.sp, color = Color(0xFFC62828))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.1f", batch.quantity)} ${batch.unit.symbol}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onViewTag, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.QrCode2, contentDescription = "View Barcode", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
