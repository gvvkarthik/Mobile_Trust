package com.example.mobiletrust.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.mobiletrust.data.model.SecurityAction
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.CyberSurfaceVariant
import com.example.mobiletrust.ui.theme.RiskCriticalColor
import com.example.mobiletrust.ui.theme.RiskHighColor
import com.example.mobiletrust.ui.theme.RiskMediumColor
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary
import com.example.mobiletrust.data.model.TrustAlert

@Composable
fun SecurityAlertDialog(
    alert: TrustAlert,
    onDismiss: () -> Unit,
    onReauthenticate: () -> Unit,
    onRecoverSession: () -> Unit
) {
    val (headerColor, icon) = when (alert.action) {
        SecurityAction.ALLOW_ACCESS -> Pair(CyberPrimary, Icons.Default.Security)
        SecurityAction.SHOW_SECURITY_WARNING -> Pair(RiskMediumColor, Icons.Default.WarningAmber)
        SecurityAction.REQUIRE_REAUTHENTICATION -> Pair(RiskHighColor, Icons.Default.Security)
        SecurityAction.TERMINATE_SESSION -> Pair(RiskCriticalColor, Icons.Default.Dangerous)
    }

    AlertDialog(
        onDismissRequest = {
            if (!alert.isBlocking) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !alert.isBlocking,
            dismissOnClickOutside = !alert.isBlocking
        ),
        containerColor = CyberSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.5.dp, headerColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(headerColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = headerColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = headerColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Policy Enforcement: ${alert.action.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            when {
                alert.requiresReauthentication -> {
                    Button(
                        onClick = onReauthenticate,
                        colors = ButtonDefaults.buttonColors(containerColor = RiskHighColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-Authenticate Now", fontWeight = FontWeight.Bold)
                    }
                }
                alert.isBlocking -> {
                    Button(
                        onClick = onRecoverSession,
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify Identity & Restore Session", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Acknowledge", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (!alert.isBlocking) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = TextSecondary)
                }
            }
        }
    )
}
