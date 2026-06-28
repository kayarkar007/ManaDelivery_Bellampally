package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    onOrdersClick: (String) -> Unit,
    onUsersClick: () -> Unit,
    onVendorsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBroadcastClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val orders by viewModel.allOrders.collectAsState()
    val revenue by viewModel.revenue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val pendingCount by viewModel.pendingVendorsCount.collectAsState()
    val activeRiders by viewModel.activeRidersCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Scaffold(
        topBar = {
            AdminTopBar(onLogout)
        },
        bottomBar = {
            AdminBottomBar(selectedTab) { selectedTab = it }
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (isLoading && orders.isEmpty()) {
            AdminShimmerLoading(padding)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> AdminStatsTab(revenue, orders.size, pendingCount, activeRiders, onOrdersClick, orders)
                    1 -> AdminOrdersTab(orders, onOrdersClick)
                    2 -> AdminSystemTab(onUsersClick, onVendorsClick, onSettingsClick, onBroadcastClick, onSupportClick)
                }
            }
        }
    }
}

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Admin Central", style = MaterialTheme.typography.titleLarge, color = ManaGold, fontWeight = FontWeight.ExtraBold)
            Text("Bellempally Operations", style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
        }
        IconButton(
            onClick = onLogout,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(ManaRedStrong.copy(alpha = 0.2f))
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, "Logout", tint = ManaRed)
        }
    }
}

@Composable
fun AdminStatsTab(revenue: Double, orderCount: Int, pendingVendors: Int, activeRiders: Int, onOrderClick: (String) -> Unit, allOrders: List<Order>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("PLATFORM OVERVIEW", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Revenue", value = "₹${revenue.toInt()}", icon = Icons.Rounded.Payments, color = ManaGold, modifier = Modifier.weight(1f))
                StatCard(title = "Orders", value = orderCount.toString(), icon = Icons.Rounded.ShoppingBasket, color = ManaSuccess, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Pending", value = pendingVendors.toString(), icon = Icons.Rounded.Store, color = ManaWarning, modifier = Modifier.weight(1f))
                StatCard(title = "Active Riders", value = activeRiders.toString(), icon = Icons.Rounded.DeliveryDining, color = ManaInfo, modifier = Modifier.weight(1f))
            }
        }
        
        item {
            Spacer(Modifier.height(8.dp))
            Text("RECENT LIVE ORDERS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        
        val recentOrders = allOrders.take(3)
        if (recentOrders.isEmpty()) {
            item {
                ManaCard {
                    Text("No recent orders to show.", color = ManaTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            items(recentOrders) { order ->
                ManaCard(onClick = { onOrderClick(order.id) }, border = BorderStroke(1.dp, ManaBorder)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(order.status == "PLACED") ManaWarning else ManaSuccess))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Order #${order.id.takeLast(6).uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(order.vendorName, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                        }
                        Text(order.status, style = MaterialTheme.typography.labelSmall, color = ManaGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun AdminOrdersTab(orders: List<Order>, onOrderClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("ALL ORDERS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        items(orders) { order ->
            ManaCard(onClick = { onOrderClick(order.id) }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                        Text(order.userName, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                        Spacer(Modifier.height(4.dp))
                        Text(order.vendorName, style = MaterialTheme.typography.bodySmall, color = ManaGold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        OrderStatusChip(order.status)
                        Spacer(Modifier.height(4.dp))
                        Text("₹${order.total.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun AdminSystemTab(onUsersClick: () -> Unit, onVendorsClick: () -> Unit, onSettingsClick: () -> Unit, onBroadcastClick: () -> Unit, onSupportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SYSTEM MANAGEMENT", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemActionCard(title = "Users", icon = Icons.Rounded.People, onClick = onUsersClick, modifier = Modifier.weight(1f))
            SystemActionCard(title = "Vendors", icon = Icons.Rounded.Store, onClick = onVendorsClick, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemActionCard(title = "System Settings", icon = Icons.Rounded.Settings, onClick = onSettingsClick, modifier = Modifier.weight(1f))
            SystemActionCard(title = "Promotions", icon = Icons.Rounded.ConfirmationNumber, onClick = { }, modifier = Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(16.dp))
        Text("UTILITIES", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        
        ManaCard(onClick = onBroadcastClick, border = BorderStroke(1.dp, ManaBorder)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NotificationsActive, null, tint = ManaGold)
                Spacer(Modifier.width(16.dp))
                Text("Send Broadcast Notification", style = MaterialTheme.typography.bodyMedium)
            }
        }
        ManaCard(onClick = onSupportClick, border = BorderStroke(1.dp, ManaBorder)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SupportAgent, null, tint = ManaGold)
                Spacer(Modifier.width(16.dp))
                Text("Customer Support Tickets", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SystemActionCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ManaCard(modifier = modifier, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = ManaGold, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ManaTextPrimary)
        }
    }
}

@Composable
private fun AdminBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = ManaBgCard, tonalElevation = 8.dp) {
        NavigationBarItem(selected = selectedTab == 0, onClick = { onTabSelected(0) }, icon = { Icon(Icons.Rounded.Dashboard, null) }, label = { Text("Stats") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaRedStrong.copy(alpha = 0.2f)))
        NavigationBarItem(selected = selectedTab == 1, onClick = { onTabSelected(1) }, icon = { Icon(Icons.AutoMirrored.Rounded.ListAlt, null) }, label = { Text("Orders") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaRedStrong.copy(alpha = 0.2f)))
        NavigationBarItem(selected = selectedTab == 2, onClick = { onTabSelected(2) }, icon = { Icon(Icons.Rounded.Settings, null) }, label = { Text("System") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaRedStrong.copy(alpha = 0.2f)))
    }
}

@Composable
private fun AdminShimmerLoading(padding: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(20.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(20.dp))
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(20.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(20.dp))
        }
        Spacer(Modifier.height(24.dp))
        repeat(4) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(70.dp).padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp))
        }
    }
}
