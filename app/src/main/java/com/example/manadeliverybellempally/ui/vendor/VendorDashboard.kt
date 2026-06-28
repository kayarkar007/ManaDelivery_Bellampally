package com.example.manadeliverybellempally.ui.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    vendorId: String,
    onLogout: () -> Unit,
    viewModel: VendorViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val vendor by viewModel.vendor.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()

    LaunchedEffect(vendorId) {
        viewModel.initialize(vendorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vendor?.storeName ?: "Vendor Dashboard", color = ManaGold, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout", tint = ManaGold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = ManaBgCard) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.ListAlt, null) }, label = { Text("Orders") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Menu, null) }, label = { Text("Menu") })
            }
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> VendorHomeTab(vendor, viewModel, orders)
                1 -> VendorOrdersTab(orders, viewModel)
                2 -> VendorMenuTab(products, viewModel)
            }
        }
    }
}

@Composable
fun VendorHomeTab(vendor: Vendor?, viewModel: VendorViewModel, orders: List<Order>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            ManaCard {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Store Status", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                        Text(if (vendor?.isStoreOpen == true) "Accepting Orders" else "Closed", color = if (vendor?.isStoreOpen == true) ManaSuccess else ManaRed)
                    }
                    Switch(checked = vendor?.isStoreOpen == true, onCheckedChange = { viewModel.toggleStoreOpen(it) })
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardStat(title = "Today's Orders", value = orders.size.toString(), modifier = Modifier.weight(1f))
                DashboardStat(title = "Revenue", value = "₹${orders.filter { it.status == "DELIVERED" }.sumOf { it.total }.toInt()}", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun VendorOrdersTab(orders: List<Order>, viewModel: VendorViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(orders) { order ->
            ManaCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Order #${order.id.takeLast(4)}", fontWeight = FontWeight.Bold, color = ManaGold)
                        Text(order.status, color = ManaGold)
                    }
                    order.items.forEach { item ->
                        Text("${item.qty}x ${item.name}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (order.status == "PLACED") {
                        Button(onClick = { viewModel.acceptOrder(order.id) }, colors = ButtonDefaults.buttonColors(containerColor = ManaSuccess)) {
                            Text("Accept Order")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorMenuTab(products: List<Product>, viewModel: VendorViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products) { product ->
            ManaCard {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                        Text("₹${product.price}", color = ManaGold)
                    }
                    Switch(checked = product.isAvailable, onCheckedChange = { viewModel.toggleProductAvailability(product.id, it) })
                }
            }
        }
    }
}

@Composable
fun DashboardStat(title: String, value: String, modifier: Modifier = Modifier) {
    ManaCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = ManaGold, fontWeight = FontWeight.Bold)
        }
    }
}
