package com.example.easycom.model

import java.util.UUID

enum class ItemCategory(val displayName: String) {
    RAW_MATERIAL("Raw Material"),
    DAIRY("Dairy & Cold"),
    GRAIN_FLOUR("Grains & Flour"),
    SPICES_SAUCES("Spices & Sauces"),
    PACKAGING("Packaging"),
    FINISHED_GOOD("Finished Good (FG)")
}

enum class StockUnit(val symbol: String) {
    KG("kg"),
    LITER("L"),
    GRAM("g"),
    ML("ml"),
    PCS("pcs"),
    PACKET("pkt"),
    BOX("box")
}

data class ItemMaster(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sku: String,
    val eanCode: String,
    val category: ItemCategory,
    val unit: StockUnit,
    val minThreshold: Double,
    val reorderQty: Double,
    val costPrice: Double,
    val sellingPrice: Double = costPrice * 1.4,
    val description: String = "",
    val isFinishedGood: Boolean = false,
    val rackLocation: String = "Rack A-01"
)

data class InventoryBatch(
    val id: String = UUID.randomUUID().toString(),
    val batchNumber: String,
    val itemId: String,
    val itemName: String,
    val eanCode: String,
    val quantity: Double,
    val unit: StockUnit,
    val location: String, // e.g. "Store: Rack A-12", "Kitchen Cold Room"
    val inKitchen: Boolean = false,
    val mfgDate: String,
    val expiryDate: String,
    val grnNumber: String = "",
    val poNumber: String = ""
)

enum class POStatus(val label: String, val colorHex: Long) {
    PENDING_APPROVAL("Pending Approval", 0xFFF57C00), // Amber/Orange
    APPROVED("Approved - Ready for GRN", 0xFF2E7D32),  // Green
    REJECTED("Rejected", 0xFFC62828),                  // Red
    PARTIALLY_RECEIVED("Partially Received", 0xFF0288D1), // Cyan/Blue
    COMPLETED("Completed (GRN Done)", 0xFF1B5E20),     // Dark Green
    CLOSED_BY_CREATOR("Closed by Creator", 0xFF546E7A), // Blue Grey
    CANCELLED("Cancelled", 0xFF757575)                 // Grey
}

data class POLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val itemName: String,
    val sku: String,
    val eanCode: String,
    val unit: StockUnit,
    val orderedQty: Double,
    val receivedQty: Double = 0.0,
    val unitPrice: Double,
    val taxPercent: Double = 5.0
) {
    val totalAmount: Double
        get() = orderedQty * unitPrice * (1 + taxPercent / 100.0)
}

data class PurchaseOrder(
    val id: String = UUID.randomUUID().toString(),
    val poNumber: String,
    val vendorId: String,
    val vendorName: String,
    val creatorName: String,
    val creatorRole: String = "Purchase Officer",
    val createdDate: String,
    val expectedDeliveryDate: String,
    val items: List<POLineItem>,
    val status: POStatus = POStatus.PENDING_APPROVAL,
    val approvedBy: String? = null,
    val approvalRemarks: String? = null,
    val approvalDate: String? = null,
    val notes: String = "",
    val closedReason: String? = null
) {
    val totalOrderValue: Double
        get() = items.sumOf { it.totalAmount }
    
    val totalOrderedQty: Double
        get() = items.sumOf { it.orderedQty }

    val totalReceivedQty: Double
        get() = items.sumOf { it.receivedQty }
}

data class GRNLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val itemName: String,
    val sku: String,
    val eanCode: String,
    val orderedQty: Double,
    val receivedQty: Double,
    val rejectedQty: Double = 0.0,
    val unit: StockUnit,
    val batchNumber: String,
    val mfgDate: String,
    val expiryDate: String,
    val putAwayLocation: String
)

data class GRN(
    val id: String = UUID.randomUUID().toString(),
    val grnNumber: String,
    val poNumber: String,
    val vendorName: String,
    val receivedBy: String,
    val receivedDate: String,
    val items: List<GRNLineItem>,
    val vehicleNumber: String = "DL-01-AB-1234",
    val invoiceNumber: String = "INV-9921",
    val remarks: String = "All seals intact and temperature verified."
)

data class KitchenIssue(
    val id: String = UUID.randomUUID().toString(),
    val issueNumber: String,
    val date: String,
    val requestedBy: String,
    val kitchenStation: String, // "Bakery Section", "Main Hot Kitchen", "Prep Counter"
    val itemId: String,
    val itemName: String,
    val eanCode: String,
    val quantity: Double,
    val unit: StockUnit,
    val sourceBatchNumber: String,
    val sourceLocation: String
)

data class RecipeIngredient(
    val itemId: String,
    val itemName: String,
    val requiredQtyPerUnit: Double,
    val unit: StockUnit
)

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val fgItemId: String,
    val fgName: String,
    val yieldUnit: StockUnit = StockUnit.PCS,
    val ingredients: List<RecipeIngredient>
)

data class ProductionLog(
    val id: String = UUID.randomUUID().toString(),
    val productionNumber: String,
    val date: String,
    val fgItemId: String,
    val fgName: String,
    val fgEanCode: String,
    val fgBatchNumber: String,
    val producedQty: Double,
    val unit: StockUnit,
    val chefName: String,
    val kitchenStation: String,
    val consumedBatchSummary: String, // e.g. "Amul Butter (Batch AM-01): 5kg, Maida (Batch MD-02): 10kg"
    val expiryDate: String
)

data class EmailAlert(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val toEmail: String,
    val subject: String,
    val itemId: String,
    val itemName: String,
    val currentStock: Double,
    val minThreshold: Double,
    val unit: StockUnit,
    val suggestedPOQty: Double,
    val isAutoTriggered: Boolean = true
)

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val action: String,
    val details: String,
    val performedBy: String,
    val tag: String // "PO", "GRN", "STOCK", "KITCHEN", "FG"
)

enum class UserRole(val title: String, val badgeColor: Long) {
    ADMIN("Admin (All Access)", 0xFF6200EA),
    PURCHASE_MANAGER("Purchase Manager (Create & Close PO)", 0xFF1976D2),
    APPROVER("Finance / Director (Approve PO)", 0xFF00796B),
    STORE_KEEPER("Store Keeper (GRN & Inward & Scan)", 0xFFF57C00),
    KITCHEN_HEAD("Kitchen Head / Chef (Requisition & FG)", 0xFFD81B60)
}

data class Vendor(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val code: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val gstin: String,
    val paymentTerms: String = "Net 30 Days"
)

data class DispatchRecord(
    val id: String = UUID.randomUUID().toString(),
    val dispatchNumber: String,
    val date: String,
    val fgItemId: String,
    val fgName: String,
    val eanCode: String,
    val batchNumber: String,
    val quantity: Double,
    val unit: StockUnit,
    val customerOrChannel: String,
    val invoiceNumber: String,
    val dispatchedBy: String
)

data class ItemBatchLedgerRow(
    val id: String = UUID.randomUUID().toString(),
    val sku: String,
    val itemName: String,
    val eanCode: String,
    val category: ItemCategory,
    val isFinishedGood: Boolean,
    val batchNumber: String,
    val mfgDate: String,
    val expiryDate: String,
    val location: String,
    val unit: StockUnit,
    val poOrderedQty: Double,
    val grnReceivedQty: Double,
    val kitchenIssuedQty: Double,
    val fgProducedQty: Double,
    val dispatchedQty: Double,
    val currentStockBalance: Double,
    val poNumber: String = "",
    val grnNumber: String = ""
)

enum class PermissionLevel(val label: String, val badgeColor: Long) {
    FULL_EDIT("Edit Access (Read/Write)", 0xFF2E7D32),
    VIEW_ONLY("View Only (Read)", 0xFF1565C0)
}

data class AppUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val role: UserRole,
    val permissionLevel: PermissionLevel = PermissionLevel.FULL_EDIT,
    val isActive: Boolean = true,
    val department: String = "Operations",
    val phone: String = "+91 98000 00000"
)

data class LocationMaster(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val name: String,
    val zone: String, // "Cold Storage (-18°C to 4°C)", "Dry Ambient Warehouse", "Kitchen Production", "Packaging Area"
    val capacity: String = "500 kg"
)

data class SystemSettings(
    val companyName: String = "EasyCom Foods & Cloud Kitchens ERP",
    val businessType: String = "Multi-Outlet Food & Beverage Supply Chain",
    val gstin: String = "07AAAAA0000A1Z5",
    val defaultWarehouse: String = "Central Cold Hub & Raw Store (Delhi NCR)",
    val primaryEmail: String = "rahulktiwari3009@gmail.com",
    val alertRecipients: String = "rahulktiwari3009@gmail.com, purchase.ops@easycom.com",
    val phone: String = "+91 98765 43210",
    val currencySymbol: String = "₹",
    // PO & Approval Rules
    val poApprovalThreshold: Double = 15000.0,
    val autoApproveBelowThreshold: Boolean = false,
    val allowCreatorToClosePO: Boolean = true,
    val poPrefix: String = "PO-2026-",
    // GRN & Store Inward Rules
    val strictPOMatching: Boolean = true,
    val allowOverReceivingPercent: Double = 5.0,
    val autoGenerateBatchOnInward: Boolean = true,
    val defaultPutawayBin: String = "Rack A-01 (General Ambient)",
    // Low-Stock Email Alerts
    val enableLowStockEmailAlerts: Boolean = true,
    val autoCreateDraftPOOnAlert: Boolean = true,
    val dailyMorningSummaryEmail: Boolean = true,
    // Kitchen & Production Rules
    val autoDeductKitchenBOM: Boolean = true,
    val allowPartialBatchIssue: Boolean = true,
    val defaultFGShelfLifeDays: Int = 14,
    val barcodeCountryPrefix: String = "890",
    // Tax Rates
    val defaultTaxPercent: Double = 5.0,
    val availableTaxSlabs: String = "0%, 5%, 12%, 18%, 28%"
)

