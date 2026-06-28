package com.example.manadeliverybellempally.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.AdminSettings
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var editableSettings by remember(settings) { mutableStateOf(settings) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateSettings(editableSettings) }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("SERVICE AVAILABILITY", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
            ManaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("App Service Enabled", style = MaterialTheme.typography.bodyLarge)
                        Text("Turn off to show maintenance message", style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                    }
                    Switch(checked = editableSettings.isServiceEnabled, onCheckedChange = { editableSettings = editableSettings.copy(isServiceEnabled = it) })
                }
            }

            Text("FEES & COMMISSIONS", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
            ManaCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingField(label = "Base Delivery Fee (₹)", value = editableSettings.deliveryFeeBase.toString(), onValueChange = { editableSettings = editableSettings.copy(deliveryFeeBase = it.toDoubleOrNull() ?: 0.0) })
                    SettingField(label = "Default Commission (%)", value = editableSettings.globalCommissionRate.toString(), onValueChange = { editableSettings = editableSettings.copy(globalCommissionRate = it.toDoubleOrNull() ?: 0.0) })
                    SettingField(label = "Tax Percentage (%)", value = editableSettings.taxPercentage.toString(), onValueChange = { editableSettings = editableSettings.copy(taxPercentage = it.toDoubleOrNull() ?: 0.0) })
                }
            }
            
            Spacer(Modifier.height(32.dp))
            ManaButton(
                text = "Save Settings",
                onClick = { viewModel.updateSettings(editableSettings) },
                icon = Icons.Default.Save,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingField(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(100.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoleAdmin)
        )
    }
}
