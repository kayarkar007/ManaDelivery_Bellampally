package com.example.manadeliverybellempally.ui.rider

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
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
fun RiderDashboardScreen(
    riderId: String,
    onLogout: () -> Unit,
    viewModel: RiderViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val rider by viewModel.rider.collectAsState()
    val orders by viewModel.availableOrders.collectAsState()
    val myDeliveries by viewModel.myOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(riderId) {
        viewModel.initialize(riderId)
    }

    Scaffold(
        containerColor = ManaBgPrimary,
        topBar = {
            RiderTopBar(rider, onLogout)
        },
        bottomBar = {
            RiderBottomBar(selectedTab) { selectedTab = it }
        }
    ) { padding ->
        if (isLoading && rider == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ManaGold) }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> RiderHomeTab(rider, viewModel, orders)
                    1 -> RiderDeliveriesTab(myDeliveries, viewModel)
                    2 -> RiderFinanceTab(rider, viewModel)
                }
            }
        }
    }
}

@Composable
private fun RiderTopBar(rider: User?, onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Namaste, ${rider?.name ?: "Rider"}", style = MaterialTheme.typography.headlineSmall, color = ManaGold, fontWeight = FontWeight.Bold)
            Text("Bellempally Delivery Partner", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
        }
        IconButton(onClick = onLogout, modifier = Modifier.background(ManaRedStrong.copy(alpha = 0.1f), CircleShape)) {
            Icon(Icons.AutoMirrored.Rounded.Logout, "Logout", tint = ManaRed)
        }
    }
}

@Composable
fun RiderHomeTab(rider: User?, viewModel: RiderViewModel, orders: List<Order>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            DutyToggleCard(isOnline = rider?.isOnline ?: false) { viewModel.toggleDuty(!(rider?.isOnline ?: false)) }
        }
        
        item {
            Text("NEW ORDERS NEAR YOU", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }

        if (orders.isEmpty()) {
            item {
                EmptyState(icon = Icons.Rounded.Search, title = "Waiting for Orders", subtitle = "Stay online to receive new delivery requests in Bellempally.")
            }
        } else {
            items(orders) { order ->
                RiderOrderCard(order) { viewModel.acceptOrder(order.id) }
            }
        }
    }
}

@Composable
private fun DutyToggleCard(isOnline: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        color = if (isOnline) ManaSuccess.copy(alpha = 0.1f) else ManaBgCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isOnline) ManaSuccess else ManaBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isOnline) ManaSuccess else ManaTextTertiary))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(if (isOnline) "YOU ARE ONLINE" else "YOU ARE OFFLINE", fontWeight = FontWeight.Black, color = if (isOnline) ManaSuccess else ManaTextPrimary)
                    Text(if (isOnline) "Ready to take orders" else "Go online to earn", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                }
            }
            Switch(checked = isOnline, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ManaSuccess))
        }
    }
}

@Composable
private fun RiderOrderCard(order: Order, onAccept: () -> Unit) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("NEW ORDER", color = ManaGold, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                Text("₹${order.deliveryFee.toInt()} EARNING", color = ManaSuccess, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            RiderLocationStep(icon = Icons.Rounded.Store, title = "Pickup", address = order.vendorName)
            Spacer(Modifier.height(8.dp))
            RiderLocationStep(icon = Icons.Rounded.PersonPinCircle, title = "Drop", address = order.deliveryAddress)
            
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ManaSuccess),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ACCEPT ORDER", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun RiderDeliveriesTab(myDeliveries: List<Order>, viewModel: RiderViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("MY ACTIVE TASKS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp) }
        
        if (myDeliveries.isEmpty()) {
            item { EmptyState(icon = Icons.AutoMirrored.Rounded.Assignment, title = "No Active Tasks", subtitle = "Accepted orders will show up here.") }
        } else {
            items(myDeliveries) { order ->
                ActiveTaskCard(
                    order = order,
                    onStatusUpdate = { status -> viewModel.updateStatus(order.id, status) },
                    onPingLocation = { lat, lng -> viewModel.pingLocation(order.id, lat, lng) }
                )
            }
        }
    }
}

@Composable
private fun ActiveTaskCard(
    order: Order, 
    onStatusUpdate: (String) -> Unit,
    onPingLocation: (Double, Double) -> Unit
) {
    // Mock Live Pinging
    if (order.status == "OUT_FOR_DELIVERY") {
        LaunchedEffect(order.id) {
            var lat = 19.0601 // Mock starting point near Bellempally
            var lng = 79.4890
            while(true) {
                lat += 0.0001
                lng += 0.0001
                onPingLocation(lat, lng)
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("ORDER #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold, color = ManaGold)
            Text(order.vendorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = ManaBorder)
            
            val nextStatus = when(order.status) {
                "ACCEPTED" -> "OUT_FOR_DELIVERY"
                "READY_FOR_PICKUP" -> "OUT_FOR_DELIVERY"
                "OUT_FOR_DELIVERY" -> "DELIVERED"
                else -> ""
            }
            
            if (nextStatus.isNotEmpty()) {
                Button(
                    onClick = { onStatusUpdate(nextStatus) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ManaGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MARK AS $nextStatus", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun RiderFinanceTab(rider: User?, viewModel: RiderViewModel) {
    val earnings by viewModel.totalEarnings.collectAsState()
    val deliveries by viewModel.completedDeliveriesCount.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("MY EARNINGS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = "Today", value = "₹${earnings.toInt()}", icon = Icons.Rounded.Payments, color = ManaSuccess, modifier = Modifier.weight(1f))
            StatCard(title = "Wallet", value = "₹${rider?.walletBalance?.toInt() ?: 0}", icon = Icons.Rounded.AccountBalanceWallet, color = ManaGold, modifier = Modifier.weight(1f))
        }

        ManaCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total Deliveries", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
                    Text("$deliveries", style = MaterialTheme.typography.headlineSmall, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.AutoMirrored.Rounded.DirectionsBike, null, tint = ManaGold, modifier = Modifier.size(32.dp))
            }
        }
        
        ManaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, null, tint = ManaInfo)
                Spacer(Modifier.width(12.dp))
                Text("Earnings are settled every Monday to your linked bank account. Base pay is ₹20 + ₹5/km.", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
            }
        }
    }
}

@Composable
private fun RiderLocationStep(icon: ImageVector, title: String, address: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ManaGold, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
            Text(address, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun RiderBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = ManaBgCard, tonalElevation = 8.dp) {
        NavigationBarItem(selected = selectedTab == 0, onClick = { onTabSelected(0) }, icon = { Icon(Icons.AutoMirrored.Rounded.DirectionsBike, null) }, label = { Text("Duty") })
        NavigationBarItem(selected = selectedTab == 1, onClick = { onTabSelected(1) }, icon = { Icon(Icons.AutoMirrored.Rounded.Assignment, null) }, label = { Text("Tasks") })
        NavigationBarItem(selected = selectedTab == 2, onClick = { onTabSelected(2) }, icon = { Icon(Icons.Rounded.Payments, null) }, label = { Text("Earnings") })
    }
}
