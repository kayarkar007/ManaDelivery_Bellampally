package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.example.manadeliverybellempally.data.model.User
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }

    val filteredUsers = users.filter { 
        (selectedRoleFilter == "ALL" || it.role == selectedRoleFilter) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Directory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            AdminSearchField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Search name or phone...")
            
            Spacer(Modifier.height(12.dp))
            
            // Role Filter Chips
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "CUSTOMER", "RIDER", "VENDOR", "ADMIN").forEach { role ->
                    FilterChip(
                        selected = selectedRoleFilter == role,
                        onClick = { selectedRoleFilter = role },
                        label = { Text(role, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ManaGold,
                            selectedLabelColor = Color.Black,
                            containerColor = ManaBgCard,
                            labelColor = ManaTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedRoleFilter == role, borderColor = ManaBorder, borderWidth = 1.dp, selectedBorderColor = ManaGold)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredUsers) { user ->
                    UserCommandCard(
                        user = user, 
                        onToggleBlock = { viewModel.toggleUserBlock(user) },
                        onUpdateRole = { /* Future: Admin capability to change roles */ }
                    )
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun UserCommandCard(
    user: User,
    onToggleBlock: () -> Unit,
    onUpdateRole: () -> Unit
) {
    val roleColor = when(user.role) {
        "VENDOR" -> RoleVendor
        "RIDER" -> RoleRider
        "ADMIN" -> RoleAdmin
        else -> ManaGold
    }

    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Initials Circle
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(roleColor.copy(alpha = 0.15f)).border(1.dp, roleColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(user.name.take(1).uppercase(), color = roleColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium, color = if (user.isBlocked) ManaTextTertiary else ManaTextPrimary, fontWeight = FontWeight.Bold)
                    if (user.isBlocked) {
                        Spacer(Modifier.width(8.dp))
                        AdminStatusBadge(text = "BLOCKED", color = ManaError)
                    }
                }
                Text("+91 ${user.phone}", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                
                Spacer(Modifier.height(4.dp))
                Surface(color = roleColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        user.role.uppercase(), 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, 
                        color = roleColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Quick Actions
            Row {
                IconButton(onClick = onToggleBlock) {
                    Icon(
                        imageVector = if (user.isBlocked) Icons.Rounded.LockOpen else Icons.Rounded.Block,
                        contentDescription = null,
                        tint = if (user.isBlocked) ManaSuccess else ManaError,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { /* View Detail */ }) {
                    Icon(Icons.Rounded.ChevronRight, null, tint = ManaTextTertiary)
                }
            }
        }
    }
}
