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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycom.model.*
import com.example.easycom.ui.EasyComViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: EasyComViewModel) {
    val items by viewModel.items.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val users by viewModel.users.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val systemSettings by viewModel.systemSettings.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) }
    // 0 = Staff & RBAC, 1 = Master SKUs, 2 = Vendors, 3 = Recipe BOM, 4 = Storage Bins, 5 = Edit Set, 6 = Web Blueprint, 7 = Audit Trail

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Admin Banner & Current Persona Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Admin Enterprise Control",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Full CRUD Masters • RBAC by Email • Blueprint",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.openSettings() },
                                modifier = Modifier.testTag("admin_header_edit_set_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Master Edit Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.openBlueprint() },
                                modifier = Modifier.testTag("open_blueprint_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Web Blueprint", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Logged In Persona Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Logged In As: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(currentUser.role.badgeColor).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = currentUser.role.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(currentUser.role.badgeColor),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Email: ${currentUser.email} • Dept: ${currentUser.department}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = Color(currentUser.permissionLevel.badgeColor).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (currentUser.permissionLevel == PermissionLevel.FULL_EDIT) Icons.Default.Edit else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color(currentUser.permissionLevel.badgeColor),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentUser.permissionLevel.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(currentUser.permissionLevel.badgeColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navigation Sub-Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = activeAdminTab,
                modifier = Modifier.padding(horizontal = 16.dp),
                edgePadding = 0.dp,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = activeAdminTab == 0,
                    onClick = { activeAdminTab = 0 },
                    text = { Text("Staff & RBAC (${users.size})", fontSize = 12.sp, fontWeight = if (activeAdminTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeAdminTab == 1,
                    onClick = { activeAdminTab = 1 },
                    text = { Text("Master SKUs (${items.size})", fontSize = 12.sp, fontWeight = if (activeAdminTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeAdminTab == 2,
                    onClick = { activeAdminTab = 2 },
                    text = { Text("Vendors (${vendors.size})", fontSize = 12.sp, fontWeight = if (activeAdminTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeAdminTab == 3,
                    onClick = { activeAdminTab = 3 },
                    text = { Text("Recipe BOM (${recipes.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeAdminTab == 4,
                    onClick = { activeAdminTab = 4 },
                    text = { Text("Bins & Storage (${locations.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeAdminTab == 5,
                    onClick = { activeAdminTab = 5 },
                    text = { Text("Edit Set / Config", fontSize = 12.sp, fontWeight = if (activeAdminTab == 5) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeAdminTab == 6,
                    onClick = { activeAdminTab = 6 },
                    text = { Text("Web Blueprint", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeAdminTab == 7,
                    onClick = { activeAdminTab = 7 },
                    text = { Text("Audit Trail (${auditLogs.size})", fontSize = 12.sp) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (activeAdminTab) {
            0 -> {
                // ==========================================
                // 0. STAFF DIRECTORY & RBAC BY EMAIL ID
                // ==========================================
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "User Access & Permissions by Email",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Admin can define roles, grant/revoke access, and configure Edit vs View Only permissions.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Button(
                                onClick = { viewModel.openEditUser(null) },
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("add_user_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add User", fontSize = 11.sp)
                            }
                        }
                    }
                }

                items(users, key = { it.id }) { user ->
                    val isCurrent = user.id == currentUser.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
                        else CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Name, Department, Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (user.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Active Session",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    color = if (user.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (user.isActive) "Access Granted" else "Access Revoked",
                                        color = if (user.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Email & Dept
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.email,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Department: ${user.department} • Phone: ${user.phone}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges: Role and Edit/View Permission
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(user.role.badgeColor).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = user.role.title,
                                        color = Color(user.role.badgeColor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = Color(user.permissionLevel.badgeColor).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (user.permissionLevel == PermissionLevel.FULL_EDIT) Icons.Default.Edit else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Color(user.permissionLevel.badgeColor),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = user.permissionLevel.label,
                                            color = Color(user.permissionLevel.badgeColor),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Grant / Revoke Access toggle
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = user.isActive,
                                        onCheckedChange = { viewModel.toggleUserStatus(user.id) },
                                        modifier = Modifier.height(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (user.isActive) "Active" else "Suspended",
                                        fontSize = 11.sp,
                                        color = if (user.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!isCurrent) {
                                        OutlinedButton(
                                            onClick = { viewModel.switchActiveUser(user) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Test As User", fontSize = 10.sp)
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = { viewModel.openEditUser(user) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Edit Role", fontSize = 10.sp)
                                    }

                                    if (!isCurrent) {
                                        IconButton(
                                            onClick = {
                                                viewModel.promptDelete(
                                                    EasyComViewModel.DeleteTarget.DeleteUser(user) {
                                                        viewModel.deleteUser(user.id)
                                                    }
                                                )
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // ==========================================
                // 1. MASTER SKU DIRECTORY (ADD / EDIT / DELETE)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Product Catalog & SKUs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Manage raw materials, packaging, and finished goods", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Button(
                            onClick = { viewModel.openEditSKU(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("admin_add_sku_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add SKU", fontSize = 11.sp)
                        }
                    }
                }

                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (item.isFinishedGood) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                                Text("FG", color = Color(0xFF2E7D32), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("SKU: ${item.sku} • EAN-13: ${item.eanCode}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("Bin: ${item.rackLocation} • Category: ${item.category.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${item.costPrice} / ${item.unit.symbol}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("SP: ₹${item.sellingPrice}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Safety Threshold: ${item.minThreshold} ${item.unit.symbol} | Auto-Reorder: ${item.reorderQty} ${item.unit.symbol}", fontSize = 10.sp, color = Color(0xFFE65100))

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))

                            // Action buttons: Edit & Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.openEditSKU(item) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit SKU", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        viewModel.promptDelete(
                                            EasyComViewModel.DeleteTarget.DeleteSKU(item) {
                                                viewModel.deleteSKU(item.id)
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // ==========================================
                // 2. VENDORS DIRECTORY (ADD / EDIT / DELETE)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vendors & Suppliers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Manage vendor contracts, contacts, and terms", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Button(
                            onClick = { viewModel.openEditVendor(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("admin_add_vendor_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Vendor", fontSize = 11.sp)
                        }
                    }
                }

                items(vendors, key = { it.id }) { v ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(v.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                                    Text(v.code, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Contact: ${v.contactPerson} • Phone: ${v.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Email: ${v.email}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.outline)
                            Text("GSTIN: ${v.gstin} • Terms: ${v.paymentTerms}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))

                            // Action buttons: Edit & Delete Vendor
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.openEditVendor(v) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Vendor", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        viewModel.promptDelete(
                                            EasyComViewModel.DeleteTarget.DeleteVendor(v) {
                                                viewModel.deleteVendor(v.id)
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // ==========================================
                // 3. RECIPE BOM (ADD / EDIT / DELETE)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bill of Materials (BOM) Recipes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Map raw materials consumed per finished product unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Button(
                            onClick = { viewModel.openEditRecipe(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("admin_add_recipe_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Recipe", fontSize = 11.sp)
                        }
                    }
                }

                items(recipes, key = { it.id }) { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BOM Recipe for: ${r.fgName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Required Raw Materials per 1 ${r.yieldUnit.symbol}:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            r.ingredients.forEach { ing ->
                                Text("  • ${ing.itemName}: ${ing.requiredQtyPerUnit} ${ing.unit.symbol}", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.openEditRecipe(r) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit BOM", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        viewModel.promptDelete(
                                            EasyComViewModel.DeleteTarget.DeleteRecipe(r) {
                                                viewModel.deleteRecipe(r.id)
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // ==========================================
                // 4. BINS & STORAGE LOCATIONS (ADD / EDIT / DELETE)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Storage Bins & Warehouses", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Configure temperature zones, racks, and capacities", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Button(
                            onClick = { viewModel.openEditLocation(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("admin_add_location_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Bin", fontSize = 11.sp)
                        }
                    }
                }

                items(locations, key = { it.id }) { loc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(loc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                                    Text(loc.code, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Zone: ${loc.zone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Max Safe Capacity: ${loc.capacity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.openEditLocation(loc) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Bin", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        viewModel.promptDelete(
                                            EasyComViewModel.DeleteTarget.DeleteLocation(loc) {
                                                viewModel.deleteLocation(loc.id)
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // ==========================================
                // 5. EDIT SET / SYSTEM SETTINGS CONTROL CENTER
                // ==========================================
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("System Parameters & Edit Set", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Active control settings for EasyCom ERP", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Button(
                                    onClick = { viewModel.openSettings() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("open_edit_set_full_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Set", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Parameters Grid Overview
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                BlueprintPill(
                                    title = "Organization Profile",
                                    desc = "${systemSettings.companyName} | GSTIN: ${systemSettings.gstin} | Hub: ${systemSettings.defaultWarehouse}"
                                )
                                BlueprintPill(
                                    title = "PO Approval Policy & Prefix",
                                    desc = "Prefix: ${systemSettings.poPrefix} | Director Approval Limit: ₹${systemSettings.poApprovalThreshold} | Creator Can Close: ${if (systemSettings.allowCreatorToClosePO) "Enabled" else "Disabled"}"
                                )
                                BlueprintPill(
                                    title = "Low-Stock Auto Email Notification",
                                    desc = "Recipients: ${systemSettings.alertRecipients} | Instant Dispatch: ${if (systemSettings.enableLowStockEmailAlerts) "Active" else "Off"} | Auto Draft PO: ${if (systemSettings.autoCreateDraftPOOnAlert) "Active" else "Off"}"
                                )
                                BlueprintPill(
                                    title = "Store Inward & Barcode Standards",
                                    desc = "Strict PO Match: ${if (systemSettings.strictPOMatching) "Enforced" else "Optional"} | Over-Receiving Buffer: ${systemSettings.allowOverReceivingPercent}% | Country Prefix: ${systemSettings.barcodeCountryPrefix}"
                                )
                                BlueprintPill(
                                    title = "Kitchen BOM Auto-Consumption",
                                    desc = "Auto-Deduct Raw Stock: ${if (systemSettings.autoDeductKitchenBOM) "Yes" else "No"} | Default FG Expiry: ${systemSettings.defaultFGShelfLifeDays} Days | GST Slabs: ${systemSettings.availableTaxSlabs}"
                                )
                            }
                        }
                    }
                }
            }

            6 -> {
                // ==========================================
                // 6. WEB ARCHITECTURE BLUEPRINT
                // ==========================================
                item {
                    BlueprintSummaryCard(
                        onOpenFull = { viewModel.openBlueprint() },
                        onResetDemo = { viewModel.resetDemo() }
                    )
                }
            }

            7 -> {
                // ==========================================
                // 7. AUDIT TRAIL LOGS
                // ==========================================
                items(auditLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = log.tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "By ${log.performedBy} • ${log.timestamp}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlueprintSummaryCard(
    onOpenFull: () -> Unit,
    onResetDemo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ERP Web Page Implementation Blueprint", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = onOpenFull,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Read Full Spec", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ye system pure Easyecom ke business logic par based hai. Demo me sab test karne ke baad aap is Blueprint ke schema & API structure ko copy karke directly React / Next.js web application me convert kar sakte hain:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            BlueprintPill(
                title = "1. PO & Approval Lifecycle",
                desc = "Maker-Checker pattern: Purchase Officer creates PO -> Finance/Director approves/rejects -> Locked for editing -> Creator can Close PO."
            )
            BlueprintPill(
                title = "2. Store GRN & EAN Scanning",
                desc = "Store inward against Approved PO only -> Laser scanner or camera EAN verification -> FIFO batch allotment -> Batch tag generation."
            )
            BlueprintPill(
                title = "3. Auto Low-Stock Email Alerts",
                desc = "Real-time trigger on store balance <= min_threshold -> Dispatches SMTP/SendGrid email to purchase & store head -> 1-click Auto-PO."
            )
            BlueprintPill(
                title = "4. Kitchen Issue & Batch Traceability",
                desc = "Store issues raw material batch to kitchen stations (Bakery, Pizza, Hot Kitchen) -> Exact batch deduction."
            )
            BlueprintPill(
                title = "5. FG Production & EAN Generation",
                desc = "Chef logs yield quantity -> Consumed batches linked -> System generates unique EAN-13 with checksum -> Inward to Finished Goods."
            )
            BlueprintPill(
                title = "6. Granular RBAC by Email ID & Edit/View Permissions",
                desc = "Admin defines roles per email address, toggles Full Edit vs View Only access, and can instantly revoke or grant access to any module."
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Test Data Management", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                OutlinedButton(
                    onClick = onResetDemo,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Demo Data", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BlueprintPill(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
