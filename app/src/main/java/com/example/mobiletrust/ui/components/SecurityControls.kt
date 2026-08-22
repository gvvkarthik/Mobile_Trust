package com.example.mobiletrust.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.ui.theme.CyberBorder
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.CyberSurfaceVariant
import com.example.mobiletrust.ui.theme.RiskCriticalColor
import com.example.mobiletrust.ui.theme.RiskHighColor
import com.example.mobiletrust.ui.theme.RiskLowColor
import com.example.mobiletrust.ui.theme.RiskMediumColor
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary

@Composable
fun SecurityControls(
    deviceSecurity: DeviceSecurityStatus,
    onDeviceSecurityChanged: (DeviceSecurityStatus) -> Unit,
    failedLoginAttempts: Int,
    onFailedLoginAttemptsChanged: (Int) -> Unit,
    behaviour: BehaviourStatus,
    onBehaviourChanged: (BehaviourStatus) -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEVICE & CONTEXT SECURITY CONTROLS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // 1. Device Security (Secure / Compromised)
            Column {
                Text(
                    text = "Device Integrity State",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton(
                        modifier = Modifier.weight(1f),
                        text = "Secure",
                        subtext = "0 penalty",
                        icon = Icons.Default.VerifiedUser,
                        isSelected = deviceSecurity == DeviceSecurityStatus.SECURE,
                        activeColor = RiskLowColor,
                        onClick = { onDeviceSecurityChanged(DeviceSecurityStatus.SECURE) }
                    )
                    SegmentButton(
                        modifier = Modifier.weight(1f),
                        text = "Compromised",
                        subtext = "-25 penalty",
                        icon = Icons.Default.BugReport,
                        isSelected = deviceSecurity == DeviceSecurityStatus.COMPROMISED,
                        activeColor = RiskCriticalColor,
                        onClick = { onDeviceSecurityChanged(DeviceSecurityStatus.COMPROMISED) }
                    )
                }
            }

            // 2. Failed Login Attempts (0, 1, 3, 5)
            Column {
                Text(
                    text = "Failed Login Attempts (-5 per attempt)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 1, 3, 5).forEach { count ->
                        val isSelected = failedLoginAttempts == count
                        val penalty = count * 5
                        val chipColor = when (count) {
                            0 -> RiskLowColor
                            1 -> RiskMediumColor
                            3 -> RiskHighColor
                            else -> RiskCriticalColor
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onFailedLoginAttemptsChanged(count) }
                                .border(
                                    if (isSelected) 1.5.dp else 0.8.dp,
                                    if (isSelected) chipColor else CyberBorder,
                                    RoundedCornerShape(8.dp)
                                ),
                            color = if (isSelected) chipColor.copy(alpha = 0.2f) else CyberSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$count",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (penalty > 0) "-$penalty" else "0",
                                    fontSize = 10.sp,
                                    color = if (isSelected) chipColor else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Behaviour (Normal / Suspicious)
            Column {
                Text(
                    text = "User & Telemetry Behaviour",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton(
                        modifier = Modifier.weight(1f),
                        text = "Normal",
                        subtext = "0 penalty",
                        icon = Icons.Default.Psychology,
                        isSelected = behaviour == BehaviourStatus.NORMAL,
                        activeColor = RiskLowColor,
                        onClick = { onBehaviourChanged(BehaviourStatus.NORMAL) }
                    )
                    SegmentButton(
                        modifier = Modifier.weight(1f),
                        text = "Suspicious",
                        subtext = "-20 penalty",
                        icon = Icons.Default.WarningAmber,
                        isSelected = behaviour == BehaviourStatus.SUSPICIOUS,
                        activeColor = RiskHighColor,
                        onClick = { onBehaviourChanged(BehaviourStatus.SUSPICIOUS) }
                    )
                }
            }
        }
    }
}

@Composable
fun SegmentButton(
    text: String,
    subtext: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) activeColor else CyberBorder,
                RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) activeColor.copy(alpha = 0.15f) else CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = text,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = subtext,
                    fontSize = 10.sp,
                    color = if (isSelected) activeColor else TextSecondary
                )
            }
        }
    }
}
