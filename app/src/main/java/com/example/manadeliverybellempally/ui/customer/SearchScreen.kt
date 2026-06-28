package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: CustomerViewModel,
    onVendorClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cart by viewModel.cart.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        placeholder = { Text("Search dishes, groceries...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = ManaGold) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, null, tint = ManaTextTertiary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ManaGold,
                            unfocusedBorderColor = ManaBorder,
                            focusedContainerColor = ManaBgInput,
                            unfocusedContainerColor = ManaBgInput
                        )
                    )
                },
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
            if (searchQuery.isBlank()) {
                item {
                    Text(
                        "DISCOVER CATEGORIES",
                        style = MaterialTheme.typography.labelMedium,
                        color = ManaGold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            SuggestionChip(
                                onClick = { viewModel.onSearchQueryChange(category.name) },
                                label = { Text(category.name) },
                                shape = RoundedCornerShape(12.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = ManaTextPrimary,
                                    containerColor = ManaBgCard
                                ),
                                border = BorderStroke(1.dp, ManaBorder)
                            )
                        }
                    }
                }

                item {
                    EmptyState(
                        icon = Icons.Rounded.Search,
                        title = "What are you looking for?",
                        subtitle = "Find the best food and groceries in Bellempally instantly."
                    )
                }
            } else if (filteredProducts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.SentimentVeryDissatisfied,
                        title = "No matches found",
                        subtitle = "Try searching for something else or check your spelling."
                    )
                }
            } else {
                item {
                    Text(
                        "SEARCH RESULTS (${filteredProducts.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = ManaTextTertiary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(filteredProducts) { product ->
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
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
