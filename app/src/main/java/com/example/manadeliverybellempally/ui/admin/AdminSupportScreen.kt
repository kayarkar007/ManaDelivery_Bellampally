package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.SupportTicket
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val tickets by viewModel.tickets.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var resolvingTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var resolutionNotes by remember { mutableStateOf("") }

    val filteredTickets = when (selectedFilter) {
        "OPEN" -> tickets.filter { it.status == "OPEN" }
        "RESOLVED" -> tickets.filter { it.status == "RESOLVED" }
        else -> tickets
    }
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Center", color = ManaTextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "OPEN", "RESOLVED").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ManaGold,
                                selectedLabelColor = ManaBgPrimary
                            )
                        )
                    }
                }
            }

            if (filteredTickets.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.SupportAgent,
                        title = "No tickets found",
                        subtitle = "All clear! No support tickets match this filter."
                    )
                }
            }

            items(filteredTickets, key = { it.id }) { ticket ->
                ManaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                ticket.subject.ifEmpty { ticket.issueType },
                                fontWeight = FontWeight.Bold,
                                color = ManaTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ticket.status == "OPEN") ManaRedStrong.copy(alpha = 0.1f) else ManaSuccess.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    ticket.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (ticket.status == "OPEN") ManaRedStrong else ManaSuccess,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(ticket.description, color = ManaTextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${ticket.userName} (${ticket.userRole})", color = ManaTextTertiary, style = MaterialTheme.typography.labelSmall)
                            Text(dateFormat.format(Date(ticket.createdAt)), color = ManaTextTertiary, style = MaterialTheme.typography.labelSmall)
                        }
                        if (ticket.status == "OPEN") {
                            Spacer(Modifier.height(8.dp))
                            ManaButton(
                                text = "Resolve",
                                onClick = { resolvingTicket = ticket },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (ticket.internalNotes.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text("Notes: ${ticket.internalNotes}", color = ManaGold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    // Resolve Dialog
    if (resolvingTicket != null) {
        AlertDialog(
            onDismissRequest = { resolvingTicket = null },
            title = { Text("Resolve Ticket", color = ManaTextPrimary) },
            text = {
                OutlinedTextField(
                    value = resolutionNotes,
                    onValueChange = { resolutionNotes = it },
                    label = { Text("Resolution Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    resolvingTicket?.let { viewModel.resolveTicket(it, resolutionNotes) }
                    resolvingTicket = null
                    resolutionNotes = ""
                }) { Text("Resolve", color = ManaGold) }
            },
            dismissButton = {
                TextButton(onClick = { resolvingTicket = null; resolutionNotes = "" }) {
                    Text("Cancel", color = ManaTextSecondary)
                }
            },
            containerColor = ManaBgCard
        )
    }
}
