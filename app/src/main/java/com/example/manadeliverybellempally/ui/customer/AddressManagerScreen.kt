package com.example.manadeliverybellempally.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.Address
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaButton
import com.example.manadeliverybellempally.ui.common.ManaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagerScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Addresses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ManaBgPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = ManaGold) {
                Icon(Icons.Default.Add, "Add Address")
            }
        },
        containerColor = ManaBgPrimary
    ) { padding ->
        val addresses = currentUser?.addresses ?: emptyList()

        if (addresses.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No saved addresses yet.", color = ManaTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses) { address ->
                    AddressItem(
                        address = address,
                        onDelete = { viewModel.removeAddress(address.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddressDialog(
            onDismiss = { showAddDialog = false },
            onSave = { label, full, landmark ->
                viewModel.saveAddress(Address(label = label, fullAddress = full, landmark = landmark))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddressItem(address: Address, onDelete: () -> Unit) {
    ManaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when(address.label.uppercase()) {
                    "HOME" -> Icons.Default.Home
                    "WORK" -> Icons.Default.Work
                    else -> Icons.Default.LocationOn
                },
                contentDescription = null,
                tint = ManaGold
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(address.label, style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary)
                Text(address.fullAddress, style = MaterialTheme.typography.bodySmall, color = ManaTextSecondary)
                if (address.landmark.isNotEmpty()) {
                    Text("Landmark: ${address.landmark}", style = MaterialTheme.typography.labelSmall, color = ManaGoldDim)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = ManaError)
            }
        }
    }
}

@Composable
private fun AddAddressDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var label by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. Home, Office)") })
                OutlinedTextField(value = fullAddress, onValueChange = { fullAddress = it }, label = { Text("Full Address") })
                OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(label, fullAddress, landmark) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
