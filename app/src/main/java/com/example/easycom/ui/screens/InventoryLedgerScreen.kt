package com.example.easycom.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycom.model.InventoryBatch
import com.example.easycom.model.ItemBatchLedgerRow
import com.example.easycom.model.StockUnit
import com.example.easycom.ui.EasyComViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryLedgerScreen(viewModel: EasyComViewModel) {
    val context = LocalContext.current
    val ledgerRows by viewModel.ledgerRows.collectAsState()
    val dispatches by viewModel.dispatches.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val items by viewModel.items.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf("ALL") } // ALL, RAW, FG, DISPATCHED

    val filteredRows = ledgerRows.filter { row ->
        val matchesQuery = searchQuery.isBlank() ||
                row.itemName.contains(searchQuery, ignoreCase = true) ||
                row.sku.contains(searchQuery, ignoreCase = true) ||
                row.batchNumber.contains(searchQuery, ignoreCase = true) ||
                row.eanCode.contains(searchQuery, ignoreCase = true)

        val matchesType = when (selectedFilterType) {
            "RAW" -> !row.isFinishedGood
            "FG" -> row.isFinishedGood
            "DISPATCHED" -> row.dispatchedQty > 0
            else -> true
        }
        matchesQuery && matchesType
    }

    // Totals
    val totalPOOrdered = ledgerRows.distinctBy { it.sku }.sumOf { it.poOrderedQty }
    val totalGRNReceived = ledgerRows.distinctBy { it.sku }.sumOf { it.grnReceivedQty }
    val totalKitchenIssued = ledgerRows.sumOf { it.kitchenIssuedQty }
    val totalFGProduced = ledgerRows.distinctBy { it.batchNumber }.sumOf { it.fgProducedQty }
    val totalDispatched = ledgerRows.distinctBy { it.batchNumber }.sumOf { it.dispatchedQty }
    val totalCurrentStock = batches.sumOf { it.quantity }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openExport() },
                icon = { Icon(Icons.Default.Download, contentDescription = null) },
                text = { Text("Export Full Ledger (CSV)") },
                containerColor = Color(0xFF1B5E20),
                contentColor = Color.White,
                modifier = Modifier.testTag("export_ledger_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("inventory_ledger_screen"),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E7D32)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF2E7D32), modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "360° Unified Inventory Ledger",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "Complete lifecycle: PO -> GRN -> Kitchen Issue -> FG Produce -> Dispatch",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.openExport() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("quick_export_button")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF81C784).copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // High Level Movement Pipeline
                        Text(
                            text = "TOTAL MATERIAL MOVEMENT ACROSS FACILITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MovementKPI(label = "PO Ordered", value = "${String.format("%.0f", totalPOOrdered)}", color = Color(0xFF1565C0))
                            MovementKPI(label = "GRN Inward", value = "${String.format("%.0f", totalGRNReceived)}", color = Color(0xFF2E7D32))
                            MovementKPI(label = "Kit. Issued", value = "${String.format("%.0f", totalKitchenIssued)}", color = Color(0xFFE65100))
                            MovementKPI(label = "FG Produced", value = "${String.format("%.0f", totalFGProduced)}", color = Color(0xFF6A1B9A))
                            MovementKPI(label = "Dispatched", value = "${String.format("%.0f", totalDispatched)}", color = Color(0xFFC2185B))
                        }
                    }
                }
            }

            // Search & Filter Row
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Item, SKU, Batch# or EAN Barcode...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_search_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilterType == "ALL",
                                onClick = { selectedFilterType = "ALL" },
                                label = { Text("All Records (${ledgerRows.size})") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == "RAW",
                                onClick = { selectedFilterType = "RAW" },
                                label = { Text("Raw Materials") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == "FG",
                                onClick = { selectedFilterType = "FG" },
                                label = { Text("Finished Goods (FG)") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == "DISPATCHED",
                                onClick = { selectedFilterType = "DISPATCHED" },
                                label = { Text("Dispatched (${dispatches.size})") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Ledger Cards with 360° Movement Pipeline
            items(filteredRows, key = { "${it.sku}_${it.batchNumber}_${it.location}" }) { row ->
                LedgerBatchCard(
                    row = row,
                    onViewBarcode = {
                        val dummy = InventoryBatch(
                            batchNumber = row.batchNumber,
                            itemId = row.sku,
                            itemName = row.itemName,
                            eanCode = row.eanCode,
                            quantity = row.currentStockBalance,
                            unit = row.unit,
                            location = row.location,
                            mfgDate = row.mfgDate,
                            expiryDate = row.expiryDate,
                            poNumber = row.poNumber,
                            grnNumber = row.grnNumber
                        )
                        viewModel.openBarcodeLabel(dummy)
                    },
                    onDispatch = {
                        val matchingBatch = batches.find { it.batchNumber == row.batchNumber }
                        if (matchingBatch != null) {
                            viewModel.openDispatch(matchingBatch)
                        } else {
                            viewModel.showToast("Batch not active in current stock")
                        }
                    }
                )
            }

            if (filteredRows.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FilterListOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No inventory movements found", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovementKPI(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun LedgerBatchCard(
    row: ItemBatchLedgerRow,
    onViewBarcode: () -> Unit,
    onDispatch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ledger_card_${row.batchNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name + Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.itemName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SKU: ${row.sku} | EAN: ${row.eanCode}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    color = if (row.isFinishedGood) Color(0xFF6A1B9A) else Color(0xFF00796B),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (row.isFinishedGood) "FINISHED GOOD" else row.category.displayName.uppercase(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Batch & Expiry Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BATCH: ${row.batchNumber}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Mfg: ${row.mfgDate} | Exp: ${row.expiryDate}",
                    fontSize = 10.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // End-to-end Ledger Pipeline Matrix (Ordered -> Received -> Kitchen -> Produced -> Dispatched -> In-Stock)
            Text("Complete Lifecycle Movement Ledger:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LedgerMetric(label = "PO Ordered", value = "${String.format("%.1f", row.poOrderedQty)}", unit = row.unit.symbol, color = Color(0xFF1565C0))
                LedgerMetric(label = "GRN Recv", value = "${String.format("%.1f", row.grnReceivedQty)}", unit = row.unit.symbol, color = Color(0xFF2E7D32))
                LedgerMetric(label = "Kit. Issued", value = "${String.format("%.1f", row.kitchenIssuedQty)}", unit = row.unit.symbol, color = Color(0xFFE65100))
                LedgerMetric(label = "FG Prod", value = "${String.format("%.1f", row.fgProducedQty)}", unit = row.unit.symbol, color = Color(0xFF6A1B9A))
                LedgerMetric(label = "Dispatched", value = "${String.format("%.1f", row.dispatchedQty)}", unit = row.unit.symbol, color = Color(0xFFC2185B))
                LedgerMetric(label = "In Stock", value = "${String.format("%.1f", row.currentStockBalance)}", unit = row.unit.symbol, color = Color(0xFF1B5E20), isBold = true)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = row.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewBarcode,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Barcode Tag", fontSize = 11.sp)
                    }

                    if (row.isFinishedGood && row.currentStockBalance > 0) {
                        Button(
                            onClick = onDispatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).testTag("dispatch_button_${row.batchNumber}")
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dispatch", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerMetric(label: String, value: String, unit: String, color: Color, isBold: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
        Text(text = unit, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
    }
}
