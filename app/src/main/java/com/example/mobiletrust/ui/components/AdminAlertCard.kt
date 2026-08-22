package com.example.mobiletrust.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.TrustAlert
import com.example.mobiletrust.ui.theme.CyberBorder
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.CyberSurfaceVariant
import com.example.mobiletrust.ui.theme.RiskCriticalColor
import com.example.mobiletrust.ui.theme.RiskHighColor
import com.example.mobiletrust.ui.theme.RiskLowColor
import com.example.mobiletrust.ui.theme.RiskMediumColor
import com.example.mobiletrust.ui.theme.TextMuted
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary

@Composable
fun AdminAlertCard(
    alerts: List<TrustAlert>,
    onAcknowledgeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val openCount = alerts.count { !it.acknowledged }

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (openCount > 0) RiskHighColor else CyberPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADMIN ALERT CONSOLE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (openCount > 0) {
                    TextButton(onClick = onAcknowledgeAll) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$openCount OPEN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.heightIn(min = 8.dp))

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trust degradation alerts raised",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alerts, key = { it.id }) { alert -> AdminAlertRow(alert) }
                }
            }
        }
    }
}

@Composable
private fun AdminAlertRow(alert: TrustAlert) {
    val accent = when (alert.riskLevel) {
        RiskLevel.LOW -> RiskLowColor
        RiskLevel.MEDIUM -> RiskMediumColor
        RiskLevel.HIGH -> RiskHighColor
        RiskLevel.CRITICAL -> RiskCriticalColor
    }

    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, accent.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alert.acknowledged) TextMuted else TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.riskLevel.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = alert.timestamp,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.heightIn(min = 3.dp))
            Text(
                text = alert.message,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
            if (alert.acknowledged) {
                Text(
                    text = "acknowledged",
                    fontSize = 9.sp,
                    color = RiskLowColor
                )
            }
        }
    }
}
