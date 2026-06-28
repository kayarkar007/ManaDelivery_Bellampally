package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaButton
import com.example.manadeliverybellempally.ui.common.ManaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val products by viewModel.products.collectAsState()
    val total = viewModel.getCartTotal()
    val isLoading by viewModel.isLoading.collectAsState()

    var address by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = ManaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ManaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ManaBgCard)
                    .padding(16.dp)
                    .safeDrawingPadding()
            ) {
                ManaButton(
                    text = if (isLoading) "PLACING ORDER..." else "PLACE ORDER • ₹${total.toInt()}",
                    onClick = {
                        viewModel.placeOrder("COD", address + (if (landmark.isNotEmpty()) ", Landmark: $landmark" else ""))
                        onOrderPlaced()
                    },
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && cart.isNotEmpty() && address.isNotBlank()
                )
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
                Spacer(Modifier.height(8.dp))
                Text("Order Summary", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
            }

            items(cart.toList()) { (productId, qty) ->
                val product = products.find { it.id == productId }
                if (product != null) {
                    ManaCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(product.name, style = MaterialTheme.typography.bodyMedium, color = ManaTextPrimary)
                            Text("x$qty", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
                            Text("₹${(product.price * qty).toInt()}", style = MaterialTheme.typography.titleSmall, color = ManaGold)
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = ManaBorder, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total to Pay", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                    Text("₹${total.toInt()}", style = MaterialTheme.typography.titleLarge, color = ManaSuccess, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Delivery Address", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("House No, Street, Area") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ManaGold,
                        focusedTextColor = ManaTextPrimary,
                        unfocusedTextColor = ManaTextPrimary
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Landmark (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ManaGold,
                        focusedTextColor = ManaTextPrimary,
                        unfocusedTextColor = ManaTextPrimary
                    )
                )
            }
        }
    }
}
