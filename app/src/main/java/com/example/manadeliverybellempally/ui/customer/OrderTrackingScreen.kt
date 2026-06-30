package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onSupportClick: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }
    val isLoading by viewModel.isLoading.collectAsState()

    var rating by remember { mutableFloatStateOf(5f) }
    var reviewComment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ManaHeader(
                title = "Track Order",
                subtitle = "Status: ${order?.status ?: "Unknown"}",
                showBackButton = true,
                onBack = onBack
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (order == null) {
            EmptyState(icon = Icons.Rounded.Search, title = "Order Not Found", subtitle = "Could not find order details.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OrderOverviewCard(order)
                }

                item {
                    StatusTimeline(order)
                }

                if (order.status == "DELIVERED") {
                    item {
                        ReviewCard(
                            rating = rating,
                            onRatingChange = { rating = it },
                            comment = reviewComment,
                            onCommentChange = { reviewComment = it },
                            onSubmit = { viewModel.submitReview(order.id, order.vendorId, rating.toInt(), reviewComment) },
                            isLoading = isLoading
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
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
                
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
fun OrderOverviewCard(order: Order) {
    ManaCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("ORDER ID", style = MaterialTheme.typography.labelSmall, color = ManaGold)
                OrderStatusChip(order.status)
            }
            Text("#${order.id.takeLast(8).uppercase()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            
            HorizontalDivider(color = ManaBorder)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Store, null, tint = ManaGold)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Vendor", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                    Text(order.vendorName, fontWeight = FontWeight.Bold)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = ManaGold)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Delivery To", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                    Text(order.deliveryAddress, fontWeight = FontWeight.Bold, maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun StatusTimeline(order: Order) {
    val statuses = listOf("PLACED", "CONFIRMED", "PREPARING", "READY", "PICKED_UP", "DELIVERED")
    val currentIndex = statuses.indexOf(order.status)
    
    ManaCard {
        Column {
            Text("ORDER PROGRESS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))
            statuses.forEachIndexed { index, status ->
                TimelineItem(
                    status = status,
                    isCompleted = index <= currentIndex,
                    isLast = index == statuses.size - 1
                )
            }
        }
    }
}

@Composable
fun TimelineItem(status: String, isCompleted: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) ManaSuccess else ManaBorder)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (isCompleted) ManaSuccess else ManaBorder)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(
                status.replace("_", " "),
                fontWeight = if (isCompleted) FontWeight.Black else FontWeight.Normal,
                color = if (isCompleted) ManaTextPrimary else ManaTextTertiary
            )
        }
    }
}

@Composable
fun ReviewCard(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    ManaCard {
        Column {
            Text("RATE YOUR EXPERIENCE", style = MaterialTheme.typography.labelMedium, color = ManaGold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                repeat(5) { i ->
                    val starIndex = i + 1
                    IconButton(onClick = { onRatingChange(starIndex.toFloat()) }) {
                        Icon(
                            imageVector = if (rating >= starIndex) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = if (rating >= starIndex) ManaGold else ManaBorder,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("How was the food and delivery?") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold)
            )
            Spacer(Modifier.height(16.dp))
            ManaGradientButton(text = "SUBMIT REVIEW", onClick = onSubmit, isLoading = isLoading)
        }
    }
}
