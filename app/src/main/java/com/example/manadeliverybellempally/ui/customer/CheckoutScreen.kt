package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val products by viewModel.products.collectAsState()
    val subtotal = viewModel.getCartSubtotal()
    val deliveryFee = viewModel.getDeliveryFee()
    val discount = viewModel.getDiscountAmount()
    val total = viewModel.getCartTotal()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val currentUser by viewModel.currentUser.collectAsState()
    val orderError by viewModel.orderError.collectAsState()
    val orderSuccess by viewModel.orderSuccess.collectAsState()
    
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponError by viewModel.couponError.collectAsState()

    var address by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var showAddressPicker by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }
    var showSavedAddresses by remember { mutableStateOf(false) }
    
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var showMockPaymentGateway by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(orderError) {
        orderError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOrderState()
        }
    }

    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            onOrderPlaced()
            viewModel.clearOrderState()
        }
    }

    Scaffold(
        topBar = {
            ManaHeader(
                title = "Checkout",
                subtitle = "Complete your order",
                showBackButton = true,
                onBack = onBack
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
                ManaGradientButton(
                    text = if (isLoading) "PLACING ORDER..." else "PAY ₹${total.toInt()} & PLACE ORDER",
                    onClick = {
                        if (selectedPaymentMethod == "UPI") {
                            showMockPaymentGateway = true
                        } else {
                            viewModel.placeOrder("COD", address + (if (landmark.isNotEmpty()) ", Landmark: $landmark" else ""))
                        }
                    },
                    icon = Icons.Rounded.CheckCircle,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isLoading
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ManaBgPrimary
    ) { padding ->
        
        if (showMockPaymentGateway) {
            AlertDialog(
                onDismissRequest = { showMockPaymentGateway = false },
                title = { Text("Mock Razorpay PG", fontWeight = FontWeight.Bold, color = ManaTextPrimary) },
                text = { Text("Simulate a successful UPI payment of ₹${total.toInt()}?", color = ManaTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showMockPaymentGateway = false
                            viewModel.placeOrder("UPI", address + (if (landmark.isNotEmpty()) ", Landmark: $landmark" else ""))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ManaGold)
                    ) {
                        Text("Pay Success")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showMockPaymentGateway = false
                        scope.launch { snackbarHostState.showSnackbar("Payment Failed") }
                    }) {
                        Text("Simulate Failure", color = ManaRedStrong)
                    }
                },
                containerColor = ManaBgCard
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Order Summary", subtitle = "Items from your cart")
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
                            Column(Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.bodyMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                                Text("₹${product.price.toInt()} per unit", style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                            }
                            Text("x$qty", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
                            Spacer(Modifier.width(16.dp))
                            Text("₹${(product.price * qty).toInt()}", style = MaterialTheme.typography.titleSmall, color = ManaGold, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Coupons & Offers", subtitle = "Save on your order")
                ManaCard {
                    if (appliedCoupon != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Coupon Applied: ${appliedCoupon!!.code}", style = MaterialTheme.typography.bodyMedium, color = ManaGold, fontWeight = FontWeight.Bold)
                                Text("You saved ₹${discount.toInt()}", style = MaterialTheme.typography.labelSmall, color = ManaSuccess)
                            }
                            TextButton(onClick = { 
                                viewModel.removeCoupon() 
                                couponCode = ""
                            }) {
                                Text("REMOVE", color = ManaRedStrong)
                            }
                        }
                    } else {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = couponCode,
                                    onValueChange = { couponCode = it.uppercase() },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Enter coupon code") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ManaGold,
                                        unfocusedBorderColor = ManaBorder,
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.applyCoupon(couponCode) },
                                    enabled = couponCode.isNotBlank() && !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = ManaGold, contentColor = ManaBgPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("APPLY")
                                }
                            }
                            if (couponError != null) {
                                Text(couponError!!, color = ManaRedStrong, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            item {
                ManaCard(containerColor = ManaGold.copy(alpha = 0.05f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item Total", color = ManaTextSecondary)
                            Text("₹${subtotal.toInt()}", color = ManaTextPrimary)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", color = ManaTextSecondary)
                            Text("₹${deliveryFee.toInt()}", color = ManaTextPrimary)
                        }
                        if (discount > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", color = ManaSuccess)
                                Text("- ₹${discount.toInt()}", color = ManaSuccess)
                            }
                        }
                        Divider(color = ManaBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total to Pay", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                            Text("₹${total.toInt()}", style = MaterialTheme.typography.headlineSmall, color = ManaGold, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Delivery Location", subtitle = "Where should we deliver?")
                
                val hasSavedAddresses = currentUser?.savedAddresses?.isNotEmpty() == true
                
                ManaCard(
                    onClick = { 
                        if (hasSavedAddresses) showSavedAddresses = !showSavedAddresses
                        else showAddressPicker = true 
                    },
                    border = BorderStroke(1.dp, if (address.isEmpty()) ManaRedStrong.copy(alpha = 0.5f) else ManaBorder)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocationOn, null, tint = ManaGold)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (address.isEmpty()) "Select Delivery Address" else address,
                                    fontWeight = FontWeight.Bold,
                                    color = if (address.isEmpty()) ManaTextTertiary else ManaTextPrimary
                                )
                                Text(if (hasSavedAddresses) "Tap to change" else "Tap to pick from landmarks", style = MaterialTheme.typography.labelSmall, color = ManaGold)
                            }
                        }
                        
                        if (showSavedAddresses && hasSavedAddresses) {
                            Divider(Modifier.padding(vertical = 12.dp), color = ManaBorder)
                            currentUser?.savedAddresses?.forEach { saved ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            address = saved.address
                                            showSavedAddresses = false
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = ManaTextTertiary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(saved.label, fontWeight = FontWeight.Bold, color = ManaTextPrimary, fontSize = 14.sp)
                                        Text(saved.address, color = ManaTextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                            TextButton(
                                onClick = { showAddressPicker = true; showSavedAddresses = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Add New Address", color = ManaGold)
                            }
                        }
                    }
                }

                if (address.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("House No / Flat / Landmark (Optional)") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ManaGold,
                            unfocusedBorderColor = ManaBorder,
                            focusedTextColor = ManaTextPrimary,
                            unfocusedTextColor = ManaTextPrimary,
                            focusedContainerColor = ManaBgCard,
                            unfocusedContainerColor = ManaBgCard
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Payment Method", subtitle = "How would you like to pay?")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ManaCard(
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPaymentMethod = "UPI" },
                        border = BorderStroke(2.dp, if (selectedPaymentMethod == "UPI") ManaGold else Color.Transparent),
                        containerColor = if (selectedPaymentMethod == "UPI") ManaGold.copy(alpha = 0.1f) else ManaBgCard
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text("UPI / Cards", fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                            Text("Pay Online", style = MaterialTheme.typography.labelSmall, color = ManaSuccess)
                        }
                    }
                    
                    ManaCard(
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPaymentMethod = "COD" },
                        border = BorderStroke(2.dp, if (selectedPaymentMethod == "COD") ManaGold else Color.Transparent),
                        containerColor = if (selectedPaymentMethod == "COD") ManaGold.copy(alpha = 0.1f) else ManaBgCard
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text("Cash on Delivery", fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                            Text("Pay at door", style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }

        if (showAddressPicker) {
            HyperlocalAddressPicker(
                onAddressSelected = { 
                    address = it
                    showAddressPicker = false
                },
                onDismiss = { showAddressPicker = false }
            )
        }
    }
}
