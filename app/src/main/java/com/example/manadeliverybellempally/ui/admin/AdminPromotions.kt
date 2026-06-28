package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.Coupon
import com.example.manadeliverybellempally.data.model.Banner
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponManagerScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val coupons by viewModel.coupons.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coupon Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Coupon")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (coupons.isEmpty()) {
            EmptyState(icon = Icons.Default.ConfirmationNumber, title = "No Coupons", subtitle = "Click + to create your first discount coupon.")
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(coupons) { coupon ->
                    CouponListItem(coupon = coupon)
                }
            }
        }
    }

    if (showAddDialog) {
        AddCouponDialog(
            onSave = { viewModel.saveCoupon(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun CouponListItem(coupon: Coupon) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(coupon.code.uppercase(), style = MaterialTheme.typography.titleMedium, color = ManaGold)
                Text(coupon.description, style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                Text(
                    text = if (coupon.discountType == "flat") "₹${coupon.discountValue} OFF" else "${coupon.discountValue}% OFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = ManaSuccess
                )
            }
            Switch(checked = coupon.isActive, onCheckedChange = {})
        }
    }
}

@Composable
private fun AddCouponDialog(
    onSave: (Coupon) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Coupon") },
        text = {
            Column {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") })
                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Discount Value") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(Coupon(code = code, discountValue = value.toDoubleOrNull() ?: 0.0, description = desc))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBannerManagerScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val banners by viewModel.banners.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banner Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Banner")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (banners.isEmpty()) {
            EmptyState(icon = Icons.Default.Image, title = "No Banners", subtitle = "Add promotional banners for the home screen.")
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(banners) { banner ->
                    BannerListItem(banner = banner)
                }
            }
        }
    }

    if (showAddDialog) {
        AddBannerDialog(
            onSave = { viewModel.saveBanner(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun BannerListItem(banner: Banner) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(banner.title, style = MaterialTheme.typography.titleMedium)
                Text("Action: ${banner.actionType}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
            }
            Switch(checked = banner.isActive, onCheckedChange = {})
        }
    }
}

@Composable
private fun AddBannerDialog(
    onSave: (Banner) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("category") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Banner") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Banner Title") })
                Text("Action Type: $actionType", style = MaterialTheme.typography.bodySmall)
                // Placeholder for more complex selection
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(Banner(title = title, actionType = actionType))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
