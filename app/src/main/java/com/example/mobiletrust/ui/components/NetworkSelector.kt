package com.example.mobiletrust.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.ui.theme.CyberBorder
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.CyberSurfaceVariant
import com.example.mobiletrust.ui.theme.RiskCriticalColor
import com.example.mobiletrust.ui.theme.RiskHighColor
import com.example.mobiletrust.ui.theme.RiskLowColor
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary

@Composable
fun NetworkSelector(
    selectedNetwork: NetworkType,
    onNetworkSelected: (NetworkType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lan,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NETWORK ENVIRONMENT SIMULATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NetworkOptionTile(
                    network = NetworkType.SECURE_WIFI,
                    icon = Icons.Default.Wifi,
                    isSelected = selectedNetwork == NetworkType.SECURE_WIFI,
                    penaltyText = "-5 Penalty (Trusted)",
                    penaltyColor = RiskLowColor,
                    onClick = { onNetworkSelected(NetworkType.SECURE_WIFI) }
                )

                NetworkOptionTile(
                    network = NetworkType.MOBILE_4G,
                    icon = Icons.Default.CellTower,
                    isSelected = selectedNetwork == NetworkType.MOBILE_4G,
                    penaltyText = "-15 Penalty (Cellular)",
                    penaltyColor = RiskHighColor,
                    onClick = { onNetworkSelected(NetworkType.MOBILE_4G) }
                )

                NetworkOptionTile(
                    network = NetworkType.PUBLIC_WIFI,
                    icon = Icons.Default.WifiOff,
                    isSelected = selectedNetwork == NetworkType.PUBLIC_WIFI,
                    penaltyText = "-35 Penalty (Untrusted)",
                    penaltyColor = RiskCriticalColor,
                    onClick = { onNetworkSelected(NetworkType.PUBLIC_WIFI) }
                )
            }
        }
    }
}

@Composable
fun NetworkOptionTile(
    network: NetworkType,
    icon: ImageVector,
    isSelected: Boolean,
    penaltyText: String,
    penaltyColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) CyberPrimary else CyberBorder
    val bgColor = if (isSelected) Color(0xFF00363A).copy(alpha = 0.5f) else CyberSurfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp)),
        color = bgColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) CyberPrimary else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = network.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = penaltyText,
                        fontSize = 11.sp,
                        color = penaltyColor
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = CyberPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
