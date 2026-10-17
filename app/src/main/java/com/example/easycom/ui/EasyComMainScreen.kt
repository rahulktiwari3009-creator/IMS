package com.example.easycom.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycom.ui.dialogs.*
import com.example.easycom.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyComMainScreen(viewModel: EasyComViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Dialog state collectors
    val showCreatePO by viewModel.showCreatePODialog.collectAsState()
    val selectedPOForDetail by viewModel.selectedPOForDetail.collectAsState()
    val showCreateGRN by viewModel.showCreateGRNDialog.collectAsState()
    val showBarcodeScanner by viewModel.showBarcodeScannerDialog.collectAsState()
    val showKitchenIssue by viewModel.showKitchenIssueDialog.collectAsState()
    val showProduction by viewModel.showProductionDialog.collectAsState()
    val showBarcodeLabel by viewModel.showBarcodeLabelDialog.collectAsState()
    val showDispatch by viewModel.showDispatchDialog.collectAsState()
    val showExport by viewModel.showExportDialog.collectAsState()
    val showBlueprint by viewModel.showBlueprintDialog.collectAsState()
    val showEmailAlerts by viewModel.showEmailAlertsDialog.collectAsState()
    val showNewItem by viewModel.showNewItemDialog.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()

    // Admin CRUD dialog states
    val editingItem by viewModel.editingItem.collectAsState()
    val editingVendor by viewModel.editingVendor.collectAsState()
    val editingRecipe by viewModel.editingRecipe.collectAsState()
    val editingUser by viewModel.editingUser.collectAsState()
    val editingLocation by viewModel.editingLocation.collectAsState()
    val deleteTarget by viewModel.deleteTarget.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "EasyCom Supply Chain",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PO • Approval • GRN • EAN Barcode • Kitchen FG ERP",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { viewModel.openSettings() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("top_bar_settings_button")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { viewModel.setTab(MainTab.LEDGER) },
                        modifier = Modifier.testTag("top_bar_ledger_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "Ledger")
                    }
                    IconButton(
                        onClick = { viewModel.openEmailAlerts() },
                        modifier = Modifier.testTag("top_bar_alerts_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color(0xFFE65100)) { Text("!") }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.openBarcodeScanner() },
                        modifier = Modifier.testTag("top_bar_scanner_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.DASHBOARD,
                    onClick = { viewModel.setTab(MainTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.PURCHASE_ORDERS,
                    onClick = { viewModel.setTab(MainTab.PURCHASE_ORDERS) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "PO") },
                    label = { Text("PO & Flow", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_po")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.GRN_STORE,
                    onClick = { viewModel.setTab(MainTab.GRN_STORE) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "GRN") },
                    label = { Text("Store GRN", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_grn")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.KITCHEN,
                    onClick = { viewModel.setTab(MainTab.KITCHEN) },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Kitchen") },
                    label = { Text("Kitchen FG", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_kitchen")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.LEDGER,
                    onClick = { viewModel.setTab(MainTab.LEDGER) },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Ledger") },
                    label = { Text("360° Ledger", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_ledger")
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.ADMIN,
                    onClick = { viewModel.setTab(MainTab.ADMIN) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.DASHBOARD -> DashboardScreen(viewModel)
                MainTab.PURCHASE_ORDERS -> PurchaseOrderScreen(viewModel)
                MainTab.GRN_STORE -> StoreGRNScreen(viewModel)
                MainTab.KITCHEN -> KitchenScreen(viewModel)
                MainTab.LEDGER -> InventoryLedgerScreen(viewModel)
                MainTab.ADMIN -> AdminScreen(viewModel)
            }
        }

        // Active Dialogs Overlays
        if (showCreatePO) {
            CreatePODialog(viewModel)
        }
        selectedPOForDetail?.let { po ->
            PODetailDialog(viewModel, po)
        }
        showCreateGRN?.let { po ->
            CreateGRNDialog(viewModel, po)
        }
        if (showBarcodeScanner) {
            BarcodeScannerDialog(viewModel)
        }
        if (showKitchenIssue) {
            KitchenIssueDialog(viewModel)
        }
        if (showProduction) {
            ProductionFGDialog(viewModel)
        }
        showBarcodeLabel?.let { batch ->
            BarcodeLabelDialog(viewModel, batch)
        }
        showDispatch?.let { batch ->
            DispatchDialog(viewModel, batch)
        }
        if (showExport) {
            ExportLedgerDialog(viewModel)
        }
        if (showBlueprint) {
            BlueprintDialog(viewModel)
        }
        if (showEmailAlerts) {
            EmailAlertsDialog(viewModel)
        }
        if (showNewItem) {
            NewItemDialog(viewModel)
        }
        if (showSettings) {
            SystemSettingsDialog(viewModel)
        }

        // Admin CRUD Dialog Overlays
        editingItem?.let { item ->
            EditSKUDialog(viewModel, item)
        }
        editingVendor?.let { vendor ->
            EditVendorDialog(viewModel, vendor)
        }
        editingRecipe?.let { recipe ->
            EditRecipeDialog(viewModel, recipe)
        }
        editingUser?.let { user ->
            EditUserDialog(viewModel, user)
        }
        editingLocation?.let { loc ->
            EditLocationDialog(viewModel, loc)
        }
        deleteTarget?.let { target ->
            ConfirmDeleteDialog(viewModel, target)
        }
    }
}
