package com.example.manadeliverybellempally.ui.vendor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    vendorId: String,
    onLogout: () -> Unit,
    viewModel: VendorViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val vendor by viewModel.vendor.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(vendorId) {
        viewModel.initialize(vendorId)
    }

    Scaffold(
        containerColor = ManaBgPrimary,
        topBar = {
            VendorTopBar(vendor, onLogout)
        },
        bottomBar = {
            VendorBottomBar(selectedTab) { selectedTab = it }
        }
    ) { padding ->
        if (isLoading && vendor == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ManaGold) }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> VendorHomeTab(vendor, viewModel, orders)
                    1 -> VendorOrdersTab(orders, viewModel)
                    2 -> VendorMenuTab(products, viewModel)
                    3 -> VendorReviewsTab(vendor, viewModel)
                }
            }
        }
    }
}

@Composable
private fun VendorTopBar(vendor: Vendor?, onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(vendor?.storeName ?: "My Shop", style = MaterialTheme.typography.headlineSmall, color = ManaGold, fontWeight = FontWeight.Bold)
            Text("Bellempally Partner", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
        }
        IconButton(onClick = onLogout, modifier = Modifier.background(ManaRedStrong.copy(alpha = 0.1f), CircleShape)) {
            Icon(Icons.AutoMirrored.Rounded.Logout, "Logout", tint = ManaRed)
        }
    }
}

@Composable
fun VendorHomeTab(vendor: Vendor?, viewModel: VendorViewModel, orders: List<Order>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            StoreStatusCard(isOpen = vendor?.isStoreOpen ?: false) { viewModel.toggleStoreOpen(!(vendor?.isStoreOpen ?: false)) }
        }
        
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "New Orders", value = orders.count { it.status == "PLACED" }.toString(), icon = Icons.Rounded.NotificationsActive, color = ManaGold, modifier = Modifier.weight(1f))
                StatCard(title = "Preparing", value = orders.count { it.status == "CONFIRMED" || it.status == "PREPARING" }.toString(), icon = Icons.Rounded.Restaurant, color = ManaSuccess, modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("QUICK SUMMARY", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        
        item {
            ManaCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Today's Sale", color = ManaTextSecondary)
                        Text("₹${orders.filter { it.status == "DELIVERED" }.sumOf { it.total }.toInt()}", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = ManaBorder)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Completed", color = ManaTextSecondary)
                        Text("${orders.count { it.status == "DELIVERED" }} Orders", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreStatusCard(isOpen: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        color = if (isOpen) ManaSuccess.copy(alpha = 0.1f) else ManaBgCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isOpen) ManaSuccess else ManaBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isOpen) Icons.Rounded.Storefront else Icons.Rounded.Store, null, tint = if (isOpen) ManaSuccess else ManaTextTertiary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(if (isOpen) "SHOP IS OPEN" else "SHOP IS CLOSED", fontWeight = FontWeight.Black, color = if (isOpen) ManaSuccess else ManaTextPrimary)
                    Text(if (isOpen) "Visible to customers" else "Hidden from customers", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                }
            }
            Switch(checked = isOpen, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ManaSuccess))
        }
    }
}

@Composable
fun VendorOrdersTab(orders: List<Order>, viewModel: VendorViewModel) {
    val activeOrders = orders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("ACTIVE ORDERS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp) }
        
        if (activeOrders.isEmpty()) {
            item { EmptyState(icon = Icons.AutoMirrored.Rounded.ReceiptLong, title = "No Active Orders", subtitle = "New orders will appear here.") }
        } else {
            items(activeOrders) { order ->
                VendorOrderActionCard(order, viewModel)
            }
        }
    }
}

@Composable
private fun VendorOrderActionCard(order: Order, viewModel: VendorViewModel) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("ID: #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold, color = ManaGold)
                OrderStatusChip(order.status)
            }
            Spacer(Modifier.height(8.dp))
            order.items.forEach { item ->
                Text("• ${item.name} x${item.qty}", style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = ManaBorder)
            
            when(order.status) {
                "PLACED" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ManaOutlinedButton(text = "Reject", onClick = { viewModel.rejectOrder(order.id, "Vendor Busy") }, modifier = Modifier.weight(1f))
                        Button(onClick = { viewModel.acceptOrder(order.id) }, modifier = Modifier.weight(1.5f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = ManaSuccess)) {
                            Text("ACCEPT ORDER", fontWeight = FontWeight.Black)
                        }
                    }
                }
                "CONFIRMED", "PREPARING" -> {
                    Button(onClick = { viewModel.markOrderReady(order.id) }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = ManaGold)) {
                        Text("MARK AS READY", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
                "READY" -> {
                    Text("Waiting for Rider to pick up...", color = ManaGold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun VendorMenuTab(products: List<Product>, viewModel: VendorViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("MY ITEMS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp) }
        
        items(products) { product ->
            ManaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text("₹${product.price.toInt()}", color = ManaGold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if(product.isAvailable) "Available" else "Sold Out", style = MaterialTheme.typography.labelSmall, color = if(product.isAvailable) ManaSuccess else ManaRed)
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = product.isAvailable, onCheckedChange = { viewModel.toggleProductAvailability(product.id, it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ManaSuccess))
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = ManaBgCard, tonalElevation = 8.dp) {
        NavigationBarItem(selected = selectedTab == 0, onClick = { onTabSelected(0) }, icon = { Icon(Icons.Rounded.Dashboard, null) }, label = { Text("Home") })
        NavigationBarItem(selected = selectedTab == 1, onClick = { onTabSelected(1) }, icon = { Icon(Icons.Rounded.ShoppingBag, null) }, label = { Text("Orders") })
        NavigationBarItem(selected = selectedTab == 2, onClick = { onTabSelected(2) }, icon = { Icon(Icons.Rounded.RestaurantMenu, null) }, label = { Text("Menu") })
        NavigationBarItem(selected = selectedTab == 3, onClick = { onTabSelected(3) }, icon = { Icon(Icons.Rounded.Star, null) }, label = { Text("Reviews") })
    }
}

@Composable
fun VendorReviewsTab(vendor: Vendor?, viewModel: VendorViewModel) {
    val reviews by viewModel.reviews.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("PERFORMANCE", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(String.format("%.1f", vendor?.rating ?: 5.0), style = MaterialTheme.typography.displayLarge, color = ManaGold, fontWeight = FontWeight.Black)
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(5) { i ->
                            Icon(
                                imageVector = if (i < (vendor?.rating?.toInt() ?: 5)) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = null,
                                tint = ManaGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Based on ${vendor?.ratingCount ?: 0} reviews", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("CUSTOMER REVIEWS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        
        if (reviews.isEmpty()) {
            item {
                EmptyState(icon = Icons.Rounded.StarBorder, title = "No reviews yet", subtitle = "Deliver great food to earn 5-star ratings!")
            }
        } else {
            items(reviews) { review ->
                ManaCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(review.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                            Row {
                                Icon(Icons.Rounded.Star, null, tint = ManaGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(review.rating.toString(), style = MaterialTheme.typography.bodyMedium, color = ManaGold, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (review.comment.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("\"${review.comment}\"", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Order #${review.orderId.takeLast(6).uppercase()}", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                    }
                }
            }
        }
    }
}
