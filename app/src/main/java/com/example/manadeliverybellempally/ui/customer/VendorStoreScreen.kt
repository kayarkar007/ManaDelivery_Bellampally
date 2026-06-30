package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStoreScreen(
    vendorId: String,
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val products by viewModel.selectedVendorProducts.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val cart by viewModel.cart.collectAsState()
    
    val vendor = remember(vendors, vendorId) { vendors.find { it.id == vendorId } }
    val cartConflict by viewModel.cartConflict.collectAsState()

    LaunchedEffect(vendorId) {
        viewModel.loadProductsForVendor(vendorId)
    }

    if (cartConflict != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCartConflict() },
            title = { Text("Replace Cart?", color = ManaTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Your cart contains items from a different store. Do you want to clear the cart and add this item instead?", color = ManaTextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.replaceCartWith(cartConflict!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = ManaGold)
                ) {
                    Text("Replace Cart", color = ManaBgPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCartConflict() }) {
                    Text("Cancel", color = ManaTextTertiary)
                }
            },
            containerColor = ManaBgCard
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vendor?.storeName ?: "Store", color = ManaTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = ManaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ManaBgCard)
                        .padding(16.dp)
                        .safeDrawingPadding()
                ) {
                    val total = viewModel.getCartTotal()
                    val itemCount = cart.values.sum()
                    
                    ManaButton(
                        text = "View Cart ($itemCount items) • ₹${total.toInt()}",
                        onClick = onCheckout,
                        icon = Icons.Rounded.ShoppingCart,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VendorDetailHeader(vendor)
            }

            item {
                Text(
                    "MENU",
                    style = MaterialTheme.typography.labelMedium,
                    color = ManaGold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (products.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.MenuBook,
                        title = "No products",
                        subtitle = "This vendor hasn't listed any items yet."
                    )
                }
            } else {
                items(products) { product ->
                    ProductCard(
                        name = product.name,
                        price = product.price,
                        discountPrice = product.discountPrice,
                        unit = product.unit,
                        isVeg = product.isVeg,
                        rating = 4.0f,
                        quantity = cart[product.id] ?: 0,
                        onAdd = { viewModel.addToCart(product) },
                        onRemove = { viewModel.removeFromCart(product) },
                    )
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun VendorDetailHeader(vendor: Vendor?) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(ManaRedStrong),
                    contentAlignment = Alignment.Center
                ) {
                    Text(vendor?.storeName?.take(1) ?: "V", color = ManaGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(vendor?.storeName ?: "Loading...", style = MaterialTheme.typography.titleLarge, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                    Text(vendor?.categoryName ?: "", style = MaterialTheme.typography.bodySmall, color = ManaGold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                InfoColumn(label = "Rating", value = "${vendor?.rating ?: 0.0} ★")
                InfoColumn(label = "Delivery", value = "20-30 min")
                InfoColumn(label = "Status", value = if (vendor?.isStoreOpen == true) "Open" else "Closed", color = if (vendor?.isStoreOpen == true) ManaSuccess else ManaRed)
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String, color: Color = ManaTextPrimary) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
    }
}
