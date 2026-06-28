package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportCenterScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val tickets by viewModel.tickets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        if (tickets.isEmpty()) {
            EmptyState(icon = Icons.AutoMirrored.Filled.HelpCenter, title = "All Clear", subtitle = "No active support tickets from users.")
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tickets) { ticket ->
                    ManaCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(ticket.subject, style = MaterialTheme.typography.titleMedium)
                            Text(ticket.description, style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                            Spacer(Modifier.height(8.dp))
                            AdminStatusBadge(text = ticket.status, color = if (ticket.status == "OPEN") ManaWarning else ManaSuccess)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            EmptyState(icon = Icons.Default.Insights, title = "Coming Soon", subtitle = "Detailed charts and PDF reports are being generated.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    // Audit logs state would be here
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            EmptyState(icon = Icons.Default.History, title = "Under Development", subtitle = "Tracking all administrative actions for transparency.")
        }
    }
}
