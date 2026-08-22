package com.example.mobiletrust.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrust.data.model.BehaviourStatus
import com.example.mobiletrust.data.model.DeviceSecurityStatus
import com.example.mobiletrust.data.model.NetworkType
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustResult
import com.example.mobiletrust.data.model.UserRole
import com.example.mobiletrust.ui.theme.CyberBorder
import com.example.mobiletrust.ui.theme.CyberSecondary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.RiskCriticalColor
import com.example.mobiletrust.ui.theme.RiskHighColor
import com.example.mobiletrust.ui.theme.RiskLowColor
import com.example.mobiletrust.ui.theme.RiskMediumColor
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary

@Composable
fun InformationCards(
    result: TrustResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            val netIcon = when (result.input.networkType) {
                NetworkType.SECURE_WIFI -> Icons.Default.Wifi
                NetworkType.MOBILE_4G -> Icons.Default.CellTower
                NetworkType.PUBLIC_WIFI -> Icons.Default.WifiOff
            }
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "CURRENT NETWORK",
                value = result.input.networkType.displayName,
                icon = netIcon,
                accentColor = CyberSecondary
            )

            val (riskColor, riskIcon) = when (result.riskLevel) {
                RiskLevel.LOW -> Pair(RiskLowColor, Icons.Default.Shield)
                RiskLevel.MEDIUM -> Pair(RiskMediumColor, Icons.Default.Security)
                RiskLevel.HIGH -> Pair(RiskHighColor, Icons.Default.Dangerous)
                RiskLevel.CRITICAL -> Pair(RiskCriticalColor, Icons.Default.Dangerous)
            }
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "RISK LEVEL",
                value = result.riskLevel.displayName,
                icon = riskIcon,
                accentColor = riskColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            val (sessionColor, sessionIcon) = when (result.sessionStatus) {
                SessionStatus.ACTIVE -> Pair(RiskLowColor, Icons.Default.LockPerson)
                SessionStatus.WARNING -> Pair(RiskMediumColor, Icons.Default.Info)
                SessionStatus.REAUTH_REQUIRED -> Pair(RiskHighColor, Icons.Default.VpnKey)
                SessionStatus.TERMINATED -> Pair(RiskCriticalColor, Icons.Default.Dangerous)
            }
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "SESSION STATUS",
                value = result.sessionStatus.displayName,
                icon = sessionIcon,
                accentColor = sessionColor
            )

            val devColor = if (result.input.deviceSecurity == DeviceSecurityStatus.SECURE) RiskLowColor else RiskCriticalColor
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "DEVICE SECURITY",
                value = result.input.deviceSecurity.displayName.uppercase(),
                icon = Icons.Default.PermDeviceInformation,
                accentColor = devColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            val loginColor = if (result.input.failedLoginAttempts == 0) RiskLowColor else if (result.input.failedLoginAttempts <= 1) RiskMediumColor else RiskCriticalColor
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "FAILED LOGINS",
                value = "${result.input.failedLoginAttempts} attempts",
                icon = Icons.Default.VpnKey,
                accentColor = loginColor
            )

            val behColor = if (result.input.behaviour == BehaviourStatus.NORMAL) RiskLowColor else RiskHighColor
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "BEHAVIOUR",
                value = result.input.behaviour.displayName.uppercase(),
                icon = Icons.Default.Psychology,
                accentColor = behColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val roleApproved = result.input.userRole in UserRole.PUBLIC_NETWORK_APPROVED
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "ACTIVE ROLE",
                value = result.input.userRole.displayName.uppercase(),
                icon = Icons.Default.Badge,
                accentColor = if (roleApproved) CyberSecondary else RiskMediumColor
            )

            val transitionColor = when {
                result.input.networkTransitions == 0 -> RiskLowColor
                result.input.networkTransitions < 4 -> RiskMediumColor
                else -> RiskHighColor
            }
            InfoStatusTile(
                modifier = Modifier.weight(1f),
                title = "TRANSITIONS",
                value = "${result.input.networkTransitions} this session",
                icon = Icons.Default.SwapHoriz,
                accentColor = transitionColor
            )
        }
    }
}

@Composable
fun InfoStatusTile(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, CyberBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}
