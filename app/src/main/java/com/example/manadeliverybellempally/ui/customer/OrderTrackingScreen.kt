package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import com.example.manadeliverybellempally.data.model.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onSupportClick: (String) -> Unit = {}
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }
    val isLoading by viewModel.isLoading.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    
    // Live timer for cancellation window and ETA
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order", color = ManaTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (order == null) {
            EmptyState(icon = Icons.Rounded.Search, title = "Order not found", subtitle = "We couldn't retrieve the details for this order.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ManaCard(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("CURRENT STATUS", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                                Text(order.status.replace("_", " "), style = MaterialTheme.typography.headlineSmall, color = ManaGold, fontWeight = FontWeight.Black)
                            }
                            OrderStatusChip(status = order.status)
                        }
                        
                        // ETA calculation
                        if (order.status != "DELIVERED" && order.status != "CANCELLED") {
                            val elapsedMinutes = (currentTime - order.createdAt) / (60 * 1000)
                            val remainingMinutes = (order.estimatedDeliveryMinutes - elapsedMinutes).coerceAtLeast(0)
                            
                            Spacer(Modifier.height(16.dp))
                            Divider(color = ManaBorder.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, tint = ManaGold)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Estimated Arrival", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                                    if (remainingMinutes > 0) {
                                        Text("$remainingMinutes mins", style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Arriving any moment", style = MaterialTheme.typography.titleMedium, color = ManaSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (order.status == "OUT_FOR_DELIVERY" || order.status == "READY_FOR_PICKUP") {
                    item {
                        Text("LIVE TRACKING", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = ManaBgCard,
                            border = BorderStroke(1.dp, ManaBorder)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Map Grid Background
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                                    repeat(5) { HorizontalDivider(color = ManaBorder.copy(alpha = 0.5f)) }
                                }
                                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    repeat(5) { Divider(color = ManaBorder.copy(alpha = 0.5f), modifier = Modifier.width(1.dp).fillMaxHeight()) }
                                }
                                
                                // Route Line
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(4.dp)
                                        .height(100.dp),
                                    color = ManaGold.copy(alpha = 0.5f)
                                ) {}

                                // Destination Marker
                                Icon(
                                    Icons.Rounded.LocationOn,
                                    contentDescription = "Destination",
                                    tint = ManaRedStrong,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 32.dp)
                                        .size(32.dp)
                                )

                                // Rider Marker (Simulated live location)
                                val animateY by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = if (order.riderLocationLat > 0.0) 0f else 1f, // Just a simple mock animation state
                                    animationSpec = androidx.compose.animation.core.tween(1000)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 32.dp)
                                ) {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = ManaGold,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Rounded.DirectionsBike, contentDescription = "Rider", tint = ManaBgPrimary, modifier = Modifier.padding(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (order.deliveryOtp.isNotEmpty() && order.status != "DELIVERED") {
                    item {
                        Surface(
                            color = ManaGold.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, ManaGold.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DELIVERY OTP", style = MaterialTheme.typography.labelSmall, color = ManaGold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(order.deliveryOtp, style = MaterialTheme.typography.displayMedium, color = ManaGold, fontWeight = FontWeight.Black, letterSpacing = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Share this with the rider only at your doorstep", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                            }
                        }
                    }
                }

                item {
                    Text("TIMELINE", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
                    ManaCard(modifier = Modifier.fillMaxWidth()) {
                        val timeline = order.statusTimeline.toList().sortedByDescending { it.second }
                        timeline.forEachIndexed { index, (status, time) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Row(Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (index == 0) ManaSuccess else ManaTextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(status.replace("_", " "), style = MaterialTheme.typography.bodyMedium, color = if(index == 0) ManaTextPrimary else ManaTextSecondary, fontWeight = if(index == 0) FontWeight.Bold else FontWeight.Normal)
                                        Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(time)), style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                                    }
                                }
                            }
                            if (index < timeline.size - 1) {
                                HorizontalDivider(color = ManaBorder.copy(alpha = 0.3f), modifier = Modifier.padding(start = 32.dp))
                            }
                        }
                    }
                }

                // Action Buttons / Sections
                item {
                    // Cancel Window
                    if (order.status == "PLACED" && (currentTime - order.createdAt) < Order.CANCEL_WINDOW_MS) {
                        val secondsLeft = (Order.CANCEL_WINDOW_MS - (currentTime - order.createdAt)) / 1000
                        Button(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ManaRedStrong)
                        ) {
                            Text("Cancel Order (0:${secondsLeft.toString().padStart(2, '0')})")
                        }
                    }
                    
                    // Reorder
                    if (order.status == "DELIVERED" || order.status == "CANCELLED") {
                        ManaGradientButton(
                            text = "Reorder",
                            icon = Icons.Rounded.Refresh,
                            onClick = {
                                viewModel.reorder(order.id)
                                onBack() // Send user back to cart/checkout flow
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = isLoading
                        )
                    }
                    
                    // Ratings & Reviews
                    if (order.status == "DELIVERED" && !order.isReviewed) {
                        Spacer(Modifier.height(16.dp))
                        SectionHeader(title = "Rate your experience", subtitle = "Help us improve")
                        ManaCard(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    (1..5).forEach { i ->
                                        IconButton(onClick = { rating = i }) {
                                            Icon(
                                                imageVector = if (i <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                                contentDescription = null,
                                                tint = ManaGold,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = reviewComment,
                                    onValueChange = { reviewComment = it },
                                    placeholder = { Text("Write a review (optional)") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold)
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.submitReview(order.id, order.vendorId, rating, reviewComment) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ManaGold, contentColor = ManaBgPrimary),
                                    enabled = !isLoading
                                ) {
                                    Text("Submit Review")
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        OutlinedButton(
                            onClick = { onSupportClick(order.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ManaRed),
                            border = BorderStroke(1.dp, ManaRed.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Rounded.SupportAgent, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Report Issue with Order")
                        }
                    }
                }
                
                item { Spacer(Modifier.height(32.dp)) }
            }
            
            if (showCancelDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text("Cancel Order", color = ManaTextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Are you sure you want to cancel this order?", color = ManaTextSecondary)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = cancelReason,
                                onValueChange = { cancelReason = it },
                                placeholder = { Text("Reason for cancellation (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.cancelOrder(order.id, if (cancelReason.isBlank()) "Customer changed mind" else cancelReason)
                                showCancelDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ManaRedStrong)
                        ) {
                            Text("Confirm Cancel")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("Keep Order", color = ManaTextTertiary)
                        }
                    },
                    containerColor = ManaBgCard
                )
            }
        }
    }
}
