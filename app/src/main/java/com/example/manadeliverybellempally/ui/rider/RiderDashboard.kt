package com.example.manadeliverybellempally.ui.rider

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
fun RiderDashboardScreen(
    riderId: String,
    onLogout: () -> Unit,
    viewModel: RiderViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val rider by viewModel.rider.collectAsState()
    val availableOrders by viewModel.availableOrders.collectAsState()
    val myOrders by viewModel.myOrders.collectAsState()

    LaunchedEffect(riderId) {
        viewModel.initialize(riderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rider Dashboard", color = ManaGold, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout", tint = ManaGold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = ManaBgCard) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.ListAlt, null) }, label = { Text("My Deliveries") })
            }
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> RiderHomeTab(rider, viewModel, availableOrders)
                1 -> RiderDeliveriesTab(myOrders, viewModel)
            }
        }
    }
}

@Composable
fun RiderHomeTab(rider: User?, viewModel: RiderViewModel, availableOrders: List<Order>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            ManaCard {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Duty Status", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                        Text(if (rider?.isOnline == true) "Online" else "Offline", color = if (rider?.isOnline == true) ManaSuccess else ManaRed)
                    }
                    Switch(checked = rider?.isOnline == true, onCheckedChange = { viewModel.toggleDuty(it) })
                }
            }
        }
        item {
            Text("Available Deliveries", style = MaterialTheme.typography.titleMedium, color = ManaGold)
        }
        items(availableOrders) { order ->
            ManaCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Store: ${order.vendorName}", fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                    Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                    Text("Amount: ₹${order.total}", color = ManaGold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.acceptOrder(order.id) }, colors = ButtonDefaults.buttonColors(containerColor = ManaSuccess)) {
                        Text("Accept Order")
                    }
                }
            }
        }
    }
}

@Composable
fun RiderDeliveriesTab(myOrders: List<Order>, viewModel: RiderViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(myOrders) { order ->
            ManaCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Order #${order.id.takeLast(4)}", fontWeight = FontWeight.Bold, color = ManaGold)
                        Text(order.status, color = ManaGold)
                    }
                    Text("Customer: ${order.customerName}", color = ManaTextPrimary)
                    Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                    Spacer(Modifier.height(8.dp))
                    if (order.status == "READY") {
                        Button(onClick = { viewModel.updateStatus(order.id, "PICKED_UP") }) { Text("Mark Picked Up") }
                    } else if (order.status == "PICKED_UP") {
                        Button(onClick = { viewModel.updateStatus(order.id, "DELIVERED") }) { Text("Mark Delivered") }
                    }
                }
            }
        }
    }
}
