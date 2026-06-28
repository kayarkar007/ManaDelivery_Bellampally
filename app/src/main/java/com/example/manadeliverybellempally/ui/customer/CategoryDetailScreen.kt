package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Widgets
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
fun CategoryDetailScreen(
    categoryId: String,
    viewModel: CustomerViewModel,
    onVendorClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val vendors = viewModel.vendorsForCategory(categoryId)
    val products = viewModel.productsForCategory(categoryId)
    val cart by viewModel.cart.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryId.replace("cat_", "").replaceFirstChar { it.uppercase() }, color = ManaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = ManaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (vendors.isNotEmpty()) {
                item {
                    Text("Top Vendors", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                }
                items(vendors) { vendor ->
                    VendorCard(
                        storeName = vendor.storeName,
                        category = vendor.categoryName,
                        rating = vendor.rating.toFloat(),
                        deliveryTime = 20,
                        isOpen = vendor.isStoreOpen,
                        onClick = { onVendorClick(vendor.id) }
                    )
                }
            }

            if (products.isNotEmpty()) {
                item {
                    Text("Products", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                }
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
                        onRemove = { viewModel.removeFromCart(product) }
                    )
                }
            }
            
            if (vendors.isEmpty() && products.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Widgets,
                        title = "No items found",
                        subtitle = "We couldn't find any vendors or products in this category yet."
                    )
                }
            }
            
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
