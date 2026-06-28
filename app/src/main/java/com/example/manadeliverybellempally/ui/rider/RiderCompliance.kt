package com.example.manadeliverybellempally.ui.rider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaButton
import com.example.manadeliverybellempally.ui.common.ManaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderComplianceScreen(
    viewModel: RiderViewModel,
    onBack: () -> Unit
) {
    val rider by viewModel.rider.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Info") },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("VEHICLE DETAILS", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
            }

            item {
                VehicleInfoCard(
                    vehicleNumber = rider?.vehicleNumber ?: "",
                    onUpdate = { n -> viewModel.updateVehicle("bike", n, "") }
                )
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(
    vehicleNumber: String,
    onUpdate: (String) -> Unit
) {
    var number by remember { mutableStateOf(vehicleNumber) }

    ManaCard(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Vehicle Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ManaGold)
        )
        Spacer(Modifier.height(16.dp))
        ManaButton(text = "Save Vehicle Info", onClick = { onUpdate(number) }, modifier = Modifier.fillMaxWidth())
    }
}
