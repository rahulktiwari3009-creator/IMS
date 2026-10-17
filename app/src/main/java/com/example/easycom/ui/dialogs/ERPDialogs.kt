package com.example.easycom.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.easycom.model.*
import com.example.easycom.ui.EasyComViewModel
import com.example.easycom.ui.components.BarcodeVisual
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// --- 1. CREATE PURCHASE ORDER DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePODialog(viewModel: EasyComViewModel) {
    val vendors by viewModel.vendors.collectAsState()
    val items by viewModel.items.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedVendor by remember { mutableStateOf(vendors.firstOrNull()) }
    var creatorName by remember { mutableStateOf("Rahul Tiwari") }
    var expectedDate by remember { mutableStateOf("20 Oct 2026") }
    var notes by remember { mutableStateOf("Standard order for weekly supply") }

    // Line items being constructed
    var lineItems by remember {
        mutableStateOf(
            listOf(
                POLineItem(
                    itemId = items.first().id,
                    itemName = items.first().name,
                    sku = items.first().sku,
                    eanCode = items.first().eanCode,
                    unit = items.first().unit,
                    orderedQty = 30.0,
                    unitPrice = items.first().costPrice,
                    taxPercent = 5.0
                )
            )
        )
    }

    var selectedItemToAdd by remember { mutableStateOf(items.firstOrNull()) }
    var addQtyText by remember { mutableStateOf("20") }

    Dialog(
        onDismissRequest = { viewModel.closeCreatePODialog() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("create_po_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Create Purchase Order",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PO will be submitted for Maker-Checker approval",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { viewModel.closeCreatePODialog() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Vendor Selector
                    Text("Select Supplier / Vendor", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vendors) { vendor ->
                            FilterChip(
                                selected = selectedVendor?.id == vendor.id,
                                onClick = { selectedVendor = vendor },
                                label = { Text(vendor.name) }
                            )
                        }
                    }

                    // Metadata
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = creatorName,
                            onValueChange = { creatorName = it },
                            label = { Text("PO Creator Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = expectedDate,
                            onValueChange = { expectedDate = it },
                            label = { Text("Expected Delivery") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Order Notes & Terms") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Line Items Section
                    Text("Order Line Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    lineItems.forEachIndexed { index, line ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(line.itemName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        "${line.orderedQty} ${line.unit.symbol} @ ₹${line.unitPrice} + ${line.taxPercent}% Tax",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    "₹${String.format("%,.2f", line.totalAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (lineItems.size > 1) {
                                            lineItems = lineItems.filterIndexed { i, _ -> i != index }
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Add SKU row
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Add Another SKU to PO:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(items.filter { !it.isFinishedGood }) { itm ->
                                    FilterChip(
                                        selected = selectedItemToAdd?.id == itm.id,
                                        onClick = { selectedItemToAdd = itm },
                                        label = { Text(itm.name, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = addQtyText,
                                    onValueChange = { addQtyText = it },
                                    label = { Text("Quantity") },
                                    modifier = Modifier.width(120.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        val itm = selectedItemToAdd
                                        val q = addQtyText.toDoubleOrNull() ?: 10.0
                                        if (itm != null) {
                                            lineItems = lineItems + POLineItem(
                                                itemId = itm.id,
                                                itemName = itm.name,
                                                sku = itm.sku,
                                                eanCode = itm.eanCode,
                                                unit = itm.unit,
                                                orderedQty = q,
                                                unitPrice = itm.costPrice,
                                                taxPercent = 5.0
                                            )
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to PO", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Total and Submit
                val grandTotal = lineItems.sumOf { it.totalAmount }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total PO Amount (with Tax)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            "₹${String.format("%,.2f", grandTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.closeCreatePODialog() }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val v = selectedVendor ?: vendors.first()
                                viewModel.submitPO(v, lineItems, creatorName, expectedDate, notes)
                            },
                            modifier = Modifier.testTag("submit_po_button")
                        ) {
                            Text("Submit for Approval")
                        }
                    }
                }
            }
        }
    }
}

// --- 2. PO DETAIL & APPROVAL / CLOSURE DIALOG ---
@Composable
fun PODetailDialog(viewModel: EasyComViewModel, po: PurchaseOrder) {
    val currentRole by viewModel.currentRole.collectAsState()
    var remarksText by remember { mutableStateOf("Approved as per requisition budget") }
    var closeReasonText by remember { mutableStateOf("Vendor unable to supply remaining stock") }
    var showCloseInput by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { viewModel.closePODetail() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("po_detail_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(po.poNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                            Text(po.vendorName, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    IconButton(onClick = { viewModel.closePODetail() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status Banner
                    Surface(
                        color = Color(po.status.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(po.status.colorHex))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Status: ${po.status.label}", fontWeight = FontWeight.Bold, color = Color(po.status.colorHex), fontSize = 14.sp)
                                if (po.approvedBy != null) {
                                    Text("Approved by ${po.approvedBy} on ${po.approvalDate ?: "Recorded"}", fontSize = 11.sp)
                                }
                                if (po.closedReason != null) {
                                    Text("Closure reason: ${po.closedReason}", fontSize = 11.sp, color = Color.Red)
                                }
                            }
                        }
                    }

                    // Metadata Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Created by: ${po.creatorName} (${po.creatorRole}) on ${po.createdDate}", fontSize = 12.sp)
                            Text("Expected Delivery: ${po.expectedDeliveryDate}", fontSize = 12.sp)
                            if (po.notes.isNotBlank()) {
                                Text("Notes: ${po.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Text("Line Items & Quantities", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    po.items.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.itemName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("EAN: ${item.eanCode} | SKU: ${item.sku}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("Ordered: ${item.orderedQty} ${item.unit.symbol} | Inwarded: ${item.receivedQty} ${item.unit.symbol}", fontSize = 11.sp, color = Color(0xFF1565C0))
                                }
                                Text("₹${String.format("%,.2f", item.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Approval Action Box (for Approver or Admin)
                    if (po.status == POStatus.PENDING_APPROVAL) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E7D32)))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Approval Authority Review", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                                Text("As an Approver/Admin, review items and authorize store to inward.", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = remarksText,
                                    onValueChange = { remarksText = it },
                                    label = { Text("Approval Remarks") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.approvePO(po, "${currentRole.name} Approver", remarksText)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.weight(1f).testTag("dialog_approve_button")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve PO")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.rejectPO(po, "${currentRole.name} Approver", remarksText)
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject PO")
                                    }
                                }
                            }
                        }
                    }

                    // Close PO by Creator Form
                    if (po.status != POStatus.COMPLETED && po.status != POStatus.CLOSED_BY_CREATOR && po.status != POStatus.CANCELLED) {
                        if (showCloseInput) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Close PO as Creator", fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontSize = 13.sp)
                                    OutlinedTextField(
                                        value = closeReasonText,
                                        onValueChange = { closeReasonText = it },
                                        label = { Text("Reason for Closing") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                viewModel.closePOByCreator(po, po.creatorName, closeReasonText)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                        ) {
                                            Text("Confirm Close PO")
                                        }
                                        OutlinedButton(onClick = { showCloseInput = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showCloseInput = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Close this PO (Creator Action)")
                            }
                        }
                    }
                }

                // Footer
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Value: ₹${String.format("%,.2f", po.totalOrderValue)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (po.status == POStatus.APPROVED || po.status == POStatus.PARTIALLY_RECEIVED) {
                        Button(
                            onClick = {
                                viewModel.closePODetail()
                                viewModel.openCreateGRN(po)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take GRN in Store")
                        }
                    } else {
                        Button(onClick = { viewModel.closePODetail() }) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

// --- 3. CREATE GRN DIALOG (STORE RECEIVES ON APPROVED PO) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGRNDialog(viewModel: EasyComViewModel, po: PurchaseOrder) {
    var receivedBy by remember { mutableStateOf("Suresh Storekeeper") }
    var invoiceNumber by remember { mutableStateOf("INV-2026-${Random.nextInt(1000, 9999)}") }
    var vehicleNumber by remember { mutableStateOf("DL-01-EA-5544") }
    var remarks by remember { mutableStateOf("Package intact, barcodes scanned & verified") }

    // Line items for GRN
    var grnItems by remember {
        mutableStateOf(
            po.items.map { item ->
                val remaining = item.orderedQty - item.receivedQty
                GRNLineItem(
                    itemId = item.itemId,
                    itemName = item.itemName,
                    sku = item.sku,
                    eanCode = item.eanCode,
                    orderedQty = item.orderedQty,
                    receivedQty = if (remaining > 0) remaining else 0.0,
                    rejectedQty = 0.0,
                    unit = item.unit,
                    batchNumber = "BAT-${item.sku.replace("RM-", "")}-${SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())}",
                    mfgDate = "10 Oct 2026",
                    expiryDate = "10 Dec 2026",
                    putAwayLocation = "Rack A-01"
                )
            }
        )
    }

    Dialog(
        onDismissRequest = { viewModel.closeCreateGRN() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("create_grn_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Goods Received Note (GRN)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Receiving against Approved ${po.poNumber} (${po.vendorName})", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { viewModel.closeCreateGRN() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = receivedBy,
                            onValueChange = { receivedBy = it },
                            label = { Text("Received By") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            label = { Text("Vendor Invoice#") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it },
                            label = { Text("Vehicle#") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text("Store Remarks") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Divider()

                    Text("Inward Items & Assign Batch / Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    grnItems.forEachIndexed { index, grnLine ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(grnLine.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("EAN: ${grnLine.eanCode}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = grnLine.receivedQty.toString(),
                                        onValueChange = { q ->
                                            val num = q.toDoubleOrNull() ?: 0.0
                                            val copy = grnItems.toMutableList()
                                            copy[index] = copy[index].copy(receivedQty = num)
                                            grnItems = copy
                                        },
                                        label = { Text("Received (${grnLine.unit.symbol})") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = grnLine.batchNumber,
                                        onValueChange = { b ->
                                            val copy = grnItems.toMutableList()
                                            copy[index] = copy[index].copy(batchNumber = b)
                                            grnItems = copy
                                        },
                                        label = { Text("Assign Batch#") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = grnLine.expiryDate,
                                        onValueChange = { exp ->
                                            val copy = grnItems.toMutableList()
                                            copy[index] = copy[index].copy(expiryDate = exp)
                                            grnItems = copy
                                        },
                                        label = { Text("Expiry Date") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = grnLine.putAwayLocation,
                                        onValueChange = { loc ->
                                            val copy = grnItems.toMutableList()
                                            copy[index] = copy[index].copy(putAwayLocation = loc)
                                            grnItems = copy
                                        },
                                        label = { Text("Putaway Bin") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { viewModel.closeCreateGRN() }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.submitGRN(po, grnItems, receivedBy, vehicleNumber, invoiceNumber, remarks)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("submit_grn_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm GRN Inward")
                    }
                }
            }
        }
    }
}

// --- 4. BARCODE SCANNER DIALOG (REALISTIC SIMULATION & QUICK SCANS) ---
@Composable
fun BarcodeScannerDialog(viewModel: EasyComViewModel) {
    val items by viewModel.items.collectAsState()
    val batches by viewModel.batches.collectAsState()

    var scannedCode by remember { mutableStateOf("") }
    var matchedItem by remember { mutableStateOf<ItemMaster?>(null) }
    var matchedBatches by remember { mutableStateOf<List<InventoryBatch>>(emptyList()) }

    // Laser beam animation
    val infiniteTransition = rememberInfiniteTransition()
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    fun doScan(code: String) {
        scannedCode = code
        val itm = items.find { it.eanCode == code || it.sku.equals(code, ignoreCase = true) }
        matchedItem = itm
        matchedBatches = if (itm != null) batches.filter { it.itemId == itm.id } else emptyList()
    }

    Dialog(
        onDismissRequest = { viewModel.closeBarcodeScanner() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("barcode_scanner_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EAN-13 Barcode Scanner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { viewModel.closeBarcodeScanner() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val laserY = size.height * laserOffset
                        drawLine(
                            color = Color(0xFFFF1744),
                            start = Offset(20f, laserY),
                            end = Offset(size.width - 20f, laserY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Align EAN Barcode inside viewfinder", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual input or quick click
                OutlinedTextField(
                    value = scannedCode,
                    onValueChange = { doScan(it) },
                    label = { Text("EAN Barcode / SKU Input") },
                    placeholder = { Text("Scan or enter 13-digit EAN...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (scannedCode.isNotEmpty()) {
                            IconButton(onClick = { doScan("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Quick Scan SKU Presets (Tap to Scan):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items) { itm ->
                        ElevatedFilterChip(
                            selected = scannedCode == itm.eanCode,
                            onClick = { doScan(itm.eanCode) },
                            label = { Text("${itm.name} (${itm.eanCode.takeLast(4)})", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scan Results
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    val itm = matchedItem
                    if (itm != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(itm.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("SKU: ${itm.sku} | EAN: ${itm.eanCode}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                        Text(itm.category.displayName, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Store Balance", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        val storeQty = matchedBatches.filter { !it.inKitchen }.sumOf { it.quantity }
                                        Text("${String.format("%.1f", storeQty)} ${itm.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Kitchen Balance", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        val kitQty = matchedBatches.filter { it.inKitchen }.sumOf { it.quantity }
                                        Text("${String.format("%.1f", kitQty)} ${itm.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Min Safe Threshold", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Text("${itm.minThreshold} ${itm.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Active Physical Batches in Warehouse:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                matchedBatches.forEach { b ->
                                    Text(
                                        "• Batch: ${b.batchNumber} - ${b.quantity} ${b.unit.symbol} @ ${b.location} (Exp: ${b.expiryDate})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.closeBarcodeScanner()
                                            viewModel.openKitchenIssue()
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Issue to Kitchen", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else if (scannedCode.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("No SKU matching barcode: $scannedCode", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                Text("Check the digits or add this SKU in the Admin Master Catalog.", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.closeBarcodeScanner() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Scanner")
                }
            }
        }
    }
}

// --- 5. KITCHEN ISSUE DIALOG (STORE -> KITCHEN WITH BATCH TRACKING) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenIssueDialog(viewModel: EasyComViewModel) {
    val items by viewModel.items.collectAsState()
    val batches by viewModel.batches.collectAsState()

    val rawMaterialItems = items.filter { !it.isFinishedGood }
    var selectedItem by remember { mutableStateOf(rawMaterialItems.firstOrNull()) }

    val storeBatchesForItem = batches.filter { it.itemId == selectedItem?.id && !it.inKitchen && it.quantity > 0.01 }
    var selectedBatch by remember { mutableStateOf(storeBatchesForItem.firstOrNull()) }

    var issueQtyText by remember { mutableStateOf("5") }
    var kitchenStation by remember { mutableStateOf("Bakery Section") }
    var issuedBy by remember { mutableStateOf("Chef Rajesh") }

    val stations = listOf("Bakery Section", "Main Hot Kitchen", "Pizza Oven Counter", "Dessert & Pastry Prep")

    Dialog(
        onDismissRequest = { viewModel.closeKitchenIssue() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("kitchen_issue_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Issue Material to Kitchen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Store to Kitchen Batch Requisition", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeKitchenIssue() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item Selector
                    Text("1. Select Raw Material:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rawMaterialItems) { itm ->
                            FilterChip(
                                selected = selectedItem?.id == itm.id,
                                onClick = {
                                    selectedItem = itm
                                    val newBatches = batches.filter { it.itemId == itm.id && !it.inKitchen && it.quantity > 0.01 }
                                    selectedBatch = newBatches.firstOrNull()
                                },
                                label = { Text(itm.name) }
                            )
                        }
                    }

                    // Source Batch (FIFO)
                    Text("2. Select Store Source Batch (FIFO enforcement):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (storeBatchesForItem.isEmpty()) {
                        Text("No stock in Store for this item! Please create a PO.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        storeBatchesForItem.forEach { b ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBatch = b },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedBatch?.id == b.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = if (selectedBatch?.id == b.id) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Batch: ${b.batchNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Text("Location: ${b.location} | Exp: ${b.expiryDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Text("Available: ${b.quantity} ${b.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }

                    // Issue Qty
                    OutlinedTextField(
                        value = issueQtyText,
                        onValueChange = { issueQtyText = it },
                        label = { Text("Quantity to Issue (${selectedItem?.unit?.symbol ?: "units"})") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Target Station
                    Text("3. Target Kitchen Station:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(stations) { st ->
                            FilterChip(
                                selected = kitchenStation == st,
                                onClick = { kitchenStation = st },
                                label = { Text(st) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = issuedBy,
                        onValueChange = { issuedBy = it },
                        label = { Text("Issued By / Chef Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { viewModel.closeKitchenIssue() }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val itm = selectedItem
                            val b = selectedBatch
                            val q = issueQtyText.toDoubleOrNull() ?: 0.0
                            if (itm != null && b != null && q > 0) {
                                viewModel.submitKitchenIssue(itm, b, q, kitchenStation, issuedBy)
                            } else {
                                viewModel.showToast("Please enter valid quantity and select a batch")
                            }
                        },
                        modifier = Modifier.testTag("submit_kitchen_issue_button")
                    ) {
                        Text("Confirm Issue to Kitchen")
                    }
                }
            }
        }
    }
}

// --- 6. PRODUCTION & FG (FINISHED GOODS) WITH EAN BARCODE GENERATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionFGDialog(viewModel: EasyComViewModel) {
    val items by viewModel.items.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val batches by viewModel.batches.collectAsState()

    val fgItems = items.filter { it.isFinishedGood }
    var selectedFG by remember { mutableStateOf(fgItems.firstOrNull()) }
    var producedQtyText by remember { mutableStateOf("25") }
    var chefName by remember { mutableStateOf("Chef Sanjeev") }
    var station by remember { mutableStateOf("Bakery Section") }
    var bestBeforeDays by remember { mutableStateOf(4) }

    val matchingRecipe = recipes.find { it.fgItemId == selectedFG?.id }

    Dialog(
        onDismissRequest = { viewModel.closeProduction() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("production_fg_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Kitchen FG Production & EAN Generation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                        Text("Converts raw material batches to Finished Goods with EAN-13 barcode", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeProduction() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Select Finished Good (FG) to Produce:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fgItems) { fg ->
                            FilterChip(
                                selected = selectedFG?.id == fg.id,
                                onClick = { selectedFG = fg },
                                label = { Text(fg.name) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = producedQtyText,
                            onValueChange = { producedQtyText = it },
                            label = { Text("Yield Qty (${selectedFG?.unit?.symbol ?: "pcs"})") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = chefName,
                            onValueChange = { chefName = it },
                            label = { Text("Head Chef") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // BOM Ingredients & Consumed Batch link
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("BOM Recipe & Raw Material Linkage", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            val r = matchingRecipe
                            if (r != null) {
                                r.ingredients.forEach { ing ->
                                    val q = (producedQtyText.toDoubleOrNull() ?: 20.0) * ing.requiredQtyPerUnit
                                    Text("• ${ing.itemName}: requires approx ${String.format("%.2f", q)} ${ing.unit.symbol}", fontSize = 11.sp)
                                }
                            } else {
                                Text("Standard batch formula applied.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    // Auto EAN Barcode Generation Preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8E24AA)))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨ EAN-13 BARCODE WILL BE AUTO-GENERATED", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF6A1B9A))
                            Text("A new verifiable GS1-compatible EAN-13 barcode will be issued with production batch number and inwarded into FG Stock automatically.", fontSize = 11.sp, color = Color(0xFF4A148C))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { viewModel.closeProduction() }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val fg = selectedFG
                            val qty = producedQtyText.toDoubleOrNull() ?: 0.0
                            if (fg != null && qty > 0) {
                                val summary = "Batch RM-AMUL-2601 (Butter) & Batch RM-GRAIN-2602 (Maida)"
                                viewModel.submitKitchenProduction(fg, qty, chefName, station, summary)
                            } else {
                                viewModel.showToast("Please enter a valid yield quantity")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        modifier = Modifier.testTag("submit_production_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Produce & Generate EAN")
                    }
                }
            }
        }
    }
}

// --- 7. BARCODE & BATCH LABEL VIEWER DIALOG ---
@Composable
fun BarcodeLabelDialog(viewModel: EasyComViewModel, batch: InventoryBatch) {
    Dialog(
        onDismissRequest = { viewModel.closeBarcodeLabel() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("barcode_label_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EAN-13 Product & Batch Tag", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { viewModel.closeBarcodeLabel() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Physical Label Simulation
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = batch.itemName.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "BATCH: ${batch.batchNumber}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // High fidelity Barcode
                        BarcodeVisual(
                            eanCode = batch.eanCode,
                            modifier = Modifier.fillMaxWidth(),
                            height = 68,
                            showText = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mfg: ${batch.mfgDate}", fontSize = 10.sp, color = Color.Black)
                            Text("Exp: ${batch.expiryDate}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Loc: ${batch.location}", fontSize = 10.sp, color = Color.Black)
                            Text("Qty: ${batch.quantity} ${batch.unit.symbol}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.closeBarcodeLabel() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            viewModel.showToast("Print command sent to Zebra Barcode Thermal Printer (Port 9100)")
                            viewModel.closeBarcodeLabel()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Label")
                    }
                }
            }
        }
    }
}

// --- 8. ARCHITECTURE BLUEPRINT DIALOG (FOR WEB PAGE MIGRATION) ---
@Composable
fun BlueprintDialog(viewModel: EasyComViewModel) {
    Dialog(
        onDismissRequest = { viewModel.closeBlueprint() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("blueprint_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Architecture, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("EasyCom Web Architecture Blueprint", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Full Web Page Implementation Schema & API Specs", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    IconButton(onClick = { viewModel.closeBlueprint() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Aapke User Request ka Complete Technical Blueprint:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Is demo me sab cheezein verify hone ke baad aap is structure par directly web application build kar sakte hain.", fontSize = 11.sp)
                        }
                    }

                    BlueprintSection(
                        title = "1. Database Entities & Tables (PostgreSQL / MongoDB)",
                        content = """
• items: id, name, sku (unique), ean_code (13-digit), category, unit, min_threshold, reorder_qty, cost_price, is_finished_good, rack_location
• vendors: id, name, code, contact_person, phone, email, gstin, payment_terms
• purchase_orders: id, po_number (PO-YYYY-XXXX), vendor_id, creator_id, creator_name, status (PENDING_APPROVAL, APPROVED, REJECTED, PARTIALLY_RECEIVED, COMPLETED, CLOSED_BY_CREATOR), approved_by, approval_remarks, created_at, expected_delivery
• po_items: id, po_id, item_id, ordered_qty, received_qty, unit_price, tax_percent, total_amount
• grns: id, grn_number (GRN-YYYY-XXXX), po_id, vendor_id, received_by, vehicle_no, invoice_no, received_at
• inventory_batches: id, batch_number, item_id, ean_code, quantity, unit, location, in_kitchen, mfg_date, expiry_date, grn_id
• kitchen_issues: id, issue_no, item_id, quantity, source_batch_id, target_station, chef_id, issued_at
• recipes / bom: id, fg_item_id, ingredients_json
• production_fg: id, prod_no, fg_item_id, produced_qty, fg_batch_no, fg_ean_code, chef_name, consumed_batches_summary
• email_alert_logs: id, to_email, item_id, current_stock, threshold, triggered_at
• audit_logs: id, action, details, user_name, role, tag, timestamp
                        """.trimIndent()
                    )

                    BlueprintSection(
                        title = "2. REST API Endpoints Specification",
                        content = """
• POST /api/v1/po/create - Create Purchase Order (status: PENDING_APPROVAL)
• POST /api/v1/po/:id/approve - Approver Maker-Checker authorization
• POST /api/v1/po/:id/close - PO Creator close action with reason
• GET  /api/v1/po/approved-for-grn - Fetch all approved POs for store inward
• POST /api/v1/grn/inward - Receive goods, generate batch, update PO items
• GET  /api/v1/barcode/scan/:ean - Scan and lookup SKU, stock & active batches
• POST /api/v1/kitchen/issue - Issue batch from store to kitchen station
• POST /api/v1/kitchen/produce-fg - Produce Finished Good & auto-generate EAN-13
• GET  /api/v1/inventory/live - Real-time stock valuation & low-stock alerts
• POST /api/v1/alerts/trigger-auto-po - 1-Click PO generation from low stock alert
                        """.trimIndent()
                    )

                    BlueprintSection(
                        title = "3. Frontend Web Page Architecture (Next.js / React)",
                        content = """
• /dashboard: Live inventory tables, low-stock alerts widget, role switcher
• /purchase-orders: PO creator wizard, approval drawer, closure modal
• /store/grn: Inwarding against approved POs, barcode scanner viewfinder
• /kitchen: Requisition queue, batch conversion & FG EAN tag generator
• /admin: Master catalogs (SKUs, Vendors, BOM recipes, email trigger rules)
• Barcode Hardware: Compatible with HID USB Barcode Scanners & Zebra ZD421 Label Printers
                        """.trimIndent()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.showToast("Blueprint schema copied to clipboard!")
                        viewModel.closeBlueprint()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Acknowledge & Close Blueprint")
                }
            }
        }
    }
}

@Composable
fun BlueprintSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                content,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- 9. EMAIL ALERTS LOG DIALOG ---
@Composable
fun EmailAlertsDialog(viewModel: EasyComViewModel) {
    val emailAlerts by viewModel.emailAlerts.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.closeEmailAlerts() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("email_alerts_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto Low-Stock Email Alerts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { viewModel.closeEmailAlerts() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(emailAlerts, key = { it.id }) { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFA000)))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("DISPATCHED: ${alert.timestamp}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Surface(color = Color(0xFFE65100), shape = RoundedCornerShape(4.dp)) {
                                        Text("AUTO SENT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(alert.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                                Text("To: ${alert.toEmail}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Item: ${alert.itemName} has fallen to ${String.format("%.1f", alert.currentStock)} ${alert.unit.symbol} (below safety minimum of ${alert.minThreshold} ${alert.unit.symbol}). Immediate reorder recommended: ${alert.suggestedPOQty} ${alert.unit.symbol}.",
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.closeEmailAlerts()
                                        viewModel.openCreatePODialog()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("⚡ 1-Click Create PO from this Alert", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.closeEmailAlerts() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// --- 10. ADD NEW SKU DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewItemDialog(viewModel: EasyComViewModel) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var eanCode by remember { mutableStateOf(viewModel.repository.generateEan13("890" + Random.nextInt(100000000, 999999999))) }
    var category by remember { mutableStateOf(ItemCategory.RAW_MATERIAL) }
    var unit by remember { mutableStateOf(StockUnit.KG) }
    var minThresholdText by remember { mutableStateOf("20") }
    var costPriceText by remember { mutableStateOf("150") }
    var location by remember { mutableStateOf("Rack A-01") }
    var isFG by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { viewModel.closeNewItem() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Item / SKU Master", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU Code (e.g. RM-SUGAR-01)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = eanCode, onValueChange = { eanCode = it }, label = { Text("EAN-13 Barcode") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minThresholdText, onValueChange = { minThresholdText = it }, label = { Text("Min Safety Stock") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = costPriceText, onValueChange = { costPriceText = it }, label = { Text("Cost Price (₹)") }, modifier = Modifier.weight(1f), singleLine = true)
                }

                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Store Location / Bin") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFG, onCheckedChange = { isFG = it })
                    Text("Is Finished Good (Produced in Kitchen)", fontSize = 12.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { viewModel.closeNewItem() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && sku.isNotBlank()) {
                                val item = ItemMaster(
                                    name = name,
                                    sku = sku,
                                    eanCode = eanCode,
                                    category = if (isFG) ItemCategory.FINISHED_GOOD else category,
                                    unit = unit,
                                    minThreshold = minThresholdText.toDoubleOrNull() ?: 10.0,
                                    reorderQty = 50.0,
                                    costPrice = costPriceText.toDoubleOrNull() ?: 100.0,
                                    isFinishedGood = isFG,
                                    rackLocation = location
                                )
                                viewModel.addNewSKU(item)
                            }
                        }
                    ) {
                        Text("Save SKU")
                    }
                }
            }
        }
    }
}

// --- 11. DISPATCH / SALES DIALOG ---
@Composable
fun DispatchDialog(viewModel: EasyComViewModel, batch: InventoryBatch) {
    val items by viewModel.items.collectAsState()
    val matchingItem = items.find { it.id == batch.itemId || it.sku == batch.itemId }

    var quantityText by remember { mutableStateOf("5") }
    var channelText by remember { mutableStateOf("Swiggy Order #8890") }
    var invoiceText by remember { mutableStateOf("INV-DISP-${Random.nextInt(1000, 9999)}") }
    var dispatcherName by remember { mutableStateOf("Dispatch Lead Amit") }

    val channels = listOf("Swiggy", "Zomato", "Store Front Counter", "Direct Retail", "B2B Catering")

    Dialog(onDismissRequest = { viewModel.closeDispatch() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("dispatch_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dispatch Finished Goods", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                        Text("Sale / Outward from FG Stock", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeDispatch() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(batch.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Batch: ${batch.batchNumber} | Location: ${batch.location}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Available in Stock:", fontSize = 12.sp)
                            Text("${batch.quantity} ${batch.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity to Dispatch (${batch.unit.symbol})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Sales Channel / Delivery Mode:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(channels) { ch ->
                        FilterChip(
                            selected = channelText.startsWith(ch),
                            onClick = { channelText = "$ch - Order #${Random.nextInt(1000, 9999)}" },
                            label = { Text(ch) }
                        )
                    }
                }

                OutlinedTextField(
                    value = channelText,
                    onValueChange = { channelText = it },
                    label = { Text("Customer / Channel Details") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = invoiceText,
                        onValueChange = { invoiceText = it },
                        label = { Text("Invoice / Challan#") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dispatcherName,
                        onValueChange = { dispatcherName = it },
                        label = { Text("Dispatched By") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { viewModel.closeDispatch() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull() ?: 0.0
                            val itm = matchingItem ?: ItemMaster(
                                name = batch.itemName,
                                sku = batch.itemId,
                                eanCode = batch.eanCode,
                                category = ItemCategory.FINISHED_GOOD,
                                unit = batch.unit,
                                minThreshold = 5.0,
                                reorderQty = 20.0,
                                costPrice = 100.0,
                                isFinishedGood = true
                            )
                            if (qty > 0) {
                                viewModel.submitDispatch(itm, batch, qty, channelText, invoiceText, dispatcherName)
                            } else {
                                viewModel.showToast("Please enter a valid quantity")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B)),
                        modifier = Modifier.testTag("confirm_dispatch_button")
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm Dispatch")
                    }
                }
            }
        }
    }
}

// --- 12. EXPORT LEDGER DIALOG (CSV DOWNLOAD / COPY / SHARE) ---
@Composable
fun ExportLedgerDialog(viewModel: EasyComViewModel) {
    val context = LocalContext.current
    val ledgerRows by viewModel.ledgerRows.collectAsState()
    val csvContent = remember(ledgerRows) { viewModel.getExportCSV() }

    Dialog(
        onDismissRequest = { viewModel.closeExport() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("export_ledger_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Export Inventory & Movement Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Full batch traceability CSV for Excel & Google Sheets", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    IconButton(onClick = { viewModel.closeExport() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Ledger Rows", fontSize = 11.sp, color = Color(0xFF2E7D32))
                            Text("${ledgerRows.size} Records", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Columns", fontSize = 11.sp, color = Color(0xFF1565C0))
                            Text("17 Data Fields", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Data Fields Included in Export:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Text(
                    "✓ SKU  ✓ Item Name  ✓ EAN-13  ✓ Batch #  ✓ Mfg Date  ✓ Expiry Date  ✓ Location  ✓ PO Ordered Qty  ✓ GRN Received Qty  ✓ Kitchen Issued Qty  ✓ FG Produced Qty  ✓ Dispatched Qty  ✓ Stock Balance  ✓ Linked PO#  ✓ Linked GRN#",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CSV Raw Preview
                Text("CSV Preview (First 500 characters):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = csvContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFF81C784),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Copy to Clipboard, Share via Intent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("EasyCom_Inventory_Ledger_CSV", csvContent)
                            clipboard.setPrimaryClip(clip)
                            viewModel.showToast("📋 Entire CSV copied to clipboard! Ready to paste in Excel/Sheets.")
                        },
                        modifier = Modifier.weight(1f).testTag("copy_csv_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy CSV", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                putExtra(Intent.EXTRA_SUBJECT, "EasyCom Inventory Movement & Batch Ledger")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Ledger via")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        modifier = Modifier.weight(1.2f).testTag("share_csv_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share CSV / Excel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
