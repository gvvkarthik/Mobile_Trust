package com.example.mobiletrust.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrust.data.model.RiskLevel
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.data.model.TrustResult
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrustScoreCard(
    result: TrustResult,
    modifier: Modifier = Modifier
) {
    val riskColor by animateColorAsState(
        targetValue = when (result.riskLevel) {
            RiskLevel.LOW -> RiskLowColor
            RiskLevel.MEDIUM -> RiskMediumColor
            RiskLevel.HIGH -> RiskHighColor
            RiskLevel.CRITICAL -> RiskCriticalColor
        },
        animationSpec = tween(500),
        label = "riskColor"
    )

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Shield",
                        tint = CyberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRUST SCORE EVALUATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary,
                        letterSpacing = 1.sp
                    )
                }

                SessionStatusBadge(status = result.sessionStatus)
            }

            Spacer(modifier = Modifier.height(20.dp))

            CircularTrustScoreMeter(
                score = result.trustScore,
                color = riskColor,
                size = 180.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            RiskLevelBadge(riskLevel = result.riskLevel)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Policy: ${result.securityAction.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = riskColor
            )

            if (result.matchedRules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enforced by: ${result.matchedRules.joinToString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DeductionChip(label = "Network", penalty = result.penalties.network)
                Spacer(modifier = Modifier.width(4.dp))
                DeductionChip(label = "Device", penalty = result.penalties.device)
                Spacer(modifier = Modifier.width(4.dp))
                DeductionChip(label = "Logins", penalty = result.penalties.failedLogins)
                Spacer(modifier = Modifier.width(4.dp))
                DeductionChip(label = "Behaviour", penalty = result.penalties.behaviour)
                Spacer(modifier = Modifier.width(4.dp))
                DeductionChip(label = "Transitions", penalty = result.penalties.transitions)
            }
        }
    }
}

@Composable
fun CircularTrustScoreMeter(
    score: Int,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "scoreProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()

            drawCircle(
                color = Color(0xFF21262D),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Text(
                text = "/ 100",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RiskLevelBadge(riskLevel: RiskLevel) {
    val (bgColor, textColor) = when (riskLevel) {
        RiskLevel.LOW -> Pair(Color(0xFF13361E), RiskLowColor)
        RiskLevel.MEDIUM -> Pair(Color(0xFF3B2E05), RiskMediumColor)
        RiskLevel.HIGH -> Pair(Color(0xFF3D1D09), RiskHighColor)
        RiskLevel.CRITICAL -> Pair(Color(0xFF490202), RiskCriticalColor)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RISK: ${riskLevel.displayName}",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SessionStatusBadge(status: SessionStatus) {
    val (bgColor, textColor) = when (status) {
        SessionStatus.ACTIVE -> Pair(Color(0xFF13361E), RiskLowColor)
        SessionStatus.WARNING -> Pair(Color(0xFF3B2E05), RiskMediumColor)
        SessionStatus.REAUTH_REQUIRED -> Pair(Color(0xFF3D1D09), RiskHighColor)
        SessionStatus.TERMINATED -> Pair(Color(0xFF490202), RiskCriticalColor)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DeductionChip(label: String, penalty: Int) {
    Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = if (penalty > 0) "-$penalty" else "0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (penalty > 0) RiskHighColor else RiskLowColor
            )
        }
    }
}
