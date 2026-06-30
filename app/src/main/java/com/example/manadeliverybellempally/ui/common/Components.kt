package com.example.manadeliverybellempally.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*

@Composable
fun ManaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = ManaGold
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(16.dp)),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = if (containerColor == ManaGold) Color.Black else Color.White,
            disabledContainerColor = ManaTextTertiary.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ManaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, ManaGold),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ManaGold)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = BorderStroke(1.dp, ManaBorder.copy(alpha = 0.5f)),
    containerColor: Color = ManaBgCard,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ManaCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                Text(value, style = MaterialTheme.typography.titleLarge, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
            }
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("See All", color = ManaGold, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(16.dp), tint = ManaGold)
                }
            }
        }
    }
}

@Composable
fun VendorCard(
    storeName: String,
    categoryName: String,
    rating: Float,
    deliveryTime: Int,
    isOpen: Boolean,
    isBusy: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ManaCard(
        modifier = modifier.width(280.dp),
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(ManaRedStrong, ManaRed.copy(alpha = 0.8f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Storefront, null, modifier = Modifier.size(80.dp).alpha(0.1f), tint = ManaGold)
                Text(storeName.take(1).uppercase(), style = MaterialTheme.typography.displayLarge, color = ManaGold.copy(alpha = 0.15f), fontWeight = FontWeight.Black)
                
                if (isBusy && isOpen) {
                    Surface(color = ManaWarning.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                        Text("BUSY", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                if (!isOpen) {
                    Surface(color = Color.Black.copy(alpha = 0.7f), modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("CLOSED", color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(storeName, style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Surface(color = ManaSuccess.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Icon(Icons.Rounded.Star, null, tint = ManaGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(rating.toString(), style = MaterialTheme.typography.labelSmall, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(categoryName, style = MaterialTheme.typography.bodySmall, color = ManaGold.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccessTime, null, tint = ManaTextTertiary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${deliveryTime} min", style = MaterialTheme.typography.labelMedium, color = ManaTextSecondary)
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ManaTextTertiary))
                    Spacer(Modifier.width(12.dp))
                    Text("Free Delivery", style = MaterialTheme.typography.labelMedium, color = ManaSuccess)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    name: String,
    price: Double,
    discountPrice: Double,
    unit: String,
    isVeg: Boolean,
    rating: Float,
    quantity: Int = 0,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ManaCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(ManaBgSecondary), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Fastfood, null, tint = ManaGold.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
                Icon(imageVector = Icons.Rounded.FiberManualRecord, contentDescription = null, tint = if (isVeg) ManaSuccess else ManaRed, modifier = Modifier.size(16.dp).align(Alignment.TopStart).padding(4.dp).border(1.dp, Color.White, CircleShape))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = ManaTextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Local Special", style = MaterialTheme.typography.labelSmall, color = ManaTextTertiary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val finalPrice = if (discountPrice > 0) discountPrice else price
                    Text("₹${finalPrice.toInt()}", style = MaterialTheme.typography.titleMedium, color = ManaGold, fontWeight = FontWeight.Black)
                    if (discountPrice > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text("₹${price.toInt()}", style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = ManaTextTertiary)
                    }
                    Text(" / $unit", style = MaterialTheme.typography.bodySmall, color = ManaTextTertiary)
                }
            }
            if (quantity == 0) {
                Button(onClick = onAdd, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ManaRedStrong, contentColor = Color.White), modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    Text("ADD", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(ManaRedStrong, RoundedCornerShape(12.dp)).padding(horizontal = 2.dp)) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    Text(quantity.toString(), color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "PLACED" -> ManaWarning
        "CONFIRMED", "ACCEPTED" -> ManaInfo
        "PREPARING" -> ManaGold
        "READY", "PICKED_UP" -> ManaSuccess
        "DELIVERED" -> ManaSuccess
        "CANCELLED", "REJECTED" -> ManaRed
        else -> ManaTextTertiary
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.4f)), modifier = modifier) {
        Text(text = status.replace("_", " "), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(100.dp), tint = ManaGold.copy(alpha = 0.1f))
        Spacer(Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = ManaTextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = ManaTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1200, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "translate"
    )
    val brush = Brush.linearGradient(colors = listOf(ManaBgSecondary, ManaBgCard, ManaBgSecondary), start = Offset.Zero, end = Offset(x = translateAnim, y = translateAnim))
    Box(modifier = modifier.clip(shape).background(brush))
}
