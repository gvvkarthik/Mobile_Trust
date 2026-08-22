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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrust.data.model.PolicyRule
import com.example.mobiletrust.data.model.TrustPolicyConfig
import com.example.mobiletrust.ui.theme.CyberBorder
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.CyberSurfaceVariant
import com.example.mobiletrust.ui.theme.RiskLowColor
import com.example.mobiletrust.ui.theme.TextMuted
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary

@Composable
fun PolicyConfigCard(
    config: TrustPolicyConfig,
    onRuleToggled: (String, Boolean) -> Unit,
    onMlWeightChanged: (Double) -> Unit,
    onMlWeightCommitted: () -> Unit,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Rule,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONFIGURABLE POLICY ENGINE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(color = CyberSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "${config.rules.count { it.enabled }}/${config.rules.size} ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Scoring blend",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Rules ${((1.0 - config.mlWeight) * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Slider(
                    value = config.mlWeight.toFloat(),
                    onValueChange = { onMlWeightChanged(it.toDouble()) },
                    onValueChangeFinished = onMlWeightCommitted,
                    valueRange = 0f..1f,
                    steps = 9,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = CyberPrimary,
                        activeTrackColor = CyberPrimary,
                        inactiveTrackColor = CyberSurfaceVariant
                    )
                )
                Text(
                    text = "ML ${(config.mlWeight * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Thresholds: LOW >= ${config.thresholds.lowMin}, " +
                    "MEDIUM >= ${config.thresholds.mediumMin}, " +
                    "HIGH >= ${config.thresholds.highMin}, " +
                    "admin alert < ${config.alertThreshold}",
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                config.rules.forEach { rule ->
                    PolicyRuleRow(
                        rule = rule,
                        onToggled = { enabled -> onRuleToggled(rule.id, enabled) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyRuleRow(rule: PolicyRule, onToggled: (Boolean) -> Unit) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.enabled) TextPrimary else TextMuted
                )
                Text(
                    text = rule.description,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
                Text(
                    text = "Enforces ${rule.action.displayName}",
                    fontSize = 9.sp,
                    color = if (rule.enabled) CyberPrimary else TextMuted
                )
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RiskLowColor,
                    checkedTrackColor = RiskLowColor.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = CyberSurface
                )
            )
        }
    }
}
