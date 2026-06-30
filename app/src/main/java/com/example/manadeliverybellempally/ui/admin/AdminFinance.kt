package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.data.model.*
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.EmptyState
import com.example.manadeliverybellempally.ui.common.ManaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPayoutScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val allVendors by viewModel.allVendors.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Control", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(Modifier.padding(padding)) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = ManaBgPrimary,
                contentColor = ManaGold
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Settlements") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("History") })
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("PENDING VENDOR PAYOUTS", style = MaterialTheme.typography.labelMedium, color = ManaGold, letterSpacing = 2.sp)
                    }
                    items(allVendors) { vendor ->
                        val vendorOrders = allOrders.filter { it.vendorId == vendor.id && it.status == "DELIVERED" }
                        val balance = vendorOrders.sumOf { it.total * 0.9 }
                        VendorPayoutItem(vendor, balance)
                    }
                }
            } else {
                EmptyState(icon = Icons.Rounded.History, title = "Transaction History", subtitle = "Past settlements and payouts will appear here.")
            }
        }
    }
}

@Composable
private fun VendorPayoutItem(vendor: Vendor, balance: Double) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(vendor.storeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Settlement Balance: ₹${balance.toInt()}", style = MaterialTheme.typography.bodySmall, color = if(balance > 0) ManaSuccess else ManaTextTertiary)
            }
            Button(
                onClick = { /* Future: Execute payout */ }, 
                colors = ButtonDefaults.buttonColors(containerColor = ManaGold), 
                enabled = balance > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SETTLE", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
