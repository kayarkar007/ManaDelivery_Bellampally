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
import com.example.manadeliverybellempally.data.model.Order
import com.example.manadeliverybellempally.data.model.User
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: String,
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()
    val riders by viewModel.allUsers.collectAsState()
    val order = orders.find { it.id == orderId } ?: return

    var showRiderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ManaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("ORDER ID", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                            Text("#${order.id.takeLast(8).uppercase()}", style = MaterialTheme.typography.titleLarge, color = ManaGold)
                        }
                        OrderStatusChip(status = order.status)
                    }
                }
            }

            item {
                Text("PARTIES", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
                Spacer(Modifier.height(8.dp))
                ManaCard(modifier = Modifier.fillMaxWidth()) {
                    AdminDetailRow(label = "Customer", value = order.userName, icon = Icons.Default.Person)
                    AdminDetailRow(label = "Phone", value = order.userPhone, icon = Icons.Default.Phone)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = ManaBorder)
                    AdminDetailRow(label = "Vendor", value = order.vendorName, icon = Icons.Default.Store)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = ManaBorder)
                    AdminDetailRow(
                        label = "Rider",
                        value = if (order.riderName.isEmpty()) "Not Assigned" else order.riderName,
                        icon = Icons.Default.DeliveryDining,
                        color = if (order.riderName.isEmpty()) ManaError else ManaSuccess
                    )
                    if (order.status != "DELIVERED" && order.status != "CANCELLED") {
                        ManaButton(
                            text = if (order.riderId.isEmpty()) "Assign Rider" else "Change Rider",
                            onClick = { showRiderDialog = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            icon = Icons.Default.AssignmentInd
                        )
                    }
                }
            }

            item {
                Text("ORDER TIMELINE", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
                Spacer(Modifier.height(8.dp))
                ManaCard(modifier = Modifier.fillMaxWidth()) {
                    val timeline = order.statusTimeline.toList().sortedBy { it.second }
                    if (timeline.isEmpty()) {
                        Text("Placed at: ${formatDate(order.createdAt)}", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column {
                            for ((status, time) in timeline) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(status, style = MaterialTheme.typography.bodySmall, color = ManaTextPrimary)
                                    Text(formatDate(time), style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("MANAGE", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.status != "CANCELLED" && order.status != "DELIVERED") {
                        ManaOutlinedButton(
                            text = "Cancel Order",
                            onClick = { viewModel.updateOrderStatus(order.id, "CANCELLED") },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Cancel
                        )
                    }
                    if (order.status == "PLACED") {
                        ManaButton(
                            text = "Confirm",
                            onClick = { viewModel.updateOrderStatus(order.id, "CONFIRMED") },
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CheckCircle,
                            containerColor = ManaSuccess
                        )
                    }
                }
            }
        }
    }

    if (showRiderDialog) {
        RiderSelectionDialog(
            riders = riders.filter { it.role == "RIDER" && it.isOnline },
            onRiderSelected = {
                viewModel.assignRider(order.id, it)
                showRiderDialog = false
            },
            onDismiss = { showRiderDialog = false }
        )
    }
}

@Composable
private fun RiderSelectionDialog(
    riders: List<User>,
    onRiderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Online Rider") },
        text = {
            if (riders.isEmpty()) {
                Text("No online riders available.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(riders) { rider ->
                        TextButton(
                            onClick = { onRiderSelected(rider.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(rider.name, style = MaterialTheme.typography.bodyLarge)
                                Text(rider.phone, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        HorizontalDivider(color = ManaBorder)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
