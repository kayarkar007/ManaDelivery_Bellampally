package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.allOrders.collectAsState()
    val vendors by viewModel.allVendors.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Reports") },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("BUSINESS PERFORMANCE", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(title = "Revenue", value = "₹${orders.filter { it.status == "DELIVERED" }.sumOf { it.total }.toInt()}", icon = Icons.Rounded.CurrencyRupee, color = ManaGold, modifier = Modifier.weight(1f))
                    StatCard(title = "Delivered", value = "${orders.count { it.status == "DELIVERED" }}", icon = Icons.Rounded.Receipt, color = ManaSuccess, modifier = Modifier.weight(1f))
                }
            }

            item {
                Text("TOP VENDORS", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
                Spacer(Modifier.height(12.dp))
                vendors.sortedByDescending { it.rating }.take(5).forEach { vendor ->
                    ManaCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(vendor.storeName, style = MaterialTheme.typography.bodyLarge)
                            Row {
                                Icon(Icons.Rounded.Star, null, tint = ManaGold, modifier = Modifier.size(16.dp))
                                Text(vendor.rating.toString(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            
            item {
                EmptyState(icon = Icons.Rounded.BarChart, title = "Insights", subtitle = "Detailed trend analysis and PDF exports are being processed in the background.")
            }
        }
    }
}
