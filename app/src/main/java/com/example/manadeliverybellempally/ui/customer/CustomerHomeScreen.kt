package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.R
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    customerId: String,
    viewModel: CustomerViewModel,
    onCategoryClick: (String) -> Unit,
    onVendorClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val vendors by viewModel.vendors.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(customerId) {
        viewModel.initialize(customerId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ManaBgPrimary,
        bottomBar = {
            CustomerBottomBar(selectedTab = selectedTab) { tab ->
                selectedTab = tab
                when (tab) {
                    1 -> onSearchClick()
                    2 -> onOrdersClick()
                    3 -> onProfileClick()
                }
            }
        },
    ) { padding ->
        if (isLoading && vendors.isEmpty()) {
            HomeShimmerLoading(padding)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                CustomerTopBar(
                    onSearchClick = onSearchClick,
                    onCartClick = onCartClick,
                    onWalletClick = onWalletClick,
                    cartItemCount = cart.values.sum(),
                )

                Spacer(Modifier.height(8.dp))
                HeroBanner()
                
                Spacer(Modifier.height(32.dp))
                QuickActions(onWalletClick, onOrdersClick)

                Spacer(Modifier.height(32.dp))
                SectionHeader(
                    title = "Shop by Category",
                    subtitle = "Premium local selection",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
                CategoriesGrid(
                    categories = categories,
                    onCategoryClick = onCategoryClick,
                )

                Spacer(Modifier.height(32.dp))
                SectionHeader(
                    title = "Nearby Vendors",
                    subtitle = "Top-rated in Bellempally",
                    onSeeAll = onSearchClick,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
                VendorRow(vendors = vendors, onVendorClick = onVendorClick)

                Spacer(Modifier.height(32.dp))
                SectionHeader(
                    title = "Popular Items",
                    subtitle = "Most loved by locals",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
                PopularItems(
                    products = products,
                    cart = cart,
                    onAdd = { product -> viewModel.addToCart(product) },
                    onRemove = { product -> viewModel.removeFromCart(product) },
                )
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun QuickActions(onWalletClick: () -> Unit, onOrdersClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ManaCard(
            modifier = Modifier.weight(1f),
            onClick = onWalletClick,
            border = BorderStroke(1.dp, ManaGold.copy(alpha = 0.2f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AccountBalanceWallet, null, tint = ManaGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Wallet", style = MaterialTheme.typography.titleSmall, color = ManaTextPrimary)
            }
        }
        ManaCard(
            modifier = Modifier.weight(1f),
            onClick = onOrdersClick,
            border = BorderStroke(1.dp, ManaBorder)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ReceiptLong, null, tint = ManaTextSecondary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Orders", style = MaterialTheme.typography.titleSmall, color = ManaTextPrimary)
            }
        }
    }
}

@Composable
private fun HomeShimmerLoading(padding: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(24.dp))
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp))
        }
        Spacer(Modifier.height(32.dp))
        ShimmerBox(modifier = Modifier.width(150.dp).height(24.dp))
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                ShimmerBox(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(16.dp))
            }
        }
    }
}

@Composable
private fun VendorRow(
    vendors: List<Vendor>,
    onVendorClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(vendors) { vendor ->
            VendorCard(
                storeName = vendor.storeName,
                category = vendor.categoryName,
                rating = vendor.rating.toFloat(),
                deliveryTime = 20,
                isOpen = vendor.isStoreOpen,
                isBusy = vendor.isBusy,
                onClick = { onVendorClick(vendor.id) },
            )
        }
    }
}

@Composable
private fun PopularItems(
    products: List<Product>,
    cart: Map<String, Int>,
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit,
) {
    val popularProducts = products.take(5)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        popularProducts.forEach { product ->
            ProductCard(
                name = product.name,
                price = product.price,
                discountPrice = product.discountPrice,
                unit = product.unit,
                isVeg = product.isVeg,
                rating = 4.0f,
                quantity = cart[product.id] ?: 0,
                onAdd = { onAdd(product) },
                onRemove = { onRemove(product) },
            )
        }
    }
}

@Composable
private fun CustomerTopBar(
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onWalletClick: () -> Unit,
    cartItemCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Mana Delivery", style = MaterialTheme.typography.titleLarge, color = ManaGold, fontWeight = FontWeight.Bold)
                Text("Bellempally", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
            }
        }
        Row {
            IconButton(onClick = onSearchClick) { Icon(Icons.Rounded.Search, null, tint = ManaGold) }
            IconButton(onClick = onCartClick) { 
                BadgedBox(badge = { if(cartItemCount > 0) Badge { Text(cartItemCount.toString()) } }) {
                    Icon(Icons.Rounded.ShoppingCart, null, tint = ManaGold) 
                }
            }
            IconButton(onClick = onWalletClick) { Icon(Icons.Rounded.AccountBalanceWallet, null, tint = ManaGold) }
        }
    }
}

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(ManaRedStrong, ManaRed, Color.Black.copy(alpha = 0.9f))
                )
            )
    ) {
        // Background Logo Pattern
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.15f).offset(x = 80.dp, y = 40.dp).size(240.dp),
            contentScale = ContentScale.Fit
        )
        
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = ManaGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "BELLEMPALLY'S OWN",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ManaGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Experience Premium\nDelivery Services",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Fresh Food • Groceries • Medicines",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, onSeeAll: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) { Text("See All", color = ManaGold) }
        }
    }
}

@Composable
private fun CategoriesGrid(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        categories.forEach { category ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onCategoryClick(category.id) }) {
                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(ManaBgCard).border(1.dp, ManaBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(category.id) {
                            "cat_grocery" -> Icons.Rounded.Storefront
                            "cat_food" -> Icons.Rounded.Restaurant
                            "cat_biryani" -> Icons.Rounded.OutdoorGrill
                            "cat_chicken" -> Icons.Rounded.SetMeal
                            "cat_medicine" -> Icons.Rounded.LocalPharmacy
                            "cat_vegetables" -> Icons.Rounded.Eco
                            else -> Icons.Rounded.Category
                        },
                        contentDescription = null,
                        tint = ManaGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(category.name, style = MaterialTheme.typography.labelSmall, color = ManaTextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CustomerBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = ManaBgCard, tonalElevation = 8.dp) {
        val items = listOf(
            Icons.Rounded.Home to "Home",
            Icons.Rounded.Search to "Search",
            Icons.Rounded.ShoppingBag to "Orders",
            Icons.Rounded.Person to "Profile"
        )
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, unselectedIconColor = ManaTextTertiary, indicatorColor = ManaRed.copy(alpha = 0.2f))
            )
        }
    }
}
