package com.example.easycom.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.easycom.model.SystemSettings
import com.example.easycom.ui.EasyComViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsDialog(viewModel: EasyComViewModel) {
    val currentSettings by viewModel.systemSettings.collectAsState()

    var companyName by remember(currentSettings) { mutableStateOf(currentSettings.companyName) }
    var businessType by remember(currentSettings) { mutableStateOf(currentSettings.businessType) }
    var gstin by remember(currentSettings) { mutableStateOf(currentSettings.gstin) }
    var defaultWarehouse by remember(currentSettings) { mutableStateOf(currentSettings.defaultWarehouse) }
    var primaryEmail by remember(currentSettings) { mutableStateOf(currentSettings.primaryEmail) }
    var alertRecipients by remember(currentSettings) { mutableStateOf(currentSettings.alertRecipients) }
    var phone by remember(currentSettings) { mutableStateOf(currentSettings.phone) }
    var currencySymbol by remember(currentSettings) { mutableStateOf(currentSettings.currencySymbol) }

    // PO & Approval Rules
    var poApprovalThreshold by remember(currentSettings) { mutableStateOf(currentSettings.poApprovalThreshold.toString()) }
    var autoApproveBelowThreshold by remember(currentSettings) { mutableStateOf(currentSettings.autoApproveBelowThreshold) }
    var allowCreatorToClosePO by remember(currentSettings) { mutableStateOf(currentSettings.allowCreatorToClosePO) }
    var poPrefix by remember(currentSettings) { mutableStateOf(currentSettings.poPrefix) }

    // GRN & Inward Rules
    var strictPOMatching by remember(currentSettings) { mutableStateOf(currentSettings.strictPOMatching) }
    var allowOverReceivingPercent by remember(currentSettings) { mutableStateOf(currentSettings.allowOverReceivingPercent.toString()) }
    var autoGenerateBatchOnInward by remember(currentSettings) { mutableStateOf(currentSettings.autoGenerateBatchOnInward) }
    var defaultPutawayBin by remember(currentSettings) { mutableStateOf(currentSettings.defaultPutawayBin) }

    // Auto Email Alert Rules
    var enableLowStockEmailAlerts by remember(currentSettings) { mutableStateOf(currentSettings.enableLowStockEmailAlerts) }
    var autoCreateDraftPOOnAlert by remember(currentSettings) { mutableStateOf(currentSettings.autoCreateDraftPOOnAlert) }
    var dailyMorningSummaryEmail by remember(currentSettings) { mutableStateOf(currentSettings.dailyMorningSummaryEmail) }

    // Kitchen & Production Rules
    var autoDeductKitchenBOM by remember(currentSettings) { mutableStateOf(currentSettings.autoDeductKitchenBOM) }
    var allowPartialBatchIssue by remember(currentSettings) { mutableStateOf(currentSettings.allowPartialBatchIssue) }
    var defaultFGShelfLifeDays by remember(currentSettings) { mutableStateOf(currentSettings.defaultFGShelfLifeDays.toString()) }
    var barcodeCountryPrefix by remember(currentSettings) { mutableStateOf(currentSettings.barcodeCountryPrefix) }

    // Tax
    var defaultTaxPercent by remember(currentSettings) { mutableStateOf(currentSettings.defaultTaxPercent.toString()) }
    var availableTaxSlabs by remember(currentSettings) { mutableStateOf(currentSettings.availableTaxSlabs) }

    Dialog(onDismissRequest = { viewModel.closeSettings() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("system_settings_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Control Panel: Master Edit Set",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "System Parameters • Approval Rules • Email SMTP • Warehousing",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.closeSettings() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form with Categorized Sections
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. COMPANY & WAREHOUSE IDENTITY
                    SettingsSectionCard(
                        title = "1. Organization & Warehouse Profile",
                        subtitle = "Legal entity, GSTIN, primary email and warehouse hub details",
                        icon = Icons.Default.Business
                    ) {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company / Brand Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = businessType,
                                onValueChange = { businessType = it },
                                label = { Text("Business Domain") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = gstin,
                                onValueChange = { gstin = it },
                                label = { Text("GSTIN Registration *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = defaultWarehouse,
                            onValueChange = { defaultWarehouse = it },
                            label = { Text("Primary Central Warehouse / Hub") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = primaryEmail,
                                onValueChange = { primaryEmail = it },
                                label = { Text("Master Admin Email") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Support Phone") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = currencySymbol,
                                onValueChange = { currencySymbol = it },
                                label = { Text("Currency Symbol (₹ / $)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = defaultPutawayBin,
                                onValueChange = { defaultPutawayBin = it },
                                label = { Text("Default Putaway Bin") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    // 2. PURCHASE ORDER & APPROVAL WORKFLOW
                    SettingsSectionCard(
                        title = "2. PO Policy & Approval Workflow Rules",
                        subtitle = "Configure maker-checker limits and closure permissions",
                        icon = Icons.Default.ReceiptLong
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = poApprovalThreshold,
                                onValueChange = { poApprovalThreshold = it },
                                label = { Text("Director Approval Threshold (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = poPrefix,
                                onValueChange = { poPrefix = it },
                                label = { Text("PO Numbering Prefix") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                SettingSwitchRow(
                                    title = "Allow PO Creator to Close PO",
                                    desc = "The purchase officer who made the PO can close or terminate it with reason",
                                    checked = allowCreatorToClosePO,
                                    onCheckedChange = { allowCreatorToClosePO = it }
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                SettingSwitchRow(
                                    title = "Auto-Approve Low-Value Orders",
                                    desc = "Skip director approval for orders under threshold",
                                    checked = autoApproveBelowThreshold,
                                    onCheckedChange = { autoApproveBelowThreshold = it }
                                )
                            }
                        }
                    }

                    // 3. STORE GRN & BARCODE INWARD
                    SettingsSectionCard(
                        title = "3. Store GRN & EAN Scanning Rules",
                        subtitle = "E-waybill verification, barcode allocation, and receiving tolerance",
                        icon = Icons.Default.QrCodeScanner
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = allowOverReceivingPercent,
                                onValueChange = { allowOverReceivingPercent = it },
                                label = { Text("Over-Receiving Tolerance (%)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = barcodeCountryPrefix,
                                onValueChange = { barcodeCountryPrefix = it },
                                label = { Text("EAN-13 Country Code (890=India)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                SettingSwitchRow(
                                    title = "Strict PO Matching Required",
                                    desc = "GRN can only be generated against an Approved PO",
                                    checked = strictPOMatching,
                                    onCheckedChange = { strictPOMatching = it }
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                SettingSwitchRow(
                                    title = "Auto-Generate Inventory Batch Tag",
                                    desc = "Automatically assign FIFO batch number and mfg/exp date upon receiving",
                                    checked = autoGenerateBatchOnInward,
                                    onCheckedChange = { autoGenerateBatchOnInward = it }
                                )
                            }
                        }
                    }

                    // 4. LOW-STOCK AUTO EMAIL ALERTS
                    SettingsSectionCard(
                        title = "4. Automated Low-Stock Alert Engine",
                        subtitle = "Email dispatch settings and automatic re-ordering",
                        icon = Icons.Default.Email
                    ) {
                        OutlinedTextField(
                            value = alertRecipients,
                            onValueChange = { alertRecipients = it },
                            label = { Text("Email Alert Recipients (comma separated) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 2
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                SettingSwitchRow(
                                    title = "Real-Time Email Alert on Low Stock",
                                    desc = "Dispatch instant email when store balance <= safety threshold",
                                    checked = enableLowStockEmailAlerts,
                                    onCheckedChange = { enableLowStockEmailAlerts = it }
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                SettingSwitchRow(
                                    title = "Auto-Generate Draft PO",
                                    desc = "Draft purchase order created immediately for vendor when alert fires",
                                    checked = autoCreateDraftPOOnAlert,
                                    onCheckedChange = { autoCreateDraftPOOnAlert = it }
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                SettingSwitchRow(
                                    title = "Daily 8:00 AM Inventory Digest",
                                    desc = "Send morning PDF summary of critical items and expiry risks",
                                    checked = dailyMorningSummaryEmail,
                                    onCheckedChange = { dailyMorningSummaryEmail = it }
                                )
                            }
                        }
                    }

                    // 5. KITCHEN PRODUCTION & FG RECIPES
                    SettingsSectionCard(
                        title = "5. Kitchen BOM & Production Rules",
                        subtitle = "Traceability from raw batch issue to finished goods EAN barcode",
                        icon = Icons.Default.Restaurant
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = defaultFGShelfLifeDays,
                                onValueChange = { defaultFGShelfLifeDays = it },
                                label = { Text("Default FG Shelf Life (Days)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = defaultTaxPercent,
                                onValueChange = { defaultTaxPercent = it },
                                label = { Text("Default GST Tax %") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = availableTaxSlabs,
                            onValueChange = { availableTaxSlabs = it },
                            label = { Text("Available Tax Slabs") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                SettingSwitchRow(
                                    title = "Auto-Deduct Kitchen Raw Materials",
                                    desc = "Automatically consume linked ingredients when finished goods are logged",
                                    checked = autoDeductKitchenBOM,
                                    onCheckedChange = { autoDeductKitchenBOM = it }
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                SettingSwitchRow(
                                    title = "Allow Partial Batch Requisition",
                                    desc = "Store can issue fractional batch quantities to specific kitchen stations",
                                    checked = allowPartialBatchIssue,
                                    onCheckedChange = { allowPartialBatchIssue = it }
                                )
                            }
                        }
                    }

                    // 6. QUICK ACTIONS TO DIRECT MASTERS
                    SettingsSectionCard(
                        title = "6. Direct Master Catalog Controls",
                        subtitle = "Quick jump to add, edit or delete any master directly",
                        icon = Icons.Default.Dataset
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    viewModel.closeSettings()
                                    viewModel.openEditSKU(null)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New SKU", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.closeSettings()
                                    viewModel.openEditVendor(null)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Vendor", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.closeSettings()
                                    viewModel.openEditUser(null)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New User", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.closeSettings() }
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val updated = currentSettings.copy(
                                companyName = companyName.trim(),
                                businessType = businessType.trim(),
                                gstin = gstin.trim(),
                                defaultWarehouse = defaultWarehouse.trim(),
                                primaryEmail = primaryEmail.trim(),
                                alertRecipients = alertRecipients.trim(),
                                phone = phone.trim(),
                                currencySymbol = currencySymbol.trim(),
                                poApprovalThreshold = poApprovalThreshold.toDoubleOrNull() ?: 15000.0,
                                autoApproveBelowThreshold = autoApproveBelowThreshold,
                                allowCreatorToClosePO = allowCreatorToClosePO,
                                poPrefix = poPrefix.trim(),
                                strictPOMatching = strictPOMatching,
                                allowOverReceivingPercent = allowOverReceivingPercent.toDoubleOrNull() ?: 5.0,
                                autoGenerateBatchOnInward = autoGenerateBatchOnInward,
                                defaultPutawayBin = defaultPutawayBin.trim(),
                                enableLowStockEmailAlerts = enableLowStockEmailAlerts,
                                autoCreateDraftPOOnAlert = autoCreateDraftPOOnAlert,
                                dailyMorningSummaryEmail = dailyMorningSummaryEmail,
                                autoDeductKitchenBOM = autoDeductKitchenBOM,
                                allowPartialBatchIssue = allowPartialBatchIssue,
                                defaultFGShelfLifeDays = defaultFGShelfLifeDays.toIntOrNull() ?: 14,
                                barcodeCountryPrefix = barcodeCountryPrefix.trim(),
                                defaultTaxPercent = defaultTaxPercent.toDoubleOrNull() ?: 5.0,
                                availableTaxSlabs = availableTaxSlabs.trim()
                            )
                            viewModel.saveSystemSettings(updated)
                        },
                        modifier = Modifier.testTag("save_system_settings_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Control Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(24.dp)
        )
    }
}
