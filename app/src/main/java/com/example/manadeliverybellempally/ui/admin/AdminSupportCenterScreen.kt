package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.SupportTicket
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaCard
import com.example.manadeliverybellempally.ui.common.ManaGradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportCenterScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val tickets by viewModel.tickets.collectAsState()
    val openTickets = tickets.filter { it.status == "OPEN" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Center", color = ManaTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("OPEN TICKETS (${openTickets.size})", style = MaterialTheme.typography.labelMedium, color = ManaGold)
            }

            if (openTickets.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = ManaSuccess, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("All caught up!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("No pending support tickets.", style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
                    }
                }
            } else {
                items(openTickets) { ticket ->
                    SupportTicketCard(ticket = ticket, onResolve = { resolution ->
                        viewModel.resolveTicket(ticket, resolution)
                    })
                }
            }
        }
    }
}

@Composable
fun SupportTicketCard(ticket: SupportTicket, onResolve: (String) -> Unit) {
    var resolution by remember { mutableStateOf("") }
    
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(ticket.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Role: ${ticket.userRole} | ID: ${ticket.userId.takeLast(6).uppercase()}", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                }
                Surface(
                    color = ManaRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        ticket.issueType.ifBlank { "ISSUE" },
                        color = ManaRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            
            if (ticket.orderId.isNotEmpty()) {
                Text("Order: #${ticket.orderId.takeLast(6).uppercase()}", style = MaterialTheme.typography.bodySmall, color = ManaGold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
            }
            
            Text(ticket.description.ifBlank { "No description provided." }, style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary)
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = ManaBorder.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = resolution,
                onValueChange = { resolution = it },
                label = { Text("Resolution Notes") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = {
                    if (resolution.isNotBlank()) {
                        onResolve(resolution)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ManaGold, contentColor = ManaBgPrimary),
                enabled = resolution.isNotBlank()
            ) {
                Icon(Icons.Rounded.SupportAgent, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Mark Resolved")
            }
        }
    }
}
