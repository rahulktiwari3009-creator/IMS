package com.example.easycom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycom.model.POStatus
import com.example.easycom.model.PurchaseOrder
import com.example.easycom.model.UserRole
import com.example.easycom.ui.EasyComViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderScreen(viewModel: EasyComViewModel) {
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf<POStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPOs = purchaseOrders.filter { po ->
        val matchesStatus = selectedStatusFilter == null || po.status == selectedStatusFilter
        val matchesQuery = searchQuery.isBlank() ||
                po.poNumber.contains(searchQuery, ignoreCase = true) ||
                po.vendorName.contains(searchQuery, ignoreCase = true) ||
                po.creatorName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreatePODialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create New PO") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_po_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("po_list_screen"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Screen Header & Explanation
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Purchase Orders & Approval Flow",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Maker-Checker Workflow: Create PO -> Authorize/Approve -> Store GRN -> Close by Creator",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by PO#, Vendor or Creator...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == null,
                                onClick = { selectedStatusFilter = null },
                                label = { Text("All (${purchaseOrders.size})") }
                            )
                        }
                        items(POStatus.values()) { status ->
                            val count = purchaseOrders.count { it.status == status }
                            FilterChip(
                                selected = selectedStatusFilter == status,
                                onClick = {
                                    selectedStatusFilter = if (selectedStatusFilter == status) null else status
                                },
                                label = { Text("${status.label} ($count)") }
                            )
                        }
                    }
                }
            }

            // PO Cards
            items(filteredPOs, key = { it.id }) { po ->
                POCard(
                    po = po,
                    currentRole = currentRole,
                    onClick = { viewModel.openPODetail(po) },
                    onApprove = { viewModel.openPODetail(po) },
                    onTakeGRN = { viewModel.openCreateGRN(po) },
                    onClosePO = { viewModel.openPODetail(po) }
                )
            }

            if (filteredPOs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No purchase orders found matching criteria", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun POCard(
    po: PurchaseOrder,
    currentRole: UserRole,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onTakeGRN: () -> Unit,
    onClosePO: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("po_card_${po.poNumber}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: PO# + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = po.poNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = Color(po.status.colorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = po.status.label,
                        color = Color(po.status.colorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vendor & Creator Info
            Text(
                text = po.vendorName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Created by: ${po.creatorName} (${po.creatorRole})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = po.createdDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Line items preview
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                po.items.take(2).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${item.itemName} (${item.orderedQty} ${item.unit.symbol})",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₹${String.format("%,.2f", item.totalAmount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (po.items.size > 2) {
                    Text(
                        text = "+ ${po.items.size - 2} more item(s)...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Summary & Contextual Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Order Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "₹${String.format("%,.2f", po.totalOrderValue)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Action based on status
                    when (po.status) {
                        POStatus.PENDING_APPROVAL -> {
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Review & Approve", fontSize = 11.sp)
                            }
                        }
                        POStatus.APPROVED, POStatus.PARTIALLY_RECEIVED -> {
                            Button(
                                onClick = onTakeGRN,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Take GRN in Store", fontSize = 11.sp)
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = onClick,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("View Details", fontSize = 11.sp)
                            }
                        }
                    }

                    // Creator Close PO option
                    if (po.status != POStatus.COMPLETED && po.status != POStatus.CLOSED_BY_CREATOR && po.status != POStatus.CANCELLED) {
                        OutlinedButton(
                            onClick = onClosePO,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                        ) {
                            Text("Close PO", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
