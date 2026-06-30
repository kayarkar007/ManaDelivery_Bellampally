package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaCard
import com.example.manadeliverybellempally.ui.common.ManaGradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    orderId: String?,
    viewModel: CustomerViewModel,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    
    var issueType by remember { mutableStateOf("Late Delivery") }
    val issueTypes = listOf("Late Delivery", "Missing Items", "Food Spilled/Damaged", "Payment Issue", "Other")
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Support", fontWeight = FontWeight.Bold, color = ManaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ManaCard {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.SupportAgent, contentDescription = null, tint = ManaGold, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("How can we help you?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("We usually respond within 5 minutes.", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                    }
                }
            }

            if (orderId != null) {
                Text("Regarding Order: #${orderId.takeLast(6).uppercase()}", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = issueType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Issue Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, focusedLabelColor = ManaGold),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    issueTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                issueType = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe the issue in detail") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold, focusedLabelColor = ManaGold)
            )

            Spacer(Modifier.weight(1f))

            ManaGradientButton(
                text = "Submit Ticket",
                icon = Icons.Rounded.SupportAgent,
                onClick = {
                    viewModel.createSupportTicket(orderId ?: "", issueType, description)
                    onSubmitSuccess()
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoading
            )
        }
    }
}
