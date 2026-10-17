package com.example.easycom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycom.data.EasyComRepository
import com.example.easycom.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab(val title: String, val iconName: String) {
    DASHBOARD("Dashboard", "dashboard"),
    PURCHASE_ORDERS("Purchase Orders", "shopping_cart"),
    GRN_STORE("Store GRN & Scan", "inventory"),
    KITCHEN("Kitchen & FG", "restaurant"),
    LEDGER("360° Stock Ledger", "table_chart"),
    ADMIN("Admin & Blueprint", "admin_panel_settings")
}

data class ItemStockSummary(
    val item: ItemMaster,
    val storeStock: Double,
    val kitchenStock: Double,
    val totalStock: Double,
    val isLowStock: Boolean,
    val isOutOfStock: Boolean,
    val batchCount: Int
)

class EasyComViewModel(
    val repository: EasyComRepository = EasyComRepository()
) : ViewModel() {

    val currentRole = repository.currentRole
    val vendors = repository.vendors
    val items = repository.items
    val batches = repository.batches
    val purchaseOrders = repository.purchaseOrders
    val grnList = repository.grnList
    val kitchenIssues = repository.kitchenIssues
    val recipes = repository.recipes
    val productionLogs = repository.productionLogs
    val dispatches = repository.dispatches
    val emailAlerts = repository.emailAlerts
    val auditLogs = repository.auditLogs
    val users = repository.users
    val currentUser = repository.currentUser
    val locations = repository.locations
    val systemSettings = repository.systemSettings

    sealed class DeleteTarget(val title: String, val message: String, val onConfirm: () -> Unit) {
        class DeleteSKU(val item: ItemMaster, onConfirm: () -> Unit) : DeleteTarget("Delete SKU", "Are you sure you want to delete '${item.name}' (${item.sku})? This cannot be undone.", onConfirm)
        class DeleteVendor(val vendor: Vendor, onConfirm: () -> Unit) : DeleteTarget("Delete Vendor", "Are you sure you want to remove supplier '${vendor.name}'?", onConfirm)
        class DeleteRecipe(val recipe: Recipe, onConfirm: () -> Unit) : DeleteTarget("Delete Recipe", "Are you sure you want to delete BOM recipe for '${recipe.fgName}'?", onConfirm)
        class DeleteUser(val user: AppUser, onConfirm: () -> Unit) : DeleteTarget("Delete User Account", "Are you sure you want to revoke access for '${user.name}' (${user.email})?", onConfirm)
        class DeleteLocation(val location: LocationMaster, onConfirm: () -> Unit) : DeleteTarget("Delete Storage Bin", "Are you sure you want to delete bin '${location.name}' (${location.code})?", onConfirm)
    }

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(MainTab.DASHBOARD)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ItemCategory?>(null)
    val selectedCategoryFilter: StateFlow<ItemCategory?> = _selectedCategoryFilter.asStateFlow()

    // UI Dialog State Holders
    private val _showCreatePODialog = MutableStateFlow(false)
    val showCreatePODialog: StateFlow<Boolean> = _showCreatePODialog.asStateFlow()

    private val _selectedPOForDetail = MutableStateFlow<PurchaseOrder?>(null)
    val selectedPOForDetail: StateFlow<PurchaseOrder?> = _selectedPOForDetail.asStateFlow()

    private val _showCreateGRNDialog = MutableStateFlow<PurchaseOrder?>(null)
    val showCreateGRNDialog: StateFlow<PurchaseOrder?> = _showCreateGRNDialog.asStateFlow()

    private val _showBarcodeScannerDialog = MutableStateFlow(false)
    val showBarcodeScannerDialog: StateFlow<Boolean> = _showBarcodeScannerDialog.asStateFlow()

    private val _showKitchenIssueDialog = MutableStateFlow(false)
    val showKitchenIssueDialog: StateFlow<Boolean> = _showKitchenIssueDialog.asStateFlow()

    private val _showProductionDialog = MutableStateFlow(false)
    val showProductionDialog: StateFlow<Boolean> = _showProductionDialog.asStateFlow()

    private val _showBarcodeLabelDialog = MutableStateFlow<InventoryBatch?>(null)
    val showBarcodeLabelDialog: StateFlow<InventoryBatch?> = _showBarcodeLabelDialog.asStateFlow()

    private val _showDispatchDialog = MutableStateFlow<InventoryBatch?>(null)
    val showDispatchDialog: StateFlow<InventoryBatch?> = _showDispatchDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showBlueprintDialog = MutableStateFlow(false)
    val showBlueprintDialog: StateFlow<Boolean> = _showBlueprintDialog.asStateFlow()

    private val _showEmailAlertsDialog = MutableStateFlow(false)
    val showEmailAlertsDialog: StateFlow<Boolean> = _showEmailAlertsDialog.asStateFlow()

    private val _showNewItemDialog = MutableStateFlow(false)
    val showNewItemDialog: StateFlow<Boolean> = _showNewItemDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    // CRUD Edit State Holders
    private val _editingItem = MutableStateFlow<ItemMaster?>(null)
    val editingItem: StateFlow<ItemMaster?> = _editingItem.asStateFlow()

    private val _editingVendor = MutableStateFlow<Vendor?>(null)
    val editingVendor: StateFlow<Vendor?> = _editingVendor.asStateFlow()

    private val _editingRecipe = MutableStateFlow<Recipe?>(null)
    val editingRecipe: StateFlow<Recipe?> = _editingRecipe.asStateFlow()

    private val _editingUser = MutableStateFlow<AppUser?>(null)
    val editingUser: StateFlow<AppUser?> = _editingUser.asStateFlow()

    private val _editingLocation = MutableStateFlow<LocationMaster?>(null)
    val editingLocation: StateFlow<LocationMaster?> = _editingLocation.asStateFlow()

    private val _deleteTarget = MutableStateFlow<DeleteTarget?>(null)
    val deleteTarget: StateFlow<DeleteTarget?> = _deleteTarget.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Derived: 360° Comprehensive Stock Movement Ledger
    val ledgerRows: StateFlow<List<ItemBatchLedgerRow>> = combine(
        batches,
        purchaseOrders,
        kitchenIssues,
        productionLogs,
        dispatches
    ) { _, _, _, _, _ ->
        repository.generateFullLedger()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.generateFullLedger())

    // Derived: Combined Stock Summary per SKU
    val stockSummaries: StateFlow<List<ItemStockSummary>> = combine(
        items,
        batches,
        searchQuery,
        selectedCategoryFilter
    ) { allItems, allBatches, query, catFilter ->
        allItems.map { item ->
            val itemBatches = allBatches.filter { it.itemId == item.id }
            val storeStock = itemBatches.filter { !it.inKitchen }.sumOf { it.quantity }
            val kitchenStock = itemBatches.filter { it.inKitchen }.sumOf { it.quantity }
            val totalStock = storeStock + kitchenStock
            val isOutOfStock = totalStock <= 0.001
            val isLowStock = storeStock <= item.minThreshold && !isOutOfStock

            ItemStockSummary(
                item = item,
                storeStock = storeStock,
                kitchenStock = kitchenStock,
                totalStock = totalStock,
                isLowStock = isLowStock,
                isOutOfStock = isOutOfStock,
                batchCount = itemBatches.size
            )
        }.filter { summary ->
            val matchesQuery = query.isBlank() ||
                    summary.item.name.contains(query, ignoreCase = true) ||
                    summary.item.sku.contains(query, ignoreCase = true) ||
                    summary.item.eanCode.contains(query, ignoreCase = true)
            val matchesCat = catFilter == null || summary.item.category == catFilter
            matchesQuery && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setRole(role: UserRole) {
        repository.setRole(role)
        showToast("Switched to ${role.title}")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: ItemCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Dialog toggles
    fun openCreatePODialog() { _showCreatePODialog.value = true }
    fun closeCreatePODialog() { _showCreatePODialog.value = false }

    fun openPODetail(po: PurchaseOrder) { _selectedPOForDetail.value = po }
    fun closePODetail() { _selectedPOForDetail.value = null }

    fun openCreateGRN(po: PurchaseOrder) {
        _showCreateGRNDialog.value = po
    }
    fun closeCreateGRN() { _showCreateGRNDialog.value = null }

    fun openBarcodeScanner() { _showBarcodeScannerDialog.value = true }
    fun closeBarcodeScanner() { _showBarcodeScannerDialog.value = false }

    fun openKitchenIssue() { _showKitchenIssueDialog.value = true }
    fun closeKitchenIssue() { _showKitchenIssueDialog.value = false }

    fun openProduction() { _showProductionDialog.value = true }
    fun closeProduction() { _showProductionDialog.value = false }

    fun openBarcodeLabel(batch: InventoryBatch) { _showBarcodeLabelDialog.value = batch }
    fun closeBarcodeLabel() { _showBarcodeLabelDialog.value = null }

    fun openBlueprint() { _showBlueprintDialog.value = true }
    fun closeBlueprint() { _showBlueprintDialog.value = false }

    fun openEmailAlerts() { _showEmailAlertsDialog.value = true }
    fun closeEmailAlerts() { _showEmailAlertsDialog.value = false }

    fun openDispatch(batch: InventoryBatch) { _showDispatchDialog.value = batch }
    fun closeDispatch() { _showDispatchDialog.value = null }

    fun openExport() { _showExportDialog.value = true }
    fun closeExport() { _showExportDialog.value = false }

    fun openNewItem() { _showNewItemDialog.value = true }
    fun closeNewItem() { _showNewItemDialog.value = false }

    fun openSettings() { _showSettingsDialog.value = true }
    fun closeSettings() { _showSettingsDialog.value = false }
    fun saveSystemSettings(settings: SystemSettings) {
        repository.updateSystemSettings(settings)
        _showSettingsDialog.value = false
        showToast("System Control Settings updated successfully!")
    }

    fun submitDispatch(
        fgItem: ItemMaster,
        sourceBatch: InventoryBatch,
        quantity: Double,
        channel: String,
        invoice: String,
        dispatchedBy: String
    ) {
        val success = repository.recordDispatch(fgItem, sourceBatch, quantity, channel, invoice, dispatchedBy)
        if (success) {
            showToast("Dispatched $quantity ${fgItem.unit.symbol} to $channel")
            closeDispatch()
        } else {
            showToast("Error: Batch has insufficient quantity!")
        }
    }

    fun getExportCSV(): String = repository.exportLedgerToCSV()

    // Actions
    fun submitPO(vendor: Vendor, items: List<POLineItem>, creatorName: String, expectedDate: String, notes: String) {
        val po = repository.createPurchaseOrder(vendor, items, creatorName, expectedDate, notes)
        showToast("Created ${po.poNumber} (Pending Approval)")
        closeCreatePODialog()
    }

    fun approvePO(po: PurchaseOrder, approverName: String, remarks: String) {
        repository.approvePurchaseOrder(po.id, approverName, remarks)
        showToast("PO ${po.poNumber} has been APPROVED! Store can now take GRN.")
        closePODetail()
    }

    fun rejectPO(po: PurchaseOrder, approverName: String, remarks: String) {
        repository.rejectPurchaseOrder(po.id, approverName, remarks)
        showToast("PO ${po.poNumber} has been REJECTED.")
        closePODetail()
    }

    fun closePOByCreator(po: PurchaseOrder, closerName: String, reason: String) {
        repository.closePurchaseOrder(po.id, closerName, reason)
        showToast("PO ${po.poNumber} has been CLOSED by Creator.")
        closePODetail()
    }

    fun submitGRN(
        po: PurchaseOrder,
        receivedItems: List<GRNLineItem>,
        receivedBy: String,
        vehicleNo: String,
        invoiceNo: String,
        remarks: String
    ) {
        val grn = repository.processGRN(po, receivedItems, receivedBy, vehicleNo, invoiceNo, remarks)
        showToast("GRN ${grn.grnNumber} generated! Stock inwarded with EAN barcode tags.")
        closeCreateGRN()
    }

    fun submitKitchenIssue(
        item: ItemMaster,
        sourceBatch: InventoryBatch,
        qty: Double,
        station: String,
        issuedBy: String
    ) {
        val success = repository.issueToKitchen(item, sourceBatch, qty, station, issuedBy)
        if (success) {
            showToast("Issued $qty ${item.unit.symbol} of ${item.name} to $station")
            closeKitchenIssue()
        } else {
            showToast("Error: Store batch has insufficient quantity!")
        }
    }

    fun submitKitchenProduction(
        fgItem: ItemMaster,
        producedQty: Double,
        chefName: String,
        station: String,
        consumedSummary: String
    ) {
        val log = repository.recordKitchenProduction(fgItem, producedQty, chefName, station, consumedSummary)
        showToast("Produced $producedQty ${fgItem.unit.symbol}! EAN ${log.fgEanCode} generated & added to FG Stock.")
        closeProduction()
    }

    fun addNewSKU(item: ItemMaster) {
        repository.addItem(item)
        showToast("SKU ${item.sku} added to catalog.")
        closeNewItem()
    }

    // --- CRUD Dialog Open / Close ---
    fun openEditSKU(item: ItemMaster?) {
        _editingItem.value = item ?: ItemMaster(
            name = "",
            sku = "RM-NEW-${kotlin.random.Random.nextInt(100, 999)}",
            eanCode = repository.generateEan13("890" + kotlin.random.Random.nextInt(100000000, 999999999)),
            category = ItemCategory.RAW_MATERIAL,
            unit = StockUnit.KG,
            minThreshold = 20.0,
            reorderQty = 50.0,
            costPrice = 100.0
        )
    }
    fun closeEditSKU() { _editingItem.value = null }

    fun openEditVendor(vendor: Vendor?) {
        _editingVendor.value = vendor ?: Vendor(
            name = "",
            code = "VEN-${kotlin.random.Random.nextInt(100, 999)}",
            contactPerson = "",
            phone = "",
            email = "",
            gstin = ""
        )
    }
    fun closeEditVendor() { _editingVendor.value = null }

    fun openEditRecipe(recipe: Recipe?) {
        val defaultFG = items.value.firstOrNull { it.isFinishedGood }
        _editingRecipe.value = recipe ?: Recipe(
            fgItemId = defaultFG?.id ?: "",
            fgName = defaultFG?.name ?: "Finished Good",
            ingredients = emptyList()
        )
    }
    fun closeEditRecipe() { _editingRecipe.value = null }

    fun openEditUser(user: AppUser?) {
        _editingUser.value = user ?: AppUser(
            name = "",
            email = "",
            role = UserRole.STORE_KEEPER,
            permissionLevel = PermissionLevel.FULL_EDIT
        )
    }
    fun closeEditUser() { _editingUser.value = null }

    fun openEditLocation(loc: LocationMaster?) {
        _editingLocation.value = loc ?: LocationMaster(
            code = "LOC-${kotlin.random.Random.nextInt(10, 99)}",
            name = "",
            zone = "Ambient Warehouse"
        )
    }
    fun closeEditLocation() { _editingLocation.value = null }

    fun promptDelete(target: DeleteTarget) { _deleteTarget.value = target }
    fun dismissDelete() { _deleteTarget.value = null }

    // --- Save & Delete Actions ---
    fun saveSKU(item: ItemMaster) {
        val exists = items.value.any { it.id == item.id }
        if (exists) {
            repository.updateItem(item)
            showToast("SKU '${item.sku}' updated successfully")
        } else {
            repository.addItem(item)
            showToast("SKU '${item.sku}' created successfully")
        }
        closeEditSKU()
    }

    fun deleteSKU(itemId: String) {
        repository.deleteItem(itemId)
        showToast("SKU deleted")
        dismissDelete()
    }

    fun saveVendor(vendor: Vendor) {
        val exists = vendors.value.any { it.id == vendor.id }
        if (exists) {
            repository.updateVendor(vendor)
            showToast("Vendor '${vendor.name}' updated")
        } else {
            repository.addVendor(vendor)
            showToast("Vendor '${vendor.name}' created")
        }
        closeEditVendor()
    }

    fun deleteVendor(vendorId: String) {
        repository.deleteVendor(vendorId)
        showToast("Vendor removed")
        dismissDelete()
    }

    fun saveRecipe(recipe: Recipe) {
        val exists = recipes.value.any { it.id == recipe.id }
        if (exists) {
            repository.updateRecipe(recipe)
            showToast("BOM Recipe for '${recipe.fgName}' updated")
        } else {
            repository.addRecipe(recipe)
            showToast("BOM Recipe for '${recipe.fgName}' created")
        }
        closeEditRecipe()
    }

    fun deleteRecipe(recipeId: String) {
        repository.deleteRecipe(recipeId)
        showToast("Recipe deleted")
        dismissDelete()
    }

    fun saveUser(user: AppUser) {
        val exists = users.value.any { it.id == user.id }
        if (exists) {
            repository.updateUser(user)
            showToast("User '${user.name}' updated")
        } else {
            repository.addUser(user)
            showToast("User '${user.name}' (${user.email}) registered with ${user.permissionLevel.label}")
        }
        closeEditUser()
    }

    fun toggleUserStatus(userId: String) {
        repository.toggleUserStatus(userId)
        val u = users.value.find { it.id == userId }
        val st = if (u?.isActive == true) "Active" else "Suspended"
        showToast("User access status: $st")
    }

    fun deleteUser(userId: String) {
        val success = repository.deleteUser(userId)
        if (success) {
            showToast("User access removed")
        } else {
            showToast("Cannot delete currently active user!")
        }
        dismissDelete()
    }

    fun switchActiveUser(user: AppUser) {
        repository.switchActiveUser(user)
        showToast("Switched active user: ${user.name} (${user.permissionLevel.label})")
    }

    fun saveLocation(loc: LocationMaster) {
        val exists = locations.value.any { it.id == loc.id }
        if (exists) {
            repository.updateLocation(loc)
            showToast("Storage location '${loc.name}' updated")
        } else {
            repository.addLocation(loc)
            showToast("Storage location '${loc.name}' created")
        }
        closeEditLocation()
    }

    fun deleteLocation(locId: String) {
        repository.deleteLocation(locId)
        showToast("Location removed")
        dismissDelete()
    }

    fun resetDemo() {
        repository.resetDemoData()
        showToast("Demo data reloaded to initial state.")
    }
}
