package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    val isLoading by viewModel.isLoading.collectAsState()

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
                    0 -> AdminStatsTab(viewModel = viewModel, onOrderClick = onOrdersClick, allOrders = orders)
                    1 -> AdminOrdersTab(viewModel = viewModel, onOrderClick = onOrdersClick)
                    2 -> AdminSystemTab(onUsersClick, onVendorsClick, onSettingsClick, onBroadcastClick, onSupportClick)
                    3 -> AdminMarketingTab(viewModel)
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
fun AdminStatsTab(
    viewModel: AdminViewModel,
    onOrderClick: (String) -> Unit,
    allOrders: List<Order>
) {
    val revenue by viewModel.revenue.collectAsState()
    val todayRevenue by viewModel.todayRevenue.collectAsState()
    val todayOrderCount by viewModel.todayOrderCount.collectAsState()
    val pendingVendors by viewModel.pendingVendorsCount.collectAsState()
    val activeRiders by viewModel.activeRidersCount.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("TODAY'S PERFORMANCE", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Today Revenue", value = "₹${todayRevenue.toInt()}", icon = Icons.Rounded.CurrencyRupee, color = ManaSuccess, modifier = Modifier.weight(1f))
                StatCard(title = "Today Orders", value = todayOrderCount.toString(), icon = Icons.Rounded.ShoppingBasket, color = ManaGold, modifier = Modifier.weight(1f))
            }
        }
        item {
            Text("ALL-TIME STATS", style = MaterialTheme.typography.labelMedium, color = ManaTextTertiary, letterSpacing = 2.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Total Revenue", value = "₹${revenue.toInt()}", icon = Icons.Rounded.Payments, color = ManaGold, modifier = Modifier.weight(1f))
                StatCard(title = "Total Orders", value = allOrders.size.toString(), icon = Icons.Rounded.Inventory2, color = ManaInfo, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Pending Vendors", value = pendingVendors.toString(), icon = Icons.Rounded.Store, color = ManaWarning, modifier = Modifier.weight(1f))
                StatCard(title = "Active Riders", value = activeRiders.toString(), icon = Icons.Rounded.DeliveryDining, color = ManaInfo, modifier = Modifier.weight(1f))
            }
        }
        
        item {
            Spacer(Modifier.height(8.dp))
            Text("RECENT LIVE ORDERS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        
        val recentOrders = allOrders.sortedByDescending { it.createdAt }.take(5)
        if (recentOrders.isEmpty()) {
            item {
                ManaCard {
                    Text("No recent orders to show.", color = ManaTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            items(recentOrders, key = { it.id }) { order ->
                ManaCard(onClick = { onOrderClick(order.id) }, border = BorderStroke(1.dp, ManaBorder)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(
                            when (order.status) {
                                "PLACED" -> ManaWarning
                                "PREPARING" -> ManaInfo
                                "READY" -> ManaGold
                                "PICKED_UP" -> ManaSuccess
                                "DELIVERED" -> ManaSuccess
                                else -> ManaRed
                            }
                        ))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Order #${order.id.takeLast(6).uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${order.vendorName} • ₹${order.total.toInt()}", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                        }
                        OrderStatusChip(order.status)
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun AdminOrdersTab(viewModel: AdminViewModel, onOrderClick: (String) -> Unit) {
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val currentFilter by viewModel.orderStatusFilter.collectAsState()
    val statusOptions = listOf("ALL", "PLACED", "PREPARING", "READY", "PICKED_UP", "DELIVERED", "CANCELLED")

    Column(modifier = Modifier.fillMaxSize()) {
        // Status Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statusOptions) { status ->
                FilterChip(
                    selected = currentFilter == status,
                    onClick = { viewModel.setOrderStatusFilter(status) },
                    label = {
                        Text(
                            if (status == "ALL") "All" else status.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            fontWeight = if (currentFilter == status) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ManaGold,
                        selectedLabelColor = Color.Black,
                        containerColor = ManaBgCard,
                        labelColor = ManaTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = ManaBorder,
                        selectedBorderColor = ManaGold,
                        enabled = true,
                        selected = currentFilter == status
                    )
                )
            }
        }

        // Orders Count
        Text(
            "${filteredOrders.size} orders",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ManaTextTertiary
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(filteredOrders, key = { it.id }) { order ->
                val dateFormat = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                val dateStr = dateFormat.format(java.util.Date(order.createdAt))

                ManaCard(onClick = { onOrderClick(order.id) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Order #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                            Text(order.userName, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                            Spacer(Modifier.height(4.dp))
                            Text(order.vendorName, style = MaterialTheme.typography.bodySmall, color = ManaGold)
                            Text("${order.items.size} items • $dateStr", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            OrderStatusChip(order.status)
                            Spacer(Modifier.height(4.dp))
                            Text("₹${order.total.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        }
                    }

                    // Refund button for delivered orders
                    if (order.status == "DELIVERED" || order.status == "CANCELLED") {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = ManaBorder.copy(alpha = 0.3f))
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.refundOrder(order) },
                                colors = ButtonDefaults.textButtonColors(contentColor = ManaRed)
                            ) {
                                Icon(Icons.Rounded.CurrencyRupee, null, modifier = Modifier.size(16.dp))
                                Text("REFUND", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
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
        NavigationBarItem(selected = selectedTab == 3, onClick = { onTabSelected(3) }, icon = { Icon(Icons.Rounded.Notifications, null) }, label = { Text("Marketing") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaRedStrong.copy(alpha = 0.2f)))
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

@Composable
fun AdminMarketingTab(viewModel: AdminViewModel) {
    var promoTitle by remember { mutableStateOf("") }
    var promoMsg by remember { mutableStateOf("") }
    var couponCode by remember { mutableStateOf("") }
    var discountValue by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("PUSH NOTIFICATIONS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = promoTitle, onValueChange = { promoTitle = it },
                        label = { Text("Notification Title", color = ManaTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, focusedLabelColor = ManaGold, unfocusedTextColor = ManaTextPrimary, focusedTextColor = ManaTextPrimary)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = promoMsg, onValueChange = { promoMsg = it },
                        label = { Text("Notification Message", color = ManaTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, focusedLabelColor = ManaGold, unfocusedTextColor = ManaTextPrimary, focusedTextColor = ManaTextPrimary)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.sendPromoNotification(promoTitle, promoMsg)
                            promoTitle = ""
                            promoMsg = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ManaGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("BROADCAST TO ALL USERS", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text("COUPONS & OFFERS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = couponCode, onValueChange = { couponCode = it.uppercase() },
                        label = { Text("Coupon Code (e.g. MANA50)", color = ManaTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, focusedLabelColor = ManaGold, unfocusedTextColor = ManaTextPrimary, focusedTextColor = ManaTextPrimary)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountValue, onValueChange = { discountValue = it },
                        label = { Text("Discount Value (%)", color = ManaTextSecondary) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, focusedLabelColor = ManaGold, unfocusedTextColor = ManaTextPrimary, focusedTextColor = ManaTextPrimary)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val v = discountValue.toDoubleOrNull() ?: 0.0
                            val c = com.example.manadeliverybellempally.data.model.Coupon(
                                code = couponCode,
                                description = "$discountValue% OFF",
                                discountType = "PERCENTAGE",
                                discountValue = v,
                                isActive = true
                            )
                            viewModel.saveCoupon(c)
                            couponCode = ""
                            discountValue = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ManaGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CREATE COUPON", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
