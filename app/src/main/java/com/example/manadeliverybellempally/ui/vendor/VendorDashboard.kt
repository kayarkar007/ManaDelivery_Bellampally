package com.example.manadeliverybellempally.ui.vendor

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val error by viewModel.error.collectAsState()

    LaunchedEffect(vendorId) {
        viewModel.initialize(vendorId)
    }

    // Show error snackbar
    error?.let { msg ->
        LaunchedEffect(msg) {
            // Auto-clear after showing
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = ManaBgPrimary,
        topBar = {
            VendorTopBar(vendor, onLogout)
        },
        bottomBar = {
            VendorBottomBar(selectedTab) { selectedTab = it }
        },
        snackbarHost = {
            error?.let {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = ManaRedStrong,
                    contentColor = Color.White
                ) { Text(it) }
            }
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
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
    }
    val todayOrders = orders.filter { it.createdAt >= todayStart }
    val deliveredToday = todayOrders.filter { it.status == "DELIVERED" }
    val todayRevenue = deliveredToday.sumOf { it.total }
    val pendingOrders = orders.count { it.status == "PLACED" }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            StoreStatusCard(isOpen = vendor?.isStoreOpen ?: false) { viewModel.toggleStoreOpen(!(vendor?.isStoreOpen ?: false)) }
        }

        // New orders alert
        if (pendingOrders > 0) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ManaGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ManaGold)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = ManaGold, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("$pendingOrders New Order${if (pendingOrders > 1) "s" else ""}!", fontWeight = FontWeight.Bold, color = ManaGold)
                            Text("Tap Orders tab to accept", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                        }
                    }
                }
            }
        }
        
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Today's Revenue", value = "₹${todayRevenue.toInt()}", icon = Icons.Rounded.CurrencyRupee, color = ManaSuccess, modifier = Modifier.weight(1f))
                StatCard(title = "Orders Today", value = todayOrders.size.toString(), icon = Icons.Rounded.ShoppingBag, color = ManaGold, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Delivered", value = deliveredToday.size.toString(), icon = Icons.Rounded.CheckCircle, color = ManaSuccess, modifier = Modifier.weight(1f))
                StatCard(title = "Preparing", value = orders.count { it.status == "CONFIRMED" || it.status == "PREPARING" }.toString(), icon = Icons.Rounded.Restaurant, color = ManaGold, modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("ALL TIME STATS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
        }
        
        item {
            ManaCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total Revenue", color = ManaTextSecondary)
                        Text("₹${orders.filter { it.status == "DELIVERED" }.sumOf { it.total }.toInt()}", fontWeight = FontWeight.Bold, color = ManaSuccess)
                    }
                    HorizontalDivider(color = ManaBorder)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total Orders", color = ManaTextSecondary)
                        Text("${orders.size}", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = ManaBorder)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Completed", color = ManaTextSecondary)
                        Text("${orders.count { it.status == "DELIVERED" }} Orders", fontWeight = FontWeight.Bold, color = ManaSuccess)
                    }
                    HorizontalDivider(color = ManaBorder)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelled", color = ManaTextSecondary)
                        Text("${orders.count { it.status == "CANCELLED" }} Orders", fontWeight = FontWeight.Bold, color = ManaRed)
                    }
                    HorizontalDivider(color = ManaBorder)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Average Rating", color = ManaTextSecondary)
                        Text("⭐ ${String.format("%.1f", vendor?.rating ?: 0.0)}", fontWeight = FontWeight.Bold, color = ManaGold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
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
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("ACTIVE ORDERS (${activeOrders.size})", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp) }
        
        if (activeOrders.isEmpty()) {
            item { EmptyState(icon = Icons.AutoMirrored.Rounded.ReceiptLong, title = "No Active Orders", subtitle = "New orders will appear here when customers order from your store.") }
        } else {
            items(activeOrders, key = { it.id }) { order ->
                VendorOrderActionCard(order, viewModel, context)
            }
        }
    }
}

@Composable
private fun VendorOrderActionCard(order: Order, viewModel: VendorViewModel, context: android.content.Context) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Order #${order.id.takeLast(5).uppercase()}", fontWeight = FontWeight.Bold, color = ManaGold)
                    Text(dateFormat.format(Date(order.createdAt)), style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                }
                OrderStatusChip(order.status)
            }
            Spacer(Modifier.height(8.dp))

            // Customer info with call button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(order.customerName.ifEmpty { order.userName }, style = MaterialTheme.typography.bodyMedium, color = ManaTextPrimary)
                    if (order.deliveryAddress.isNotEmpty()) {
                        Text(order.deliveryAddress, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary, maxLines = 1)
                    }
                }
                val phone = order.customerPhone.ifEmpty { order.userPhone }
                if (phone.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.background(ManaSuccess.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Call, "Call Customer", tint = ManaSuccess)
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = ManaBorder)

            // Order items
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.name} × ${item.qty}", style = MaterialTheme.typography.bodyMedium, color = ManaTextPrimary)
                    Text("₹${(item.price * item.qty).toInt()}", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = ManaBorder)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                Text("₹${order.total.toInt()}", fontWeight = FontWeight.Bold, color = ManaGold)
            }

            Spacer(Modifier.height(12.dp))
            
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ManaGold.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⏳ Waiting for rider to pick up...", modifier = Modifier.padding(12.dp), color = ManaGold, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorMenuTab(products: List<Product>, viewModel: VendorViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var deletingProduct by remember { mutableStateOf<Product?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MY ITEMS (${products.size})", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ManaGold),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Item", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (products.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.RestaurantMenu,
                    title = "No items yet",
                    subtitle = "Tap '+ Add Item' to create your first product."
                )
            }
        }
        
        items(products, key = { it.id }) { product ->
            ManaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(product.name, fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                            if (product.isVeg) {
                                Spacer(Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = ManaSuccess.copy(alpha = 0.1f)) {
                                    Text("VEG", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ManaSuccess)
                                }
                            }
                        }
                        if (product.description.isNotEmpty()) {
                            Text(product.description, style = MaterialTheme.typography.bodySmall, color = ManaTextTertiary, maxLines = 1)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (product.discountPrice > 0 && product.discountPrice < product.price) {
                                Text("₹${product.discountPrice.toInt()}", color = ManaGold, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Text("₹${product.price.toInt()}", color = ManaTextTertiary, style = MaterialTheme.typography.bodySmall, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            } else {
                                Text("₹${product.price.toInt()}", color = ManaGold, fontWeight = FontWeight.Bold)
                            }
                            Text(" / ${product.unit}", color = ManaTextTertiary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = product.isAvailable, 
                            onCheckedChange = { viewModel.toggleProductAvailability(product.id, it) }, 
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ManaSuccess)
                        )
                        Text(if(product.isAvailable) "Available" else "Sold Out", style = MaterialTheme.typography.labelSmall, color = if(product.isAvailable) ManaSuccess else ManaRed)
                    }
                    IconButton(onClick = { editingProduct = product }) {
                        Icon(Icons.Rounded.Edit, "Edit", tint = ManaTextTertiary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { deletingProduct = product }) {
                        Icon(Icons.Rounded.Delete, "Delete", tint = ManaRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    // Add/Edit Product Dialog
    if (showAddDialog || editingProduct != null) {
        val isEdit = editingProduct != null
        val initial = editingProduct ?: Product()
        AddProductDialog(
            initial = initial,
            isEdit = isEdit,
            onDismiss = { showAddDialog = false; editingProduct = null },
            onSave = { product ->
                if (isEdit) viewModel.addProduct(product) // addProduct does upsert via set()
                else viewModel.addProduct(product)
                showAddDialog = false
                editingProduct = null
            }
        )
    }

    // Delete Confirmation
    if (deletingProduct != null) {
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("Delete Product?", color = ManaTextPrimary) },
            text = { Text("Are you sure you want to delete '${deletingProduct?.name}'? This action cannot be undone.", color = ManaTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    deletingProduct?.let { viewModel.deleteProduct(it.id) }
                    deletingProduct = null
                }) { Text("Delete", color = ManaRedStrong) }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) { Text("Cancel", color = ManaTextSecondary) }
            },
            containerColor = ManaBgCard
        )
    }
}

@Composable
private fun AddProductDialog(
    initial: Product,
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var price by remember { mutableStateOf(if (initial.price > 0) initial.price.toString() else "") }
    var discountPrice by remember { mutableStateOf(if (initial.discountPrice > 0) initial.discountPrice.toString() else "") }
    var unit by remember { mutableStateOf(initial.unit) }
    var isVeg by remember { mutableStateOf(initial.isVeg) }
    var prescriptionRequired by remember { mutableStateOf(initial.prescriptionRequired) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Product" else "Add New Product", color = ManaGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Price (₹) *") }, modifier = Modifier.weight(1f), singleLine = true
                    )
                    OutlinedTextField(
                        value = discountPrice, onValueChange = { discountPrice = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Offer Price") }, modifier = Modifier.weight(1f), singleLine = true
                    )
                }
                OutlinedTextField(
                    value = unit, onValueChange = { unit = it },
                    label = { Text("Unit (kg, pcs, plate, litre)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vegetarian", color = ManaTextPrimary)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it }, colors = SwitchDefaults.colors(checkedTrackColor = ManaSuccess))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prescription Required", color = ManaTextPrimary)
                    Switch(checked = prescriptionRequired, onCheckedChange = { prescriptionRequired = it }, colors = SwitchDefaults.colors(checkedTrackColor = ManaGold))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && parsedPrice > 0) {
                        onSave(initial.copy(
                            name = name.trim(),
                            description = description.trim(),
                            price = parsedPrice,
                            discountPrice = discountPrice.toDoubleOrNull() ?: 0.0,
                            unit = unit.ifBlank { "pcs" },
                            isVeg = isVeg,
                            prescriptionRequired = prescriptionRequired
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ManaGold),
                enabled = name.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(if (isEdit) "Update" else "Add Product", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ManaTextSecondary) }
        },
        containerColor = ManaBgCard
    )
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
            items(reviews, key = { it.id }) { review ->
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
