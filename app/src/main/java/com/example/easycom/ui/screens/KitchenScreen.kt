package com.example.easycom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycom.model.InventoryBatch
import com.example.easycom.model.KitchenIssue
import com.example.easycom.model.ProductionLog
import com.example.easycom.ui.EasyComViewModel
import com.example.easycom.ui.components.BarcodeVisual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(viewModel: EasyComViewModel) {
    val kitchenIssues by viewModel.kitchenIssues.collectAsState()
    val productionLogs by viewModel.productionLogs.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val items by viewModel.items.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = FG Production & EAN, 1 = Material Issued to Kitchen, 2 = Kitchen Stock

    val kitchenBatches = batches.filter { it.inKitchen }
    val fgBatches = batches.filter {
        val itm = items.find { itm -> itm.id == it.itemId }
        itm?.isFinishedGood == true
    }

    Scaffold(
        floatingActionButton = {
            if (activeSubTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openProduction() },
                    icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null) },
                    text = { Text("Produce FG & Generate EAN") },
                    containerColor = Color(0xFF6A1B9A),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("produce_fg_fab")
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openKitchenIssue() },
                    icon = { Icon(Icons.Default.SoupKitchen, contentDescription = null) },
                    text = { Text("Issue Raw Material") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("issue_material_fab")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("kitchen_screen"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFAB47BC)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color(0xFF6A1B9A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kitchen Requisition & FG Production",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track raw material batch issued to chefs, log batch conversion, and automatically generate EAN barcodes for Finished Goods (FG) added to stock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3E2723)
                        )
                    }
                }
            }

            // Sub-Tabs
            item {
                TabRow(
                    selectedTabIndex = activeSubTab,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text("FG Production & EAN (${productionLogs.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text("Store -> Kitchen Issues (${kitchenIssues.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSubTab == 2,
                        onClick = { activeSubTab = 2 },
                        text = { Text("Floor Stock (${kitchenBatches.size})", fontSize = 12.sp) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (activeSubTab) {
                0 -> {
                    // Production Logs with Generated EAN
                    if (productionLogs.isEmpty()) {
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
                                    Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.size(44.dp), tint = Color(0xFF6A1B9A))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Finished Goods produced yet", fontWeight = FontWeight.Bold)
                                    Text("Tap the floating button below to record production and generate an EAN barcode.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    } else {
                        items(productionLogs, key = { it.id }) { log ->
                            ProductionLogCard(
                                log = log,
                                onViewBarcode = {
                                    val dummy = InventoryBatch(
                                        batchNumber = log.fgBatchNumber,
                                        itemId = log.fgItemId,
                                        itemName = log.fgName,
                                        eanCode = log.fgEanCode,
                                        quantity = log.producedQty,
                                        unit = log.unit,
                                        location = "FG Stock: Display / Shelf",
                                        mfgDate = log.date,
                                        expiryDate = log.expiryDate,
                                        poNumber = "KITCHEN-PROD"
                                    )
                                    viewModel.openBarcodeLabel(dummy)
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Kitchen Issues
                    if (kitchenIssues.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No materials issued to kitchen yet", color = Color.Gray)
                            }
                        }
                    } else {
                        items(kitchenIssues, key = { it.id }) { issue ->
                            KitchenIssueCard(issue = issue)
                        }
                    }
                }
                2 -> {
                    // Current Floor stock in Kitchen
                    if (kitchenBatches.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No active raw materials on kitchen floor", color = Color.Gray)
                            }
                        }
                    } else {
                        items(kitchenBatches, key = { it.id }) { batch ->
                            KitchenFloorBatchCard(batch = batch)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionLogCard(
    log: ProductionLog,
    onViewBarcode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("production_card_${log.productionNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8E24AA).copy(alpha = 0.3f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = log.fgName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = "${log.productionNumber} • ${log.date}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    color = Color(0xFF6A1B9A).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+${log.producedQty} ${log.unit.symbol} ADDED TO FG",
                        color = Color(0xFF6A1B9A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barcode preview strip
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E0))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "GENERATED FG EAN-13 BARCODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                        Text(text = log.fgEanCode, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "FG Batch: ${log.fgBatchNumber} | Best Before: ${log.expiryDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onViewBarcode,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Tag", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Consumed Raw Material Batches (Traceability)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Consumed Raw Material Batches:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = log.consumedBatchSummary,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Prepared by Chef: ${log.chefName} @ ${log.kitchenStation}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun KitchenIssueCard(issue: KitchenIssue) {
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
                Text(text = issue.issueNumber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Text(text = issue.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = issue.itemName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(text = "Target Station: ${issue.kitchenStation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Source Batch: ${issue.sourceBatchNumber}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${issue.quantity} ${issue.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1565C0))
                    Text(text = "Issued by: ${issue.requestedBy}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun KitchenFloorBatchCard(batch: InventoryBatch) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = batch.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Batch: ${batch.batchNumber} | Loc: ${batch.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text(text = "Exp: ${batch.expiryDate}", fontSize = 11.sp, color = Color(0xFFC62828))
            }
            Text(
                text = "${String.format("%.1f", batch.quantity)} ${batch.unit.symbol}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
