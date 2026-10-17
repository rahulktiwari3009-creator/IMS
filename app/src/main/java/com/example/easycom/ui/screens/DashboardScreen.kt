package com.example.easycom.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.easycom.model.ItemCategory
import com.example.easycom.model.POStatus
import com.example.easycom.model.UserRole
import com.example.easycom.ui.EasyComViewModel
import com.example.easycom.ui.ItemStockSummary
import com.example.easycom.ui.MainTab
import com.example.easycom.ui.components.BarcodeVisual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: EasyComViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val stockSummaries by viewModel.stockSummaries.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val emailAlerts by viewModel.emailAlerts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()

    var showRoleMenu by remember { mutableStateOf(false) }

    val pendingApprovalCount = purchaseOrders.count { it.status == POStatus.PENDING_APPROVAL }
    val lowStockCount = stockSummaries.count { it.isLowStock || it.isOutOfStock }
    val totalStockValue = stockSummaries.sumOf { it.totalStock * it.item.costPrice }
    val rawMaterialCount = stockSummaries.count { !it.item.isFinishedGood }
    val fgCount = stockSummaries.count { it.item.isFinishedGood }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Role Switcher Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EasyCom Supply Chain ERP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Current Role: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                color = Color(currentRole.badgeColor),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentRole.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = { viewModel.openSettings() },
                            modifier = Modifier.testTag("dashboard_edit_set_button"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Edit Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box {
                            FilledTonalButton(
                                onClick = { showRoleMenu = true },
                                modifier = Modifier.testTag("switch_role_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = "Switch Role", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Switch Role", fontSize = 12.sp)
                            }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            Text(
                                "Switch ERP Persona (Demo)",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Divider()
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(role.title, fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setRole(role)
                                        showRoleMenu = false
                                    },
                                    leadingIcon = {
                                        if (role == currentRole) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        } else {
                                            Box(modifier = Modifier.size(12.dp).background(Color(role.badgeColor), CircleShape))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // Live Auto-Alert Banner (if low stock detected)
        if (lowStockCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("low_stock_alert_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFA000)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF57C00),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Low-Stock Alert Active!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$lowStockCount item(s) below reorder safety threshold. Auto-notification emails generated.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.openEmailAlerts() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("view_emails_button")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Emails", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // KPI Metric Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Real-Time Warehouse Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Valuation",
                        value = "₹${String.format("%,.0f", totalStockValue)}",
                        subtitle = "Store & Kitchen",
                        color = Color(0xFF1565C0),
                        icon = Icons.Default.CurrencyRupee,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Pending Approvals",
                        value = "$pendingApprovalCount POs",
                        subtitle = if (pendingApprovalCount > 0) "Needs Action" else "All Clear",
                        color = if (pendingApprovalCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32),
                        icon = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(MainTab.PURCHASE_ORDERS) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Raw Materials",
                        value = "$rawMaterialCount SKUs",
                        subtitle = "Dairy, Flour, Spices",
                        color = Color(0xFF00796B),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Finished Goods",
                        value = "$fgCount FG SKUs",
                        subtitle = "EAN Coded & Packaged",
                        color = Color(0xFF6A1B9A),
                        icon = Icons.Default.Fastfood,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(MainTab.KITCHEN) }
                    )
                }
            }
        }

        // Quick Action Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    QuickActionButton(
                        icon = Icons.Default.AddShoppingCart,
                        label = "Create PO",
                        onClick = { viewModel.openCreatePODialog() }
                    )
                    QuickActionButton(
                        icon = Icons.Default.QrCodeScanner,
                        label = "Scan EAN",
                        onClick = { viewModel.openBarcodeScanner() }
                    )
                    QuickActionButton(
                        icon = Icons.Default.SoupKitchen,
                        label = "Issue Kitchen",
                        onClick = { viewModel.openKitchenIssue() }
                    )
                    QuickActionButton(
                        icon = Icons.Default.AutoFixHigh,
                        label = "Produce FG",
                        onClick = { viewModel.openProduction() }
                    )
                    QuickActionButton(
                        icon = Icons.Default.TableChart,
                        label = "360° Ledger",
                        onClick = { viewModel.setTab(MainTab.LEDGER) }
                    )
                }
            }
        }

        // 360° Comprehensive Movement Ledger & Export Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.setTab(MainTab.LEDGER) }
                    .testTag("open_ledger_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E7D32)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFF2E7D32), modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "360° Stock Movement Ledger",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = Color(0xFF1B5E20), shape = RoundedCornerShape(4.dp)) {
                                    Text("EXPORT CSV", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text(
                                text = "PO Ordered -> GRN Recv -> Kitchen Issue -> FG Produce -> Dispatched (Batch/Mfg/Exp)",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF2E7D32))
                }
            }
        }

        // Live Inventory Stock Table Header & Filters
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Live Inventory & EAN Stock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stockSummaries.size} Items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by Item Name, SKU or EAN barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All SKUs") }
                        )
                    }
                    items(ItemCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                viewModel.setCategoryFilter(if (selectedCategory == cat) null else cat)
                            },
                            label = { Text(cat.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Stock Summary Cards
        items(stockSummaries, key = { it.item.id }) { summary ->
            StockItemCard(
                summary = summary,
                onViewBarcode = {
                    val matchingBatch = batches.find { it.itemId == summary.item.id }
                        ?: com.example.easycom.model.InventoryBatch(
                            batchNumber = "LIVE-STOCK-${summary.item.sku}",
                            itemId = summary.item.id,
                            itemName = summary.item.name,
                            eanCode = summary.item.eanCode,
                            quantity = summary.totalStock,
                            unit = summary.item.unit,
                            location = summary.item.rackLocation,
                            mfgDate = "Current Stock",
                            expiryDate = "Batch Traceable"
                        )
                    viewModel.openBarcodeLabel(matchingBatch)
                },
                onQuickPO = {
                    viewModel.openCreatePODialog()
                }
            )
        }

        if (stockSummaries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No SKUs match your filter", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.3f)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StockItemCard(
    summary: ItemStockSummary,
    onViewBarcode: () -> Unit,
    onQuickPO: () -> Unit
) {
    val item = summary.item
    val statusColor = when {
        summary.isOutOfStock -> Color(0xFFC62828)
        summary.isLowStock -> Color(0xFFE65100)
        else -> Color(0xFF2E7D32)
    }
    val statusText = when {
        summary.isOutOfStock -> "OUT OF STOCK"
        summary.isLowStock -> "LOW STOCK"
        else -> "HEALTHY"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("stock_item_${item.sku}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isFinishedGood) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF6A1B9A),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "FG",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SKU: ${item.sku} | EAN: ${item.eanCode}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Distribution Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StockColumn(
                    label = "Store Stock",
                    qty = "${String.format("%.1f", summary.storeStock)} ${item.unit.symbol}",
                    highlight = summary.isLowStock
                )
                StockColumn(
                    label = "Kitchen Stock",
                    qty = "${String.format("%.1f", summary.kitchenStock)} ${item.unit.symbol}",
                    highlight = false
                )
                StockColumn(
                    label = "Total Available",
                    qty = "${String.format("%.1f", summary.totalStock)} ${item.unit.symbol}",
                    highlight = false,
                    isBold = true
                )
                StockColumn(
                    label = "Min Threshold",
                    qty = "${String.format("%.1f", item.minThreshold)} ${item.unit.symbol}",
                    highlight = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer with Location & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.rackLocation,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewBarcode,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EAN Tag", fontSize = 11.sp)
                    }

                    if (summary.isLowStock || summary.isOutOfStock) {
                        Button(
                            onClick = onQuickPO,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reorder", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockColumn(label: String, qty: String, highlight: Boolean, isBold: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = qty,
            fontSize = 13.sp,
            fontWeight = if (isBold || highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
        )
    }
}
