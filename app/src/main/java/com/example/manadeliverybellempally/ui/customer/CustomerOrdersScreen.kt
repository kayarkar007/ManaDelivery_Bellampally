package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                items(orders.sortedByDescending { it.createdAt }) { order ->
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
                        Text("${order.items.size} items • ₹${order.total.toInt()}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                        
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = ManaBorder.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))
                        
                        TextButton(
                            onClick = { onOrderClick(order.id) },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.textButtonColors(contentColor = ManaGold)
                        ) {
                            Text("VIEW DETAILS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}
