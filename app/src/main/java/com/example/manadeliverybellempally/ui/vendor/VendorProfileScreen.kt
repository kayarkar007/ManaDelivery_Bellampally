package com.example.manadeliverybellempally.ui.vendor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.manadeliverybellempally.data.model.Vendor
import com.example.manadeliverybellempally.theme.*
import com.example.manadeliverybellempally.ui.common.ManaButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProfileScreen(
    viewModel: VendorViewModel,
    onBack: () -> Unit
) {
    val vendor by viewModel.vendor.collectAsState()
    var editedVendor by remember(vendor) { mutableStateOf(vendor ?: Vendor()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store Profile") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(ManaBgCard).border(1.dp, ManaBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = ManaGoldDim)
                    }
                    SmallFloatingActionButton(onClick = { /* Pick Image */ }, containerColor = ManaGold, shape = CircleShape) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = editedVendor.storeName,
                    onValueChange = { editedVendor = editedVendor.copy(storeName = it) },
                    label = { Text("Store Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = editedVendor.storeAddress,
                    onValueChange = { editedVendor = editedVendor.copy(storeAddress = it) },
                    label = { Text("Store Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = editedVendor.phone,
                    onValueChange = { editedVendor = editedVendor.copy(phone = it) },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                ManaButton(
                    text = "Save Changes",
                    onClick = { viewModel.updateVendorProfile(editedVendor) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
