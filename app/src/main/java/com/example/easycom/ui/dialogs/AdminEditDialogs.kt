package com.example.easycom.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.easycom.model.*
import com.example.easycom.ui.EasyComViewModel
import kotlin.random.Random

// --- 1. EDIT / ADD SKU DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSKUDialog(viewModel: EasyComViewModel, initialItem: ItemMaster) {
    val isNew = initialItem.name.isBlank()

    var name by remember { mutableStateOf(initialItem.name) }
    var sku by remember { mutableStateOf(initialItem.sku) }
    var eanCode by remember { mutableStateOf(initialItem.eanCode) }
    var category by remember { mutableStateOf(initialItem.category) }
    var unit by remember { mutableStateOf(initialItem.unit) }
    var minThreshold by remember { mutableStateOf(initialItem.minThreshold.toString()) }
    var reorderQty by remember { mutableStateOf(initialItem.reorderQty.toString()) }
    var costPrice by remember { mutableStateOf(initialItem.costPrice.toString()) }
    var sellingPrice by remember { mutableStateOf(initialItem.sellingPrice.toString()) }
    var rackLocation by remember { mutableStateOf(initialItem.rackLocation) }
    var isFG by remember { mutableStateOf(initialItem.isFinishedGood) }
    var description by remember { mutableStateOf(initialItem.description) }

    Dialog(onDismissRequest = { viewModel.closeEditSKU() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("edit_sku_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isNew) "Add New Product / SKU" else "Edit Product Master",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isNew) "Create new SKU with EAN barcode and thresholds" else "Modify master specifications",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { viewModel.closeEditSKU() }) {
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
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product / Item Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU Code *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = eanCode,
                            onValueChange = { eanCode = it },
                            label = { Text("EAN-13 Barcode *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ItemCategory.values()) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = {
                                    category = cat
                                    if (cat == ItemCategory.FINISHED_GOOD) isFG = true
                                },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Unit of Measurement (UoM):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(StockUnit.values()) { u ->
                            FilterChip(
                                selected = unit == u,
                                onClick = { unit = u },
                                label = { Text("${u.name} (${u.symbol})", fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minThreshold,
                            onValueChange = { minThreshold = it },
                            label = { Text("Min Safety Stock") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reorderQty,
                            onValueChange = { reorderQty = it },
                            label = { Text("Auto Reorder Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costPrice,
                            onValueChange = { costPrice = it },
                            label = { Text("Cost Price (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = { Text("Selling Price (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = rackLocation,
                        onValueChange = { rackLocation = it },
                        label = { Text("Warehouse Rack / Storage Bin Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFG, onCheckedChange = { isFG = it })
                        Text("Is Kitchen Finished Good (Ready for Sale/Dispatch)", fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description & Quality Standards") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { viewModel.closeEditSKU() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && sku.isNotBlank()) {
                                val item = initialItem.copy(
                                    name = name,
                                    sku = sku,
                                    eanCode = eanCode.ifBlank { viewModel.repository.generateEan13("890" + Random.nextInt(100000000, 999999999)) },
                                    category = if (isFG) ItemCategory.FINISHED_GOOD else category,
                                    unit = unit,
                                    minThreshold = minThreshold.toDoubleOrNull() ?: 10.0,
                                    reorderQty = reorderQty.toDoubleOrNull() ?: 50.0,
                                    costPrice = costPrice.toDoubleOrNull() ?: 100.0,
                                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 150.0,
                                    rackLocation = rackLocation.ifBlank { "Rack A-01" },
                                    isFinishedGood = isFG,
                                    description = description
                                )
                                viewModel.saveSKU(item)
                            }
                        },
                        modifier = Modifier.testTag("save_sku_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isNew) "Create SKU" else "Save Changes")
                    }
                }
            }
        }
    }
}

// --- 2. EDIT / ADD VENDOR DIALOG ---
@Composable
fun EditVendorDialog(viewModel: EasyComViewModel, initialVendor: Vendor) {
    val isNew = initialVendor.name.isBlank()

    var name by remember { mutableStateOf(initialVendor.name) }
    var code by remember { mutableStateOf(initialVendor.code) }
    var contactPerson by remember { mutableStateOf(initialVendor.contactPerson) }
    var phone by remember { mutableStateOf(initialVendor.phone) }
    var email by remember { mutableStateOf(initialVendor.email) }
    var gstin by remember { mutableStateOf(initialVendor.gstin) }
    var paymentTerms by remember { mutableStateOf(initialVendor.paymentTerms) }

    Dialog(onDismissRequest = { viewModel.closeEditVendor() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("edit_vendor_dialog"),
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
                        Text(
                            text = if (isNew) "Register New Supplier" else "Edit Supplier Master",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Vendor profile, taxation & payment terms", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeEditVendor() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vendor / Company Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Vendor Code (e.g. VEN-01)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Contact Person") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paymentTerms,
                        onValueChange = { paymentTerms = it },
                        label = { Text("Payment Terms (e.g. Net 30)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { viewModel.closeEditVendor() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val v = initialVendor.copy(
                                    name = name,
                                    code = code.ifBlank { "VEN-${Random.nextInt(100, 999)}" },
                                    contactPerson = contactPerson,
                                    phone = phone,
                                    email = email,
                                    gstin = gstin,
                                    paymentTerms = paymentTerms
                                )
                                viewModel.saveVendor(v)
                            }
                        },
                        modifier = Modifier.testTag("save_vendor_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isNew) "Register Vendor" else "Save Vendor")
                    }
                }
            }
        }
    }
}

// --- 3. EDIT / ADD RECIPE BOM DIALOG ---
@Composable
fun EditRecipeDialog(viewModel: EasyComViewModel, initialRecipe: Recipe) {
    val items by viewModel.items.collectAsState()
    val isNew = initialRecipe.ingredients.isEmpty()

    val fgItems = items.filter { it.isFinishedGood }
    val rawItems = items.filter { !it.isFinishedGood }

    var selectedFG by remember { mutableStateOf(fgItems.find { it.id == initialRecipe.fgItemId } ?: fgItems.firstOrNull()) }
    var ingredientsList by remember { mutableStateOf(initialRecipe.ingredients) }

    var selectedRawToAdd by remember { mutableStateOf(rawItems.firstOrNull()) }
    var qtyToAddText by remember { mutableStateOf("0.1") }

    Dialog(onDismissRequest = { viewModel.closeEditRecipe() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("edit_recipe_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Configure BOM Recipe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Map raw materials required per 1 unit of Finished Good", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeEditRecipe() }) {
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
                    Text("Select Target Finished Good:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(fgItems) { fg ->
                            FilterChip(
                                selected = selectedFG?.id == fg.id,
                                onClick = { selectedFG = fg },
                                label = { Text(fg.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Ingredients in Recipe:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    ingredientsList.forEachIndexed { idx, ing ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ing.itemName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${ing.requiredQtyPerUnit} ${ing.unit.symbol}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { ingredientsList = ingredientsList.filterIndexed { i, _ -> i != idx } },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Add ingredient section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Add Raw Material to Recipe:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(rawItems) { r ->
                                    FilterChip(
                                        selected = selectedRawToAdd?.id == r.id,
                                        onClick = { selectedRawToAdd = r },
                                        label = { Text(r.name, fontSize = 10.sp) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = qtyToAddText,
                                    onValueChange = { qtyToAddText = it },
                                    label = { Text("Qty (${selectedRawToAdd?.unit?.symbol ?: "kg"})") },
                                    modifier = Modifier.width(130.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val itm = selectedRawToAdd
                                        val q = qtyToAddText.toDoubleOrNull() ?: 0.1
                                        if (itm != null) {
                                            ingredientsList = ingredientsList + RecipeIngredient(
                                                itemId = itm.id,
                                                itemName = itm.name,
                                                requiredQtyPerUnit = q,
                                                unit = itm.unit
                                            )
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 11.sp)
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
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { viewModel.closeEditRecipe() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val fg = selectedFG
                            if (fg != null && ingredientsList.isNotEmpty()) {
                                val r = initialRecipe.copy(
                                    fgItemId = fg.id,
                                    fgName = fg.name,
                                    ingredients = ingredientsList
                                )
                                viewModel.saveRecipe(r)
                            } else {
                                viewModel.showToast("Please add at least one ingredient")
                            }
                        },
                        modifier = Modifier.testTag("save_recipe_button")
                    ) {
                        Text("Save BOM Recipe")
                    }
                }
            }
        }
    }
}

// --- 4. EDIT / ADD USER & GRANULAR RBAC ACCESS DIALOG ---
@Composable
fun EditUserDialog(viewModel: EasyComViewModel, initialUser: AppUser) {
    val isNew = initialUser.name.isBlank()

    var name by remember { mutableStateOf(initialUser.name) }
    var email by remember { mutableStateOf(initialUser.email) }
    var role by remember { mutableStateOf(initialUser.role) }
    var permissionLevel by remember { mutableStateOf(initialUser.permissionLevel) }
    var department by remember { mutableStateOf(initialUser.department) }
    var phone by remember { mutableStateOf(initialUser.phone) }
    var isActive by remember { mutableStateOf(initialUser.isActive) }

    Dialog(onDismissRequest = { viewModel.closeEditUser() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("edit_user_dialog"),
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
                        Text(
                            text = if (isNew) "Add Team Member & Access" else "Manage User Permissions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Define role, email ID, and Edit vs View Only access", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeEditUser() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email ID * (e.g. rahulktiwari3009@gmail.com)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Role Assignment
                Text("Assigned ERP Role:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    UserRole.values().forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { role = r }
                        ) {
                            RadioButton(selected = role == r, onClick = { role = r })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(r.title, fontSize = 12.sp)
                        }
                    }
                }

                // Granular Permission Level (Edit vs View Only)
                Text("Granular Permission Level:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PermissionLevel.values().forEach { perm ->
                        FilterChip(
                            selected = permissionLevel == perm,
                            onClick = { permissionLevel = perm },
                            label = { Text(perm.label, fontSize = 11.sp) },
                            leadingIcon = {
                                if (perm == PermissionLevel.FULL_EDIT) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                } else {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }

                // Active / Revoke Access toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "Access Status: Active (Authorized)" else "Access Status: Suspended / Revoked",
                        color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { viewModel.closeEditUser() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && email.isNotBlank()) {
                                val u = initialUser.copy(
                                    name = name,
                                    email = email,
                                    role = role,
                                    permissionLevel = permissionLevel,
                                    department = department,
                                    phone = phone,
                                    isActive = isActive
                                )
                                viewModel.saveUser(u)
                            }
                        },
                        modifier = Modifier.testTag("save_user_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isNew) "Add User" else "Save Permissions")
                    }
                }
            }
        }
    }
}

// --- 5. EDIT / ADD LOCATION DIALOG ---
@Composable
fun EditLocationDialog(viewModel: EasyComViewModel, initialLoc: LocationMaster) {
    val isNew = initialLoc.name.isBlank()

    var name by remember { mutableStateOf(initialLoc.name) }
    var code by remember { mutableStateOf(initialLoc.code) }
    var zone by remember { mutableStateOf(initialLoc.zone) }
    var capacity by remember { mutableStateOf(initialLoc.capacity) }

    val zones = listOf("Cold Storage (-18°C to 4°C)", "Dry Ambient Warehouse", "Kitchen Prep Section", "Packaging Area", "Finished Goods Chiller")

    Dialog(onDismissRequest = { viewModel.closeEditLocation() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("edit_location_dialog"),
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
                        Text(
                            text = if (isNew) "Add Storage Bin / Location" else "Edit Storage Bin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Define warehouse racks and kitchen stations", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { viewModel.closeEditLocation() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Location Name * (e.g. Cold Room A-02)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Bin Code (e.g. COLD-A02)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("Temperature & Zone Classification:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(zones) { z ->
                        FilterChip(
                            selected = zone == z,
                            onClick = { zone = z },
                            label = { Text(z, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Capacity (e.g. 1000 kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = { viewModel.closeEditLocation() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val loc = initialLoc.copy(name = name, code = code, zone = zone, capacity = capacity)
                                viewModel.saveLocation(loc)
                            }
                        }
                    ) {
                        Text(if (isNew) "Create Bin" else "Save Bin")
                    }
                }
            }
        }
    }
}

// --- 6. GENERIC DELETE CONFIRMATION DIALOG ---
@Composable
fun ConfirmDeleteDialog(viewModel: EasyComViewModel, target: EasyComViewModel.DeleteTarget) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissDelete() },
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(32.dp)) },
        title = { Text(target.title, fontWeight = FontWeight.Bold) },
        text = { Text(target.message, fontSize = 13.sp) },
        confirmButton = {
            Button(
                onClick = { target.onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Confirm Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.dismissDelete() }) {
                Text("Cancel")
            }
        }
    )
}
