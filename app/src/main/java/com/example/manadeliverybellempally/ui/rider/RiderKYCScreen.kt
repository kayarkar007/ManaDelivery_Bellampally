package com.example.manadeliverybellempally.ui.rider

import androidx.compose.foundation.layout.*
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
fun RiderKYCScreen(
    viewModel: RiderViewModel,
    onBack: () -> Unit
) {
    val rider by viewModel.rider.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KYC Details") },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Vehicle Information", style = MaterialTheme.typography.titleMedium, color = ManaGold)
            Spacer(Modifier.height(16.dp))
            ManaCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Vehicle Number: ${rider?.vehicleNumber.orEmpty()}", color = ManaTextPrimary)
                }
            }
        }
    }
}
