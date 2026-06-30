package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val balance = user?.walletBalance ?: 0.0
    val transactions by viewModel.walletTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", color = ManaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ManaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                
                // Wallet Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ManaGold, ManaRed.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text("Total Balance", color = ManaTextPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("₹${balance.toInt()}", color = ManaTextPrimary, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    }
                    Icon(
                        Icons.Default.AccountBalanceWallet, 
                        null, 
                        modifier = Modifier.size(64.dp).align(Alignment.BottomEnd),
                        tint = ManaTextPrimary.copy(alpha = 0.15f)
                    )
                }

                Spacer(Modifier.height(32.dp))

                ManaButton(
                    text = "Add Money (Coming Soon)",
                    onClick = { /* Simulated Topup */ },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(48.dp))
                
                Text("Recent Transactions", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                Spacer(Modifier.height(16.dp))
            }
            
            if (transactions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "No transactions yet",
                        subtitle = "Your wallet activity will show up here."
                    )
                }
            } else {
                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                items(transactions) { tx ->
                    ManaCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (tx.type == "EARNED") "Cashback Earned" else "Points Redeemed",
                                    fontWeight = FontWeight.Bold,
                                    color = ManaTextPrimary
                                )
                                Text(
                                    text = dateFormat.format(Date(tx.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ManaTextSecondary
                                )
                            }
                            Text(
                                text = (if (tx.type == "EARNED") "+" else "-") + " ₹${tx.amount.toInt()}",
                                fontWeight = FontWeight.Black,
                                color = if (tx.type == "EARNED") ManaSuccess else ManaRedStrong
                            )
                        }
                    }
                }
            }
        }
    }
}
