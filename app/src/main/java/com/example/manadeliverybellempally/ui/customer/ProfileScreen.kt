package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.R
import com.example.manadeliverybellempally.data.model.User
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaCard
import com.example.manadeliverybellempally.ui.common.ManaOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: CustomerViewModel,
    onAddressClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onWalletClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account", color = ManaTextPrimary) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(user = currentUser)
            }

            item {
                WalletSummaryCard(
                    balance = currentUser?.walletBalance ?: 0.0,
                    onWalletClick = onWalletClick
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "ACCOUNT SETTINGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = ManaGold,
                    modifier = Modifier.fillMaxWidth(),
                    letterSpacing = 2.sp
                )
            }

            item {
                ProfileOptionItem(
                    icon = Icons.Outlined.ShoppingBag,
                    label = "My Orders",
                    onClick = onOrdersClick
                )
            }

            item {
                ProfileOptionItem(
                    icon = Icons.Outlined.LocationOn,
                    label = "Manage Addresses",
                    onClick = onAddressClick
                )
            }

            item {
                ProfileOptionItem(
                    icon = Icons.Outlined.HelpOutline,
                    label = "Help & Support",
                    onClick = { /* Implement Support */ }
                )
            }

            item {
                ProfileOptionItem(
                    icon = Icons.Outlined.Info,
                    label = "About Mana Delivery",
                    onClick = { /* Show About */ }
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                ManaOutlinedButton(
                    text = "Logout",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Filled.Logout
                )
            }
            
            item {
                Text(
                    "Version 1.0.0 (Production)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ManaTextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(ManaRedStrong, ManaGold)
                    )
                )
                .border(2.dp, ManaGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            user?.name ?: "Customer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ManaTextPrimary
        )
        Text(
            user?.phone ?: "Not Available",
            style = MaterialTheme.typography.bodyMedium,
            color = ManaGold
        )
    }
}

@Composable
private fun WalletSummaryCard(balance: Double, onWalletClick: () -> Unit) {
    ManaCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onWalletClick,
        border = BorderStroke(1.dp, ManaGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ManaGold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = ManaGold)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Mana Wallet", style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                    Text("₹${balance.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = ManaTextPrimary)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = ManaGoldDim)
        }
    }
}

@Composable
private fun ProfileOptionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    ManaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = RoleCustomer, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = ManaTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = ManaTextTertiary)
        }
    }
}
