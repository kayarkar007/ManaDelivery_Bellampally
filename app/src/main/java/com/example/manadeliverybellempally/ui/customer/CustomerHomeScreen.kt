package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
    val banners by viewModel.banners.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddressPicker by remember { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf("Fetching Location...") }

    LaunchedEffect(customerId) {
        viewModel.initialize(customerId)
    }

    Scaffold(
        topBar = {
            CustomerTopBar(
                onProfileClick = onProfileClick,
                onWalletClick = onWalletClick,
                onAddressClick = { showAddressPicker = true },
                cartSize = cart.values.sum(),
                address = selectedAddress
            )
        },
        bottomBar = {
            CustomerBottomBar(0) { tab ->
                when(tab) {
                    1 -> onOrdersClick()
                    2 -> onProfileClick()
                }
            }
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (isLoading && categories.isEmpty()) {
            HomeShimmerLoading(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    HeroBanner()
                }

                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(16.dp))
                        SearchBar(onClick = onSearchClick)
                        Spacer(Modifier.height(24.dp))
                        QuickActions(onOrdersClick, onWalletClick)
                        Spacer(Modifier.height(24.dp))
                    }
                }

                item {
                    SectionHeader(
                        title = "Shop by Category",
                        subtitle = "Fresh & fast delivery",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    CategoriesGrid(categories, onCategoryClick)
                }

                item {
                    SectionHeader(
                        title = "Top Stores",
                        subtitle = "Highly rated in Bellampally",
                        onSeeAll = { },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    VendorRow(vendors, onVendorClick)
                }

                item {
                    SectionHeader(
                        title = "Trending Now",
                        subtitle = "Most ordered items",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    PopularItems(products, cart, { viewModel.addToCart(it) }, { viewModel.removeFromCart(it) })
                }
            }
        }

        if (showAddressPicker) {
            HyperlocalAddressPicker(
                onAddressSelected = {
                    selectedAddress = it
                    showAddressPicker = false
                },
                onDismiss = { showAddressPicker = false }
            )
        }
    }
}

@Composable
fun SearchBar(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = ManaBgCard,
        border = BorderStroke(1.dp, ManaBorder),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, null, tint = ManaGold)
            Spacer(Modifier.width(12.dp))
            Text("Search Food, Grocery or Milk...", color = ManaTextTertiary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.Mic, null, tint = ManaGold)
        }
    }
}

@Composable
fun QuickActions(onOrdersClick: () -> Unit, onWalletClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ManaActionCard(
            title = "My Orders",
            icon = Icons.Rounded.ShoppingBag,
            onClick = onOrdersClick,
            modifier = Modifier.weight(1f),
            description = "Track Live"
        )
        ManaActionCard(
            title = "Wallet",
            icon = Icons.Rounded.AccountBalanceWallet,
            onClick = onWalletClick,
            modifier = Modifier.weight(1f),
            description = "Offers & Pay"
        )
    }
}

@Composable
fun HomeShimmerLoading(padding: PaddingValues) {
    Column(modifier = Modifier.padding(padding).padding(16.dp)) {
        ShimmerBox(Modifier.fillMaxWidth().height(180.dp), RoundedCornerShape(24.dp))
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerBox(Modifier.weight(1f).height(100.dp), RoundedCornerShape(24.dp))
            ShimmerBox(Modifier.weight(1f).height(100.dp), RoundedCornerShape(24.dp))
        }
    }
}

@Composable
fun VendorRow(vendors: List<Vendor>, onVendorClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(vendors) { vendor ->
            VendorCard(
                storeName = vendor.storeName,
                categoryName = vendor.categoryName,
                rating = vendor.rating.toFloat(),
                deliveryTime = vendor.deliveryTimeMinutes,
                isOpen = vendor.isStoreOpen,
                isBusy = vendor.isBusy,
                onClick = { onVendorClick(vendor.id) },
                modifier = Modifier.width(280.dp)
            )
        }
    }
}

@Composable
fun PopularItems(
    products: List<Product>,
    cart: Map<String, Int>,
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        products.take(5).forEach { product ->
            ProductCard(
                name = product.name,
                price = product.price,
                discountPrice = product.discountPrice,
                unit = product.unit,
                isVeg = product.isVeg,
                rating = product.rating.toFloat(),
                quantity = cart[product.id] ?: 0,
                onAdd = { onAdd(product) },
                onRemove = { onRemove(product) }
            )
        }
    }
}

@Composable
fun CustomerTopBar(
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onAddressClick: () -> Unit,
    cartSize: Int,
    address: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).clickable { onAddressClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ManaGold.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocationOn, null, tint = ManaGold, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bellampally", style = MaterialTheme.typography.labelLarge, color = ManaGold, fontWeight = FontWeight.Black)
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = ManaGold, modifier = Modifier.size(16.dp))
                }
                Text(address, style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary, maxLines = 1)
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Rounded.Person, null, tint = ManaTextPrimary)
            }
            BadgedBox(
                badge = {
                    if (cartSize > 0) {
                        Badge(containerColor = ManaRedStrong) { Text(cartSize.toString(), color = Color.White) }
                    }
                }
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.ShoppingCart, null, tint = ManaTextPrimary)
                }
            }
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C050B), ManaBgPrimary)
                )
            )
    ) {
        // Watermark Logo
        Icon(
            Icons.Rounded.Storefront,
            null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(120.dp)
                .offset(x = 20.dp, y = 20.dp),
            tint = Color.White.copy(alpha = 0.05f)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = ManaGold,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "FREE DELIVERY",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Super Fast Delivery\nin Bellampally",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Get groceries & food in 20 mins",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CategoriesGrid(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onCategoryClick(category.id) }
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = ManaBgCard,
                    border = BorderStroke(1.dp, ManaBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(category.name.take(1), fontWeight = FontWeight.Bold, color = ManaGold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(category.name, style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
            }
        }
    }
}

@Composable
fun CustomerBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = ManaBgCard,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Rounded.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaGold.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Rounded.ListAlt, null) },
            label = { Text("Orders") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaGold.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Rounded.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = ManaGold, indicatorColor = ManaGold.copy(alpha = 0.1f))
        )
    }
}
