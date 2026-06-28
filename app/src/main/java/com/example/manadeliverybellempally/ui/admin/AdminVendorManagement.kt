package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.data.model.User
import com.example.manadeliverybellempally.data.model.Vendor
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVendorListScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val vendors by viewModel.allVendors.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredVendors = vendors.filter { it.storeName.contains(searchQuery, ignoreCase = true) }
    var showDetailVendorId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Partners", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            AdminSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Search shop name...")
            
            Spacer(Modifier.height(16.dp))
            
            Text("STORE DIRECTORY", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredVendors) { vendor ->
                    val user = users.find { it.id == vendor.id }
                    VendorAdminCard(
                        vendor = vendor,
                        user = user,
                        onAction = { showDetailVendorId = it }
                    )
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    if (showDetailVendorId != null) {
        val vid = showDetailVendorId!!
        val vendor = vendors.find { it.id == vid }
        val user = users.find { it.id == vid }
        if (vendor != null && user != null) {
            AdminVendorDetailDialog(
                vendor = vendor,
                user = user,
                onUpdateApproval = { viewModel.updateApproval(user, it) },
                onDismiss = { showDetailVendorId = null }
            )
        }
    }
}

@Composable
private fun VendorAdminCard(
    vendor: Vendor,
    user: User?,
    onAction: (String) -> Unit
) {
    ManaCard(modifier = Modifier.fillMaxWidth(), onClick = { onAction(vendor.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(vendor.storeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(vendor.categoryName, style = MaterialTheme.typography.labelSmall, color = ManaGold)
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminStatusBadge(
                        text = user?.approvalStatus ?: "PENDING",
                        color = when(user?.approvalStatus) {
                            "APPROVED" -> ManaSuccess
                            "REJECTED" -> ManaRed
                            else -> ManaWarning
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    if (vendor.isStoreOpen) {
                        Text("● LIVE", color = ManaSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    } else {
                        Text("● CLOSED", color = ManaTextTertiary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            IconButton(onClick = { onAction(vendor.id) }) {
                Icon(Icons.Rounded.Settings, null, tint = ManaGold)
            }
        }
    }
}

@Composable
private fun AdminVendorDetailDialog(
    vendor: Vendor,
    user: User,
    onUpdateApproval: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ManaBgCard,
        title = { 
            Column {
                Text(vendor.storeName, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                Text("Management Suite", style = MaterialTheme.typography.labelSmall, color = ManaGold)
            }
        },
        text = {
            LazyColumn(Modifier.fillMaxWidth()) {
                item {
                    AdminDetailRow(label = "Owner Name", value = user.name)
                    AdminDetailRow(label = "Phone", value = vendor.phone)
                    AdminDetailRow(label = "Commission", value = "${user.commissionRate}%")
                    AdminDetailRow(label = "Category", value = vendor.categoryName)
                    AdminDetailRow(label = "Rating", value = "${vendor.rating} ★ (${vendor.ratingCount})")
                    
                    Spacer(Modifier.height(24.dp))
                    Text("PARTNER APPROVAL", style = MaterialTheme.typography.labelMedium, color = ManaGold)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManaOutlinedButton(
                            text = "Reject",
                            onClick = { onUpdateApproval("REJECTED"); onDismiss() },
                            modifier = Modifier.weight(1f)
                        )
                        ManaButton(
                            text = "Approve",
                            onClick = { onUpdateApproval("APPROVED"); onDismiss() },
                            modifier = Modifier.weight(1f),
                            containerColor = ManaSuccess
                        )
                    }
                }
            }
        },
        confirmButton = { 
            TextButton(onClick = onDismiss) { Text("CLOSE", color = ManaTextTertiary, fontWeight = FontWeight.Bold) } 
        }
    )
}
