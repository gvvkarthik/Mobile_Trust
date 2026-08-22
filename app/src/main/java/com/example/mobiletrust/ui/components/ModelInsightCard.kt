package com.example.mobiletrust.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrust.data.model.ModelMetrics
import com.example.mobiletrust.data.model.TrustResult
import com.example.mobiletrust.domain.ml.TrustFeatures
import com.example.mobiletrust.ui.theme.CyberBorder
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSecondary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.CyberSurfaceVariant
import com.example.mobiletrust.ui.theme.RiskCriticalColor
import com.example.mobiletrust.ui.theme.RiskHighColor
import com.example.mobiletrust.ui.theme.RiskLowColor
import com.example.mobiletrust.ui.theme.RiskMediumColor
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ModelInsightCard(
    result: TrustResult,
    metrics: ModelMetrics,
    mlWeight: Double,
    modifier: Modifier = Modifier
) {
    val probability = result.degradationProbability
    val animatedProbability by animateFloatAsState(
        targetValue = probability.toFloat(),
        animationSpec = tween(500),
        label = "degradationProbability"
    )
    val probabilityColor = when {
        probability < 0.3 -> RiskLowColor
        probability < 0.6 -> RiskMediumColor
        probability < 0.8 -> RiskHighColor
        else -> RiskCriticalColor
    }

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
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ON-DEVICE ML TRUST MODEL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(color = CyberSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = RiskLowColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.3f ms", result.inferenceMillis),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RiskLowColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Predicted trust degradation risk",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(probability * 100).toInt()}%",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = probabilityColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = { animatedProbability },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = probabilityColor,
                    trackColor = CyberSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreSourceTile(
                    modifier = Modifier.weight(1f),
                    label = "RULE SCORE",
                    value = result.ruleScore.toString(),
                    weight = "${((1.0 - mlWeight) * 100).toInt()}%",
                    accent = CyberSecondary
                )
                ScoreSourceTile(
                    modifier = Modifier.weight(1f),
                    label = "ML SCORE",
                    value = result.mlScore.toString(),
                    weight = "${(mlWeight * 100).toInt()}%",
                    accent = CyberPrimary
                )
                ScoreSourceTile(
                    modifier = Modifier.weight(1f),
                    label = "BLENDED",
                    value = result.trustScore.toString(),
                    weight = "final",
                    accent = RiskLowColor
                )
            }

            if (metrics.sampleCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Validation on ${metrics.sampleCount} synthetic samples",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricLabel("Accuracy", metrics.accuracy, RiskLowColor)
                            MetricLabel("Precision", metrics.precision, CyberPrimary)
                            MetricLabel("Recall", metrics.recall, CyberSecondary)
                            MetricLabel("F1", metrics.f1Score, RiskMediumColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Features: ${TrustFeatures.NAMES.joinToString()}",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ScoreSourceTile(
    label: String,
    value: String,
    weight: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = TextSecondary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = accent
            )
            Text(text = weight, fontSize = 9.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun MetricLabel(name: String, value: Double, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format(Locale.US, "%.1f%%", value * 100),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = accent
        )
        Text(text = name, fontSize = 9.sp, color = TextSecondary)
    }
}
