package com.example.easycom.data

import com.example.easycom.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class EasyComRepository {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Active Role
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Vendors
    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors: StateFlow<List<Vendor>> = _vendors.asStateFlow()

    // Master Catalog
    private val _items = MutableStateFlow<List<ItemMaster>>(emptyList())
    val items: StateFlow<List<ItemMaster>> = _items.asStateFlow()

    // Batches / Physical Inventory
    private val _batches = MutableStateFlow<List<InventoryBatch>>(emptyList())
    val batches: StateFlow<List<InventoryBatch>> = _batches.asStateFlow()

    // Purchase Orders
    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = _purchaseOrders.asStateFlow()

    // Goods Received Notes (GRN)
    private val _grnList = MutableStateFlow<List<GRN>>(emptyList())
    val grnList: StateFlow<List<GRN>> = _grnList.asStateFlow()

    // Kitchen Issues
    private val _kitchenIssues = MutableStateFlow<List<KitchenIssue>>(emptyList())
    val kitchenIssues: StateFlow<List<KitchenIssue>> = _kitchenIssues.asStateFlow()

    // Kitchen Recipes / BOM
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    // Production Logs (FG Created)
    private val _productionLogs = MutableStateFlow<List<ProductionLog>>(emptyList())
    val productionLogs: StateFlow<List<ProductionLog>> = _productionLogs.asStateFlow()

    // Email Alerts
    private val _emailAlerts = MutableStateFlow<List<EmailAlert>>(emptyList())
    val emailAlerts: StateFlow<List<EmailAlert>> = _emailAlerts.asStateFlow()

    // Dispatches (Finished Goods Dispatched / Sold)
    private val _dispatches = MutableStateFlow<List<DispatchRecord>>(emptyList())
    val dispatches: StateFlow<List<DispatchRecord>> = _dispatches.asStateFlow()

    // Audit Trail
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // User Directory & Granular RBAC Permissions
    private val _users = MutableStateFlow<List<AppUser>>(emptyList())
    val users: StateFlow<List<AppUser>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<AppUser>(
        AppUser(
            name = "Rahul Tiwari",
            email = "rahulktiwari3009@gmail.com",
            role = UserRole.ADMIN,
            permissionLevel = PermissionLevel.FULL_EDIT,
            department = "Executive Management",
            phone = "+91 98200 11223"
        )
    )
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    // Storage Locations & Warehouse Bins Master
    private val _locations = MutableStateFlow<List<LocationMaster>>(emptyList())
    val locations: StateFlow<List<LocationMaster>> = _locations.asStateFlow()

    // Global System Settings & Control Panel Configuration
    private val _systemSettings = MutableStateFlow(SystemSettings())
    val systemSettings: StateFlow<SystemSettings> = _systemSettings.asStateFlow()

    init {
        seedInitialData()
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        addAudit("ROLE_SWITCH", "User switched active session role to ${role.title}", role.name, "AUTH")
    }

    private fun addAudit(action: String, details: String, performedBy: String, tag: String) {
        val log = AuditLog(
            timestamp = dateFormat.format(Date()),
            action = action,
            details = details,
            performedBy = performedBy,
            tag = tag
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    private fun seedInitialData() {
        val vendorAmul = Vendor(
            id = "v-01",
            name = "Amul Dairy Products Ltd",
            code = "VEN-AMUL-01",
            contactPerson = "Rajesh Sharma",
            phone = "+91 98201 12345",
            email = "sales@amuldairy.com",
            gstin = "24AAACA1234M1Z5",
            paymentTerms = "Net 15 Days"
        )
        val vendorAgro = Vendor(
            id = "v-02",
            name = "Agro Fresh Farm Supplies",
            code = "VEN-AGRO-02",
            contactPerson = "Vikram Patel",
            phone = "+91 98765 43210",
            email = "orders@agrofresh.in",
            gstin = "27AABCA5678N1Z8",
            paymentTerms = "Net 30 Days"
        )
        val vendorMills = Vendor(
            id = "v-03",
            name = "Golden Grain Mills",
            code = "VEN-MILLS-03",
            contactPerson = "Anita Desai",
            phone = "+91 98111 22334",
            email = "dispatch@goldengrain.com",
            gstin = "07AABCG9876P1Z3",
            paymentTerms = "Advance / Net 7 Days"
        )
        val vendorPack = Vendor(
            id = "v-04",
            name = "PackMaster Solutions",
            code = "VEN-PACK-04",
            contactPerson = "Sunil Verma",
            phone = "+91 97654 32109",
            email = "support@packmasters.com",
            gstin = "06AAACP4321L1Z2",
            paymentTerms = "Net 30 Days"
        )
        _vendors.value = listOf(vendorAmul, vendorAgro, vendorMills, vendorPack)

        // Raw materials & Finished Goods
        val itemButter = ItemMaster(
            id = "itm-01",
            name = "Amul Unsalted Butter 500g",
            sku = "RM-BUTTER-500",
            eanCode = "8901262010123",
            category = ItemCategory.DAIRY,
            unit = StockUnit.KG,
            minThreshold = 25.0,
            reorderQty = 60.0,
            costPrice = 480.0,
            description = "High grade churned butter for bakery & kitchen prep",
            rackLocation = "Cold Room A-02"
        )
        val itemMaida = ItemMaster(
            id = "itm-02",
            name = "Premium Wheat Flour (Maida)",
            sku = "RM-MAIDA-001",
            eanCode = "8901030882145",
            category = ItemCategory.GRAIN_FLOUR,
            unit = StockUnit.KG,
            minThreshold = 50.0,
            reorderQty = 150.0,
            costPrice = 42.0,
            description = "Refined white flour for bakery and pizza dough",
            rackLocation = "Dry Store Bay-01"
        )
        val itemMozzarella = ItemMaster(
            id = "itm-03",
            name = "Diced Mozzarella Cheese",
            sku = "RM-CHEESE-MOZ",
            eanCode = "8901262030456",
            category = ItemCategory.DAIRY,
            unit = StockUnit.KG,
            minThreshold = 20.0,
            reorderQty = 50.0,
            costPrice = 520.0,
            description = "100% dairy mozzarella with supreme stretch",
            rackLocation = "Cold Room B-01"
        )
        val itemTomatoPuree = ItemMaster(
            id = "itm-04",
            name = "San Marzano Tomato Puree",
            sku = "RM-TOMATO-PUR",
            eanCode = "8902155098712",
            category = ItemCategory.SPICES_SAUCES,
            unit = StockUnit.KG,
            minThreshold = 30.0,
            reorderQty = 80.0,
            costPrice = 110.0,
            description = "Concentrated rich tomato sauce base",
            rackLocation = "Dry Store Bay-04"
        )
        val itemChocoChips = ItemMaster(
            id = "itm-05",
            name = "Dark Compound Chocolate Drops",
            sku = "RM-CHOC-55",
            eanCode = "8904018234567",
            category = ItemCategory.RAW_MATERIAL,
            unit = StockUnit.KG,
            minThreshold = 15.0,
            reorderQty = 40.0,
            costPrice = 360.0,
            description = "55% cocoa dark chocolate drops for pastries",
            rackLocation = "Temp Controlled C-01"
        )
        val itemPizzaBox = ItemMaster(
            id = "itm-06",
            name = "Corrugated Pizza Box 10-inch",
            sku = "PM-BOX-10",
            eanCode = "8906001239999",
            category = ItemCategory.PACKAGING,
            unit = StockUnit.PCS,
            minThreshold = 100.0,
            reorderQty = 500.0,
            costPrice = 14.5,
            description = "Kraft food-grade vented corrugated pizza box",
            rackLocation = "Packaging Bay P-01"
        )

        // Finished Goods (FG)
        val fgPastry = ItemMaster(
            id = "fg-01",
            name = "Signature Belgium Chocolate Pastry",
            sku = "FG-CHOC-PAS-01",
            eanCode = "8908889100012",
            category = ItemCategory.FINISHED_GOOD,
            unit = StockUnit.PCS,
            minThreshold = 10.0,
            reorderQty = 30.0,
            costPrice = 85.0,
            sellingPrice = 180.0,
            description = "Freshly baked artisan chocolate truffle pastry slice",
            isFinishedGood = true,
            rackLocation = "Display Chiller FG-01"
        )
        val fgPizza = ItemMaster(
            id = "fg-02",
            name = "Artisan Margherita Pizza 10\"",
            sku = "FG-MARG-PIZ-10",
            eanCode = "8908889200029",
            category = ItemCategory.FINISHED_GOOD,
            unit = StockUnit.PCS,
            minThreshold = 15.0,
            reorderQty = 40.0,
            costPrice = 140.0,
            sellingPrice = 320.0,
            description = "Hand-stretched sourdough pizza with mozzarella and basil",
            isFinishedGood = true,
            rackLocation = "Hot Kitchen Ready FG-02"
        )

        _items.value = listOf(
            itemButter, itemMaida, itemMozzarella, itemTomatoPuree, itemChocoChips, itemPizzaBox,
            fgPastry, fgPizza
        )

        // Recipes / BOM
        val recipePastry = Recipe(
            id = "rec-01",
            fgItemId = fgPastry.id,
            fgName = fgPastry.name,
            yieldUnit = StockUnit.PCS,
            ingredients = listOf(
                RecipeIngredient(itemButter.id, itemButter.name, 0.08, StockUnit.KG),
                RecipeIngredient(itemMaida.id, itemMaida.name, 0.12, StockUnit.KG),
                RecipeIngredient(itemChocoChips.id, itemChocoChips.name, 0.09, StockUnit.KG)
            )
        )
        val recipePizza = Recipe(
            id = "rec-02",
            fgItemId = fgPizza.id,
            fgName = fgPizza.name,
            yieldUnit = StockUnit.PCS,
            ingredients = listOf(
                RecipeIngredient(itemMaida.id, itemMaida.name, 0.18, StockUnit.KG),
                RecipeIngredient(itemMozzarella.id, itemMozzarella.name, 0.14, StockUnit.KG),
                RecipeIngredient(itemTomatoPuree.id, itemTomatoPuree.name, 0.08, StockUnit.KG),
                RecipeIngredient(itemPizzaBox.id, itemPizzaBox.name, 1.0, StockUnit.PCS)
            )
        )
        _recipes.value = listOf(recipePastry, recipePizza)

        // Seed Batches in Store
        val batch1 = InventoryBatch(
            id = "bat-01",
            batchNumber = "BAT-AMUL-2601",
            itemId = itemButter.id,
            itemName = itemButter.name,
            eanCode = itemButter.eanCode,
            quantity = 40.0,
            unit = StockUnit.KG,
            location = "Store: Cold Room A-02",
            inKitchen = false,
            mfgDate = "05 Oct 2026",
            expiryDate = "05 Dec 2026",
            grnNumber = "GRN-2026-0001",
            poNumber = "PO-2026-0010"
        )
        val batch2 = InventoryBatch(
            id = "bat-02",
            batchNumber = "BAT-GRAIN-2602",
            itemId = itemMaida.id,
            itemName = itemMaida.name,
            eanCode = itemMaida.eanCode,
            quantity = 120.0,
            unit = StockUnit.KG,
            location = "Store: Dry Store Bay-01",
            inKitchen = false,
            mfgDate = "01 Oct 2026",
            expiryDate = "01 Apr 2027",
            grnNumber = "GRN-2026-0002",
            poNumber = "PO-2026-0009"
        )
        val batch3 = InventoryBatch(
            id = "bat-03",
            batchNumber = "BAT-CHEESE-2603",
            itemId = itemMozzarella.id,
            itemName = itemMozzarella.name,
            eanCode = itemMozzarella.eanCode,
            quantity = 8.5, // Intentionally low to trigger real-time low-stock alert!
            unit = StockUnit.KG,
            location = "Store: Cold Room B-01",
            inKitchen = false,
            mfgDate = "10 Oct 2026",
            expiryDate = "25 Nov 2026",
            grnNumber = "GRN-2026-0003",
            poNumber = "PO-2026-0008"
        )
        val batch4 = InventoryBatch(
            id = "bat-04",
            batchNumber = "BAT-TOM-2604",
            itemId = itemTomatoPuree.id,
            itemName = itemTomatoPuree.name,
            eanCode = itemTomatoPuree.eanCode,
            quantity = 45.0,
            unit = StockUnit.KG,
            location = "Store: Dry Store Bay-04",
            inKitchen = false,
            mfgDate = "20 Sep 2026",
            expiryDate = "20 Sep 2027",
            grnNumber = "GRN-2026-0004",
            poNumber = "PO-2026-0007"
        )
        val batch5 = InventoryBatch(
            id = "bat-05",
            batchNumber = "BAT-PACK-2605",
            itemId = itemPizzaBox.id,
            itemName = itemPizzaBox.name,
            eanCode = itemPizzaBox.eanCode,
            quantity = 350.0,
            unit = StockUnit.PCS,
            location = "Store: Packaging Bay P-01",
            inKitchen = false,
            mfgDate = "02 Oct 2026",
            expiryDate = "02 Oct 2029",
            grnNumber = "GRN-2026-0005",
            poNumber = "PO-2026-0006"
        )
        // Some batch already in Kitchen
        val batchKitchen1 = InventoryBatch(
            id = "bat-k01",
            batchNumber = "BAT-AMUL-2601-K1",
            itemId = itemButter.id,
            itemName = itemButter.name,
            eanCode = itemButter.eanCode,
            quantity = 5.0,
            unit = StockUnit.KG,
            location = "Kitchen: Bakery Station",
            inKitchen = true,
            mfgDate = "05 Oct 2026",
            expiryDate = "05 Dec 2026",
            grnNumber = "GRN-2026-0001",
            poNumber = "PO-2026-0010"
        )
        // Finished Goods Batches in FG stock
        val batchFgPastry = InventoryBatch(
            id = "bat-fg-01",
            batchNumber = "BAT-FG-PAST-01",
            itemId = fgPastry.id,
            itemName = fgPastry.name,
            eanCode = fgPastry.eanCode,
            quantity = 35.0,
            unit = fgPastry.unit,
            location = "FG Stock: Display Chiller FG-01",
            inKitchen = false,
            mfgDate = "15 Oct 2026",
            expiryDate = "18 Oct 2026",
            grnNumber = "PROD-INTERNAL",
            poNumber = "KITCHEN-PROD"
        )
        val batchFgPizza = InventoryBatch(
            id = "bat-fg-02",
            batchNumber = "BAT-FG-PIZ-02",
            itemId = fgPizza.id,
            itemName = fgPizza.name,
            eanCode = fgPizza.eanCode,
            quantity = 20.0,
            unit = fgPizza.unit,
            location = "FG Stock: Hot Kitchen Ready FG-02",
            inKitchen = false,
            mfgDate = "16 Oct 2026",
            expiryDate = "17 Oct 2026",
            grnNumber = "PROD-INTERNAL",
            poNumber = "KITCHEN-PROD"
        )
        _batches.value = listOf(batch1, batch2, batch3, batch4, batch5, batchKitchen1, batchFgPastry, batchFgPizza)

        // Seed initial production log
        val prodLog1 = ProductionLog(
            productionNumber = "PROD-2026-0001",
            date = "15 Oct 2026, 09:30 AM",
            fgItemId = fgPastry.id,
            fgName = fgPastry.name,
            fgEanCode = fgPastry.eanCode,
            fgBatchNumber = "BAT-FG-PAST-01",
            producedQty = 45.0,
            unit = fgPastry.unit,
            chefName = "Chef Sanjeev",
            kitchenStation = "Bakery Station",
            consumedBatchSummary = "Batch BAT-AMUL-2601 (Butter): 4kg, Batch BAT-GRAIN-2602 (Maida): 6kg",
            expiryDate = "18 Oct 2026"
        )
        _productionLogs.value = listOf(prodLog1)

        // Seed initial kitchen issue
        val issue1 = KitchenIssue(
            issueNumber = "KIT-ISSUE-2026-0001",
            date = "15 Oct 2026, 08:00 AM",
            requestedBy = "Chef Sanjeev",
            kitchenStation = "Bakery Section",
            itemId = itemButter.id,
            itemName = itemButter.name,
            eanCode = itemButter.eanCode,
            quantity = 5.0,
            unit = itemButter.unit,
            sourceBatchNumber = "BAT-AMUL-2601",
            sourceLocation = "Store: Cold Room A-02"
        )
        _kitchenIssues.value = listOf(issue1)

        // Seed initial dispatch record (e.g. 10 pastries dispatched to Swiggy/Counter)
        val dispatch1 = DispatchRecord(
            dispatchNumber = "DISP-2026-0001",
            date = "16 Oct 2026, 01:15 PM",
            fgItemId = fgPastry.id,
            fgName = fgPastry.name,
            eanCode = fgPastry.eanCode,
            batchNumber = "BAT-FG-PAST-01",
            quantity = 10.0,
            unit = fgPastry.unit,
            customerOrChannel = "Swiggy Order Batch #4410",
            invoiceNumber = "INV-SALE-8821",
            dispatchedBy = "Dispatch Lead Amit"
        )
        _dispatches.value = listOf(dispatch1)

        // Seed Purchase Orders:
        // 1. Pending Approval PO (Ready for Approver to click Approve)
        val poPending = PurchaseOrder(
            id = "po-001",
            poNumber = "PO-2026-0015",
            vendorId = vendorAmul.id,
            vendorName = vendorAmul.name,
            creatorName = "Rahul Tiwari",
            creatorRole = "Purchase Officer",
            createdDate = "16 Oct 2026, 04:30 PM",
            expectedDeliveryDate = "18 Oct 2026",
            status = POStatus.PENDING_APPROVAL,
            notes = "Urgent replenishment for festive weekend crowd",
            items = listOf(
                POLineItem(
                    itemId = itemButter.id,
                    itemName = itemButter.name,
                    sku = itemButter.sku,
                    eanCode = itemButter.eanCode,
                    unit = itemButter.unit,
                    orderedQty = 50.0,
                    unitPrice = itemButter.costPrice,
                    taxPercent = 5.0
                ),
                POLineItem(
                    itemId = itemMozzarella.id,
                    itemName = itemMozzarella.name,
                    sku = itemMozzarella.sku,
                    eanCode = itemMozzarella.eanCode,
                    unit = itemMozzarella.unit,
                    orderedQty = 30.0,
                    unitPrice = itemMozzarella.costPrice,
                    taxPercent = 5.0
                )
            )
        )

        // 2. Approved PO (Ready for Store Keeper to click Take GRN)
        val poApproved = PurchaseOrder(
            id = "po-002",
            poNumber = "PO-2026-0014",
            vendorId = vendorMills.id,
            vendorName = vendorMills.name,
            creatorName = "Rahul Tiwari",
            creatorRole = "Purchase Officer",
            createdDate = "15 Oct 2026, 11:00 AM",
            expectedDeliveryDate = "17 Oct 2026",
            status = POStatus.APPROVED,
            approvedBy = "Vikramaditya (Finance Director)",
            approvalDate = "15 Oct 2026, 02:15 PM",
            approvalRemarks = "Budget approved under Q4 inventory expansion",
            notes = "Standard packaging bags required",
            items = listOf(
                POLineItem(
                    itemId = itemMaida.id,
                    itemName = itemMaida.name,
                    sku = itemMaida.sku,
                    eanCode = itemMaida.eanCode,
                    unit = itemMaida.unit,
                    orderedQty = 200.0,
                    unitPrice = itemMaida.costPrice,
                    taxPercent = 5.0
                )
            )
        )

        // 3. Completed PO
        val poCompleted = PurchaseOrder(
            id = "po-003",
            poNumber = "PO-2026-0010",
            vendorId = vendorAmul.id,
            vendorName = vendorAmul.name,
            creatorName = "Rahul Tiwari",
            creatorRole = "Purchase Officer",
            createdDate = "04 Oct 2026, 10:00 AM",
            expectedDeliveryDate = "06 Oct 2026",
            status = POStatus.COMPLETED,
            approvedBy = "Vikramaditya (Finance Director)",
            approvalDate = "04 Oct 2026, 01:00 PM",
            approvalRemarks = "Approved",
            items = listOf(
                POLineItem(
                    itemId = itemButter.id,
                    itemName = itemButter.name,
                    sku = itemButter.sku,
                    eanCode = itemButter.eanCode,
                    unit = itemButter.unit,
                    orderedQty = 45.0,
                    receivedQty = 45.0,
                    unitPrice = 480.0,
                    taxPercent = 5.0
                )
            )
        )
        _purchaseOrders.value = listOf(poPending, poApproved, poCompleted)

        // Seed Users with roles, email addresses & permissions
        val userAdmin = AppUser(
            id = "usr-01",
            name = "Rahul Tiwari",
            email = "rahulktiwari3009@gmail.com",
            role = UserRole.ADMIN,
            permissionLevel = PermissionLevel.FULL_EDIT,
            department = "Executive Management",
            phone = "+91 98200 11223"
        )
        val userDirector = AppUser(
            id = "usr-02",
            name = "Vikramaditya Rao",
            email = "director.finance@easycom.com",
            role = UserRole.APPROVER,
            permissionLevel = PermissionLevel.FULL_EDIT,
            department = "Finance & Approvals",
            phone = "+91 98111 44556"
        )
        val userPurchase = AppUser(
            id = "usr-03",
            name = "Pooja Sharma",
            email = "purchase.head@easycom.com",
            role = UserRole.PURCHASE_MANAGER,
            permissionLevel = PermissionLevel.FULL_EDIT,
            department = "Procurement",
            phone = "+91 98765 12345"
        )
        val userStore = AppUser(
            id = "usr-04",
            name = "Suresh Kumar",
            email = "store.head@easycom.com",
            role = UserRole.STORE_KEEPER,
            permissionLevel = PermissionLevel.FULL_EDIT,
            department = "Store & Warehouse",
            phone = "+91 97654 88990"
        )
        val userChef = AppUser(
            id = "usr-05",
            name = "Chef Sanjeev Kapoor",
            email = "executive.chef@cloudkitchen.com",
            role = UserRole.KITCHEN_HEAD,
            permissionLevel = PermissionLevel.FULL_EDIT,
            department = "Culinary Operations",
            phone = "+91 98222 33445"
        )
        val userAuditor = AppUser(
            id = "usr-06",
            name = "Neha Auditor",
            email = "audit.inspector@compliance.in",
            role = UserRole.APPROVER,
            permissionLevel = PermissionLevel.VIEW_ONLY,
            department = "External Audit",
            phone = "+91 98333 77889"
        )
        _users.value = listOf(userAdmin, userDirector, userPurchase, userStore, userChef, userAuditor)
        _currentUser.value = userAdmin

        // Seed Warehouse Locations / Bins
        val loc1 = LocationMaster(id = "loc-01", code = "COLD-A02", name = "Cold Room A-02", zone = "Cold Storage (-18°C to 4°C)", capacity = "800 kg")
        val loc2 = LocationMaster(id = "loc-02", code = "DRY-BAY01", name = "Dry Store Bay-01", zone = "Dry Ambient Warehouse", capacity = "2000 kg")
        val loc3 = LocationMaster(id = "loc-03", code = "COLD-B01", name = "Cold Room B-01", zone = "Cold Storage (2°C to 6°C)", capacity = "600 kg")
        val loc4 = LocationMaster(id = "loc-04", code = "DRY-BAY04", name = "Dry Store Bay-04", zone = "Dry Ambient Warehouse", capacity = "1500 kg")
        val loc5 = LocationMaster(id = "loc-05", code = "PACK-P01", name = "Packaging Bay P-01", zone = "Packaging Material Area", capacity = "1000 units")
        val loc6 = LocationMaster(id = "loc-06", code = "KIT-BAKE", name = "Bakery Production Section", zone = "Kitchen Floor", capacity = "Active Prep")
        val loc7 = LocationMaster(id = "loc-07", code = "FG-CHILL", name = "Display Chiller FG-01", zone = "Finished Goods Ready", capacity = "200 units")
        _locations.value = listOf(loc1, loc2, loc3, loc4, loc5, loc6, loc7)

        // Initial Email Alert for Mozzarella (since 8.5 kg < min 20 kg)
        triggerLowStockEmail(itemMozzarella, 8.5)

        // Audit seed
        addAudit("SYSTEM_INIT", "EasyCom ERP initialized with standard catalog, vendors & BOM", "System", "SYSTEM")
    }

    // --- PURCHASE ORDER MANAGEMENT ---

    fun createPurchaseOrder(
        vendor: Vendor,
        items: List<POLineItem>,
        creatorName: String,
        expectedDate: String,
        notes: String
    ): PurchaseOrder {
        val nextNum = "PO-2026-${String.format("%04d", _purchaseOrders.value.size + 16)}"
        val po = PurchaseOrder(
            poNumber = nextNum,
            vendorId = vendor.id,
            vendorName = vendor.name,
            creatorName = creatorName,
            createdDate = dateFormat.format(Date()),
            expectedDeliveryDate = expectedDate,
            items = items,
            status = POStatus.PENDING_APPROVAL,
            notes = notes
        )
        _purchaseOrders.value = listOf(po) + _purchaseOrders.value
        addAudit("PO_CREATED", "New PO $nextNum created for ${vendor.name} with ${items.size} items (₹${String.format("%.2f", po.totalOrderValue)})", creatorName, "PO")
        return po
    }

    fun approvePurchaseOrder(poId: String, approverName: String, remarks: String) {
        val currentList = _purchaseOrders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == poId }
        if (index != -1) {
            val oldPo = currentList[index]
            val updated = oldPo.copy(
                status = POStatus.APPROVED,
                approvedBy = approverName,
                approvalRemarks = remarks.ifBlank { "Approved by $approverName" },
                approvalDate = dateFormat.format(Date())
            )
            currentList[index] = updated
            _purchaseOrders.value = currentList
            addAudit("PO_APPROVED", "PO ${oldPo.poNumber} APPROVED by $approverName. Remarks: $remarks", approverName, "PO")
        }
    }

    fun rejectPurchaseOrder(poId: String, approverName: String, remarks: String) {
        val currentList = _purchaseOrders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == poId }
        if (index != -1) {
            val oldPo = currentList[index]
            val updated = oldPo.copy(
                status = POStatus.REJECTED,
                approvedBy = approverName,
                approvalRemarks = remarks.ifBlank { "Rejected by $approverName" },
                approvalDate = dateFormat.format(Date())
            )
            currentList[index] = updated
            _purchaseOrders.value = currentList
            addAudit("PO_REJECTED", "PO ${oldPo.poNumber} REJECTED by $approverName. Reason: $remarks", approverName, "PO")
        }
    }

    fun closePurchaseOrder(poId: String, closerName: String, reason: String) {
        val currentList = _purchaseOrders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == poId }
        if (index != -1) {
            val oldPo = currentList[index]
            val updated = oldPo.copy(
                status = POStatus.CLOSED_BY_CREATOR,
                closedReason = reason.ifBlank { "Closed by PO Creator" }
            )
            currentList[index] = updated
            _purchaseOrders.value = currentList
            addAudit("PO_CLOSED", "PO ${oldPo.poNumber} manually CLOSED by $closerName. Reason: $reason", closerName, "PO")
        }
    }

    // --- GRN (GOODS RECEIVED NOTE) AGAINST PO & BARCODE STOCK INWARD ---

    fun processGRN(
        po: PurchaseOrder,
        receivedItems: List<GRNLineItem>,
        receivedBy: String,
        vehicleNo: String,
        invoiceNo: String,
        remarks: String
    ): GRN {
        val grnNum = "GRN-2026-${String.format("%04d", _grnList.value.size + 10)}"
        val grn = GRN(
            grnNumber = grnNum,
            poNumber = po.poNumber,
            vendorName = po.vendorName,
            receivedBy = receivedBy,
            receivedDate = dateFormat.format(Date()),
            items = receivedItems,
            vehicleNumber = vehicleNo.ifBlank { "DL-01-AB-1234" },
            invoiceNumber = invoiceNo.ifBlank { "INV-AUTO-${Random.nextInt(1000, 9999)}" },
            remarks = remarks
        )
        _grnList.value = listOf(grn) + _grnList.value

        // Inward into Batches
        val newBatches = receivedItems.map { item ->
            InventoryBatch(
                batchNumber = item.batchNumber,
                itemId = item.itemId,
                itemName = item.itemName,
                eanCode = item.eanCode,
                quantity = item.receivedQty,
                unit = item.unit,
                location = "Store: ${item.putAwayLocation}",
                inKitchen = false,
                mfgDate = item.mfgDate,
                expiryDate = item.expiryDate,
                grnNumber = grnNum,
                poNumber = po.poNumber
            )
        }
        _batches.value = _batches.value + newBatches

        // Update PO item received counts and check if all received
        val currentPOs = _purchaseOrders.value.toMutableList()
        val poIndex = currentPOs.indexOfFirst { it.id == po.id }
        if (poIndex != -1) {
            val targetPO = currentPOs[poIndex]
            val updatedPOLineItems = targetPO.items.map { poItem ->
                val matchingReceived = receivedItems.find { it.itemId == poItem.itemId }
                if (matchingReceived != null) {
                    poItem.copy(receivedQty = poItem.receivedQty + matchingReceived.receivedQty)
                } else {
                    poItem
                }
            }
            val allComplete = updatedPOLineItems.all { it.receivedQty >= it.orderedQty }
            val newStatus = if (allComplete) POStatus.COMPLETED else POStatus.PARTIALLY_RECEIVED
            currentPOs[poIndex] = targetPO.copy(
                items = updatedPOLineItems,
                status = newStatus
            )
            _purchaseOrders.value = currentPOs
        }

        addAudit("GRN_INWARD", "GRN $grnNum generated against ${po.poNumber}. Inwarded ${receivedItems.sumOf { it.receivedQty }} units with EAN barcodes", receivedBy, "GRN")
        return grn
    }

    // --- KITCHEN ISSUE (STORE -> KITCHEN WITH BATCH TRACKING) ---

    fun issueToKitchen(
        item: ItemMaster,
        sourceBatch: InventoryBatch,
        quantityToIssue: Double,
        kitchenStation: String,
        issuedBy: String
    ): Boolean {
        if (sourceBatch.quantity < quantityToIssue) return false

        val issueNum = "KIT-ISSUE-2026-${String.format("%04d", _kitchenIssues.value.size + 1)}"
        val kitchenIssue = KitchenIssue(
            issueNumber = issueNum,
            date = dateFormat.format(Date()),
            requestedBy = issuedBy,
            kitchenStation = kitchenStation,
            itemId = item.id,
            itemName = item.name,
            eanCode = item.eanCode,
            quantity = quantityToIssue,
            unit = item.unit,
            sourceBatchNumber = sourceBatch.batchNumber,
            sourceLocation = sourceBatch.location
        )
        _kitchenIssues.value = listOf(kitchenIssue) + _kitchenIssues.value

        // Deduct from Store Batch
        val currentBatches = _batches.value.toMutableList()
        val batchIdx = currentBatches.indexOfFirst { it.id == sourceBatch.id }
        if (batchIdx != -1) {
            val updatedStoreBatch = sourceBatch.copy(
                quantity = sourceBatch.quantity - quantityToIssue
            )
            if (updatedStoreBatch.quantity <= 0.001) {
                currentBatches.removeAt(batchIdx)
            } else {
                currentBatches[batchIdx] = updatedStoreBatch
            }
        }

        // Add to Kitchen Batch
        val kitchenBatch = InventoryBatch(
            batchNumber = "${sourceBatch.batchNumber}-K",
            itemId = item.id,
            itemName = item.name,
            eanCode = item.eanCode,
            quantity = quantityToIssue,
            unit = item.unit,
            location = "Kitchen: $kitchenStation",
            inKitchen = true,
            mfgDate = sourceBatch.mfgDate,
            expiryDate = sourceBatch.expiryDate,
            grnNumber = sourceBatch.grnNumber,
            poNumber = sourceBatch.poNumber
        )
        currentBatches.add(kitchenBatch)
        _batches.value = currentBatches

        addAudit("KITCHEN_ISSUE", "$issueNum: Issued $quantityToIssue ${item.unit.symbol} of ${item.name} (Batch: ${sourceBatch.batchNumber}) to $kitchenStation", issuedBy, "KITCHEN")

        // Check Store Total Stock for this item -> trigger email if below threshold
        checkLowStockAlert(item)

        return true
    }

    // --- KITCHEN PRODUCTION -> FG (FINISHED GOODS) & EAN BARCODE GENERATION ---

    fun recordKitchenProduction(
        fgItem: ItemMaster,
        producedQty: Double,
        chefName: String,
        kitchenStation: String,
        consumedRawBatchSummary: String,
        bestBeforeDays: Int = 3
    ): ProductionLog {
        val prodNum = "PROD-2026-${String.format("%04d", _productionLogs.value.size + 1)}"
        val fgBatchNum = "FG-BAT-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}-${Random.nextInt(100, 999)}"
        
        // Auto-generate unique 13-digit EAN code for this FG batch if needed or use FG EAN
        val fgEan = generateEan13("890" + String.format("%09d", Random.nextInt(100000000, 999999999)))

        val expiryCalendar = java.util.Calendar.getInstance()
        expiryCalendar.add(java.util.Calendar.DAY_OF_YEAR, bestBeforeDays)
        val expiryStr = dateOnlyFormat.format(expiryCalendar.time)

        val prodLog = ProductionLog(
            productionNumber = prodNum,
            date = dateFormat.format(Date()),
            fgItemId = fgItem.id,
            fgName = fgItem.name,
            fgEanCode = fgEan,
            fgBatchNumber = fgBatchNum,
            producedQty = producedQty,
            unit = fgItem.unit,
            chefName = chefName,
            kitchenStation = kitchenStation,
            consumedBatchSummary = consumedRawBatchSummary,
            expiryDate = expiryStr
        )
        _productionLogs.value = listOf(prodLog) + _productionLogs.value

        // Inward Finished Good into FG Inventory
        val fgBatch = InventoryBatch(
            batchNumber = fgBatchNum,
            itemId = fgItem.id,
            itemName = fgItem.name,
            eanCode = fgEan,
            quantity = producedQty,
            unit = fgItem.unit,
            location = "FG Stock: Cold Display / Dispenser",
            inKitchen = false,
            mfgDate = dateOnlyFormat.format(Date()),
            expiryDate = expiryStr,
            grnNumber = "PROD-INTERNAL",
            poNumber = "KITCHEN-PROD"
        )
        _batches.value = _batches.value + fgBatch

        addAudit("FG_PRODUCED", "Kitchen produced $producedQty ${fgItem.unit.symbol} of ${fgItem.name}. EAN Barcode: $fgEan | Batch: $fgBatchNum", chefName, "FG")
        return prodLog
    }

    // --- LOW STOCK ALERT & AUTO EMAIL GENERATION ---

    fun checkLowStockAlert(item: ItemMaster) {
        val currentStoreStock = _batches.value
            .filter { it.itemId == item.id && !it.inKitchen }
            .sumOf { it.quantity }

        if (currentStoreStock <= item.minThreshold) {
            triggerLowStockEmail(item, currentStoreStock)
        }
    }

    private fun triggerLowStockEmail(item: ItemMaster, currentStock: Double) {
        val subject = "🚨 AUTO LOW STOCK ALERT: ${item.name} (${String.format("%.1f", currentStock)} ${item.unit.symbol} remaining)"
        val alert = EmailAlert(
            timestamp = dateFormat.format(Date()),
            toEmail = "purchase@easycomstore.com, store.head@cloudkitchen.com",
            subject = subject,
            itemId = item.id,
            itemName = item.name,
            currentStock = currentStock,
            minThreshold = item.minThreshold,
            unit = item.unit,
            suggestedPOQty = item.reorderQty,
            isAutoTriggered = true
        )
        _emailAlerts.value = listOf(alert) + _emailAlerts.value
        addAudit("EMAIL_ALERT", "Auto-dispatched low stock email for ${item.name} (Stock: $currentStock ${item.unit.symbol} <= Min: ${item.minThreshold})", "AutoNotifier", "STOCK")
    }

    // Helper: Valid EAN-13 generation with Checksum
    fun generateEan13(prefix12: String): String {
        val clean12 = prefix12.filter { it.isDigit() }.take(12).padEnd(12, '0')
        var sum = 0
        for (i in 0 until 12) {
            val digit = clean12[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val checksum = (10 - (sum % 10)) % 10
        return clean12 + checksum
    }

    // --- 1. ITEM / SKU MASTER CRUD ---
    fun addItem(item: ItemMaster) {
        _items.value = _items.value + item
        addAudit("ITEM_CREATED", "New SKU created: ${item.name} (${item.sku})", _currentUser.value.name, "ADMIN")
    }

    fun updateItem(item: ItemMaster) {
        val current = _items.value.toMutableList()
        val idx = current.indexOfFirst { it.id == item.id }
        if (idx != -1) {
            current[idx] = item
            _items.value = current
            addAudit("ITEM_UPDATED", "SKU updated: ${item.name} (${item.sku})", _currentUser.value.name, "ADMIN")
        }
    }

    fun deleteItem(itemId: String): Boolean {
        val item = _items.value.find { it.id == itemId } ?: return false
        _items.value = _items.value.filter { it.id != itemId }
        addAudit("ITEM_DELETED", "SKU deleted from catalog: ${item.name} (${item.sku})", _currentUser.value.name, "ADMIN")
        return true
    }

    // --- 2. VENDOR MASTER CRUD ---
    fun addVendor(vendor: Vendor) {
        _vendors.value = _vendors.value + vendor
        addAudit("VENDOR_CREATED", "New Vendor registered: ${vendor.name} (${vendor.code})", _currentUser.value.name, "ADMIN")
    }

    fun updateVendor(vendor: Vendor) {
        val current = _vendors.value.toMutableList()
        val idx = current.indexOfFirst { it.id == vendor.id }
        if (idx != -1) {
            current[idx] = vendor
            _vendors.value = current
            addAudit("VENDOR_UPDATED", "Vendor updated: ${vendor.name} (${vendor.code})", _currentUser.value.name, "ADMIN")
        }
    }

    fun deleteVendor(vendorId: String): Boolean {
        val v = _vendors.value.find { it.id == vendorId } ?: return false
        _vendors.value = _vendors.value.filter { it.id != vendorId }
        addAudit("VENDOR_DELETED", "Vendor removed: ${v.name}", _currentUser.value.name, "ADMIN")
        return true
    }

    // --- 3. BOM / RECIPE CRUD ---
    fun addRecipe(recipe: Recipe) {
        _recipes.value = _recipes.value + recipe
        addAudit("RECIPE_CREATED", "New Recipe BOM configured for ${recipe.fgName}", _currentUser.value.name, "ADMIN")
    }

    fun updateRecipe(recipe: Recipe) {
        val current = _recipes.value.toMutableList()
        val idx = current.indexOfFirst { it.id == recipe.id }
        if (idx != -1) {
            current[idx] = recipe
            _recipes.value = current
            addAudit("RECIPE_UPDATED", "Recipe BOM updated for ${recipe.fgName}", _currentUser.value.name, "ADMIN")
        }
    }

    fun deleteRecipe(recipeId: String): Boolean {
        val r = _recipes.value.find { it.id == recipeId } ?: return false
        _recipes.value = _recipes.value.filter { it.id != recipeId }
        addAudit("RECIPE_DELETED", "Recipe BOM deleted for ${r.fgName}", _currentUser.value.name, "ADMIN")
        return true
    }

    // --- 4. USER DIRECTORY & GRANULAR RBAC ACCESS CONTROL CRUD ---
    fun addUser(user: AppUser) {
        _users.value = _users.value + user
        addAudit("USER_CREATED", "New user registered: ${user.name} (${user.email}) - ${user.role.name} [${user.permissionLevel.label}]", _currentUser.value.name, "SECURITY")
    }

    fun updateUser(user: AppUser) {
        val current = _users.value.toMutableList()
        val idx = current.indexOfFirst { it.id == user.id }
        if (idx != -1) {
            current[idx] = user
            _users.value = current
            if (_currentUser.value.id == user.id) {
                _currentUser.value = user
                _currentRole.value = user.role
            }
            addAudit("USER_UPDATED", "User access updated: ${user.name} (${user.email}) -> Role: ${user.role.name}, Access: ${user.permissionLevel.label}, Status: ${if (user.isActive) "Active" else "Suspended"}", _currentUser.value.name, "SECURITY")
        }
    }

    fun toggleUserStatus(userId: String) {
        val current = _users.value.toMutableList()
        val idx = current.indexOfFirst { it.id == userId }
        if (idx != -1) {
            val u = current[idx]
            val updated = u.copy(isActive = !u.isActive)
            current[idx] = updated
            _users.value = current
            val statusWord = if (updated.isActive) "ACTIVATED" else "SUSPENDED / REVOKED"
            addAudit("USER_STATUS_CHANGE", "User access $statusWord for ${u.name} (${u.email})", _currentUser.value.name, "SECURITY")
        }
    }

    fun deleteUser(userId: String): Boolean {
        val u = _users.value.find { it.id == userId } ?: return false
        if (u.id == _currentUser.value.id) return false
        _users.value = _users.value.filter { it.id != userId }
        addAudit("USER_DELETED", "User removed: ${u.name} (${u.email})", _currentUser.value.name, "SECURITY")
        return true
    }

    fun switchActiveUser(user: AppUser) {
        _currentUser.value = user
        _currentRole.value = user.role
        addAudit("SESSION_SWITCH", "Session switched to ${user.name} (${user.email}) | ${user.role.name} [${user.permissionLevel.label}]", user.name, "AUTH")
    }

    // --- 5. LOCATION / WAREHOUSE BINS CRUD ---
    fun addLocation(loc: LocationMaster) {
        _locations.value = _locations.value + loc
        addAudit("LOC_CREATED", "Storage Bin created: ${loc.name} (${loc.code})", _currentUser.value.name, "ADMIN")
    }

    fun updateLocation(loc: LocationMaster) {
        val current = _locations.value.toMutableList()
        val idx = current.indexOfFirst { it.id == loc.id }
        if (idx != -1) {
            current[idx] = loc
            _locations.value = current
            addAudit("LOC_UPDATED", "Storage Bin updated: ${loc.name} (${loc.code})", _currentUser.value.name, "ADMIN")
        }
    }

    fun deleteLocation(locId: String): Boolean {
        val l = _locations.value.find { it.id == locId } ?: return false
        _locations.value = _locations.value.filter { it.id != locId }
        addAudit("LOC_DELETED", "Storage Bin removed: ${l.name}", _currentUser.value.name, "ADMIN")
        return true
    }

    // --- DISPATCH MANAGEMENT (FINISHED GOODS DISPATCH / SALES) ---

    fun recordDispatch(
        fgItem: ItemMaster,
        sourceBatch: InventoryBatch,
        quantity: Double,
        channel: String,
        invoice: String,
        dispatchedBy: String
    ): Boolean {
        if (sourceBatch.quantity < quantity) return false

        val dispNum = "DISP-2026-${String.format("%04d", _dispatches.value.size + 1)}"
        val record = DispatchRecord(
            dispatchNumber = dispNum,
            date = dateFormat.format(Date()),
            fgItemId = fgItem.id,
            fgName = fgItem.name,
            eanCode = fgItem.eanCode,
            batchNumber = sourceBatch.batchNumber,
            quantity = quantity,
            unit = fgItem.unit,
            customerOrChannel = channel.ifBlank { "Direct Counter Sale" },
            invoiceNumber = invoice.ifBlank { "INV-${Random.nextInt(1000, 9999)}" },
            dispatchedBy = dispatchedBy
        )
        _dispatches.value = listOf(record) + _dispatches.value

        // Deduct from FG batch
        val currentBatches = _batches.value.toMutableList()
        val idx = currentBatches.indexOfFirst { it.id == sourceBatch.id }
        if (idx != -1) {
            val updated = sourceBatch.copy(quantity = sourceBatch.quantity - quantity)
            if (updated.quantity <= 0.001) {
                currentBatches.removeAt(idx)
            } else {
                currentBatches[idx] = updated
            }
            _batches.value = currentBatches
        }

        addAudit("DISPATCH", "$dispNum: Dispatched $quantity ${fgItem.unit.symbol} of ${fgItem.name} (Batch: ${sourceBatch.batchNumber}) to $channel", dispatchedBy, "DISPATCH")
        return true
    }

    // --- 360° MASTER INVENTORY & MOVEMENT LEDGER ---

    fun generateFullLedger(): List<ItemBatchLedgerRow> {
        val ledgerRows = mutableListOf<ItemBatchLedgerRow>()
        val allItems = _items.value
        val allBatches = _batches.value
        val allPOs = _purchaseOrders.value
        val allIssues = _kitchenIssues.value
        val allProds = _productionLogs.value
        val allDispatches = _dispatches.value

        for (item in allItems) {
            val itemBatches = allBatches.filter { it.itemId == item.id }

            // Aggregate totals for the SKU
            val poOrdered = allPOs.flatMap { it.items }.filter { it.itemId == item.id }.sumOf { it.orderedQty }
            val grnReceived = allPOs.flatMap { it.items }.filter { it.itemId == item.id }.sumOf { it.receivedQty }
            val kitchenIssued = allIssues.filter { it.itemId == item.id }.sumOf { it.quantity }
            val fgProduced = allProds.filter { it.fgItemId == item.id }.sumOf { it.producedQty }
            val dispatched = allDispatches.filter { it.fgItemId == item.id }.sumOf { it.quantity }

            if (itemBatches.isEmpty()) {
                ledgerRows.add(
                    ItemBatchLedgerRow(
                        sku = item.sku,
                        itemName = item.name,
                        eanCode = item.eanCode,
                        category = item.category,
                        isFinishedGood = item.isFinishedGood,
                        batchNumber = "NO-ACTIVE-BATCH",
                        mfgDate = "-",
                        expiryDate = "-",
                        location = item.rackLocation,
                        unit = item.unit,
                        poOrderedQty = poOrdered,
                        grnReceivedQty = grnReceived,
                        kitchenIssuedQty = kitchenIssued,
                        fgProducedQty = fgProduced,
                        dispatchedQty = dispatched,
                        currentStockBalance = 0.0,
                        poNumber = "-",
                        grnNumber = "-"
                    )
                )
            } else {
                for (b in itemBatches) {
                    val batchKitchenIssued = allIssues.filter { it.sourceBatchNumber == b.batchNumber }.sumOf { it.quantity }
                    val batchDispatched = allDispatches.filter { it.batchNumber == b.batchNumber }.sumOf { it.quantity }
                    val batchProduced = allProds.filter { it.fgBatchNumber == b.batchNumber }.sumOf { it.producedQty }

                    ledgerRows.add(
                        ItemBatchLedgerRow(
                            sku = item.sku,
                            itemName = item.name,
                            eanCode = if (b.eanCode.isNotBlank()) b.eanCode else item.eanCode,
                            category = item.category,
                            isFinishedGood = item.isFinishedGood,
                            batchNumber = b.batchNumber,
                            mfgDate = b.mfgDate,
                            expiryDate = b.expiryDate,
                            location = b.location,
                            unit = b.unit,
                            poOrderedQty = poOrdered,
                            grnReceivedQty = grnReceived,
                            kitchenIssuedQty = if (batchKitchenIssued > 0) batchKitchenIssued else kitchenIssued,
                            fgProducedQty = if (batchProduced > 0) batchProduced else fgProduced,
                            dispatchedQty = if (batchDispatched > 0) batchDispatched else dispatched,
                            currentStockBalance = b.quantity,
                            poNumber = b.poNumber,
                            grnNumber = b.grnNumber
                        )
                    )
                }
            }
        }
        return ledgerRows
    }

    // Export to CSV String
    fun exportLedgerToCSV(): String {
        val rows = generateFullLedger()
        val sb = StringBuilder()
        sb.append("SKU,Item Name,EAN-13 Barcode,Type,Category,Batch Number,Mfg Date,Expiry Date,Current Location,PO Ordered Qty,GRN Received Qty,Kitchen Issued Qty,FG Produced Qty,Dispatched Qty,Current Stock Balance,Unit,Linked PO#,Linked GRN#\n")

        for (r in rows) {
            val typeStr = if (r.isFinishedGood) "Finished Good (FG)" else "Raw Material"
            sb.append("\"${r.sku}\",")
            sb.append("\"${r.itemName.replace("\"", "\"\"")}\",")
            sb.append("\"${r.eanCode}\",")
            sb.append("\"$typeStr\",")
            sb.append("\"${r.category.displayName}\",")
            sb.append("\"${r.batchNumber}\",")
            sb.append("\"${r.mfgDate}\",")
            sb.append("\"${r.expiryDate}\",")
            sb.append("\"${r.location}\",")
            sb.append("${r.poOrderedQty},")
            sb.append("${r.grnReceivedQty},")
            sb.append("${r.kitchenIssuedQty},")
            sb.append("${r.fgProducedQty},")
            sb.append("${r.dispatchedQty},")
            sb.append("${r.currentStockBalance},")
            sb.append("\"${r.unit.symbol}\",")
            sb.append("\"${r.poNumber}\",")
            sb.append("\"${r.grnNumber}\"\n")
        }
        return sb.toString()
    }

    fun updateSystemSettings(settings: SystemSettings) {
        _systemSettings.value = settings
        addAudit("SETTINGS_UPDATED", "System control panel parameters updated for company ${settings.companyName}", _currentUser.value.name, "CONFIG")
    }

    fun resetDemoData() {
        seedInitialData()
        _systemSettings.value = SystemSettings()
        addAudit("DEMO_RESET", "Demo system restored to initial seed state", "Admin", "SYSTEM")
    }
}
