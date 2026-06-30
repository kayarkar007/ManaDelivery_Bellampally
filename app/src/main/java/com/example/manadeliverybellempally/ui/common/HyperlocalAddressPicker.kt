package com.example.manadeliverybellempally.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*

/**
 * Hyperlocal Address Picker specifically for Bellampally context.
 * Prioritizes landmarks and colony names (SCCL Quarters, Station area).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperlocalAddressPicker(
    onAddressSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val bellampallyLandmarks = listOf(
        "Bazaar Area", "Railway Station", "SCCL Main Hospital", 
        "Gaddamvari Colony", "Babunagar", "Bellampally Mines Area",
        "Police Station", "Bus Stand", "Overbridge", "Kannala Basti"
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredLandmarks = bellampallyLandmarks.filter { it.contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ManaBgPrimary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ManaGold) }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
            Text(
                "SELECT DELIVERY LOCATION", 
                style = MaterialTheme.typography.labelMedium, 
                color = ManaGold, 
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(16.dp))
            
            // Current Location Button
            ManaCard(onClick = { onAddressSelected("Current Location") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.MyLocation, null, tint = ManaGold)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Use My Current Location", fontWeight = FontWeight.Bold)
                        Text("Auto-detect using GPS", style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Colony or Landmark...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ManaGold,
                    unfocusedContainerColor = ManaBgCard,
                    focusedContainerColor = ManaBgCard
                ),
                leadingIcon = { Icon(Icons.Rounded.NearMe, null, tint = ManaGold) }
            )
            
            Spacer(Modifier.height(16.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredLandmarks) { landmark ->
                    ListItem(
                        headlineContent = { Text(landmark, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Bellampally, Telangana", style = MaterialTheme.typography.labelSmall) },
                        leadingContent = { Icon(Icons.Rounded.LocationOn, null, tint = ManaGold) },
                        modifier = Modifier.background(ManaBgCard, RoundedCornerShape(12.dp)),
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
