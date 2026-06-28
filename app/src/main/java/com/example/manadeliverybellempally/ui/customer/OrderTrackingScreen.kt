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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }

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
                
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}
