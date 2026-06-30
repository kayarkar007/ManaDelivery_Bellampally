package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.EmptyState
import com.example.manadeliverybellempally.ui.common.ManaCard
import com.example.manadeliverybellempally.ui.common.OrderStatusChip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersScreen(
    viewModel: CustomerViewModel,
    onOrderClick: (String) -> Unit,
    onVendorClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", color = ManaTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (orders.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.ShoppingBag,
                title = "No orders yet",
                subtitle = "Hungry? Order some delicious food from local vendors!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(orders.sortedByDescending { it.createdAt }, key = { it.id }) { order ->
                    val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                    val dateStr = formatter.format(Date(order.createdAt))

                    ManaCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOrderClick(order.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order #${order.id.takeLast(6).uppercase()}", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                            }
                            OrderStatusChip(status = order.status)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text(order.vendorName, style = MaterialTheme.typography.bodyMedium, color = ManaGold, fontWeight = FontWeight.Bold)
                        
                        // Show order items summary
                        order.items.take(3).forEach { item ->
                            Text("• ${item.name} × ${item.qty}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                        }
                        if (order.items.size > 3) {
                            Text("  +${order.items.size - 3} more items", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                        }
                        
                        Text("₹${order.total.toInt()}", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = ManaBorder.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Re-Order button for delivered/cancelled orders
                            if (order.status == "DELIVERED" || order.status == "CANCELLED") {
                                Button(
                                    onClick = {
                                        // Navigate to vendor store to re-order
                                        onVendorClick(order.vendorId)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ManaGold),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Re-Order", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }
                            
                            TextButton(
                                onClick = { onOrderClick(order.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = ManaGold)
                            ) {
                                Text("VIEW DETAILS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

