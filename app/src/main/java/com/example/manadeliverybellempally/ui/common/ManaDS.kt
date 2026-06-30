package com.example.manadeliverybellempally.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manadeliverybellempally.theme.*

/**
 * ManaDS - The Official Design System for Mana Delivery Bellempally.
 * Optimized for High-End Visuals, Accessibility, and One-Handed Operation.
 */

@Composable
fun ManaHeader(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackButton && onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(ManaBgCard, RoundedCornerShape(12.dp)).border(1.dp, ManaBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = ManaGold)
                }
                Spacer(Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = ManaGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = ManaTextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

@Composable
fun ManaActionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color = ManaGold,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = ManaBgCard,
        border = BorderStroke(1.dp, ManaBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ManaTextPrimary)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.labelSmall, color = ManaTextSecondary)
                }
            }
        }
    }
}

@Composable
fun ManaGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(18.dp),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(ManaRedStrong, Color(0xFF9E1B2D))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun ManaInfoBadge(
    text: String,
    containerColor: Color = ManaGold.copy(alpha = 0.1f),
    contentColor: Color = ManaGold
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Black
        )
    }
}
