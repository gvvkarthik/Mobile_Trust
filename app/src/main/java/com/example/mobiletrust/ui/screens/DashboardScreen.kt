package com.example.mobiletrust.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiletrust.data.model.SessionStatus
import com.example.mobiletrust.ui.components.AdminAlertCard
import com.example.mobiletrust.ui.components.AuditLogCard
import com.example.mobiletrust.ui.components.DemoControls
import com.example.mobiletrust.ui.components.FederatedLearningCard
import com.example.mobiletrust.ui.components.InformationCards
import com.example.mobiletrust.ui.components.ModelInsightCard
import com.example.mobiletrust.ui.components.NetworkSelector
import com.example.mobiletrust.ui.components.PolicyConfigCard
import com.example.mobiletrust.ui.components.SecurityAlertDialog
import com.example.mobiletrust.ui.components.SecurityControls
import com.example.mobiletrust.ui.components.SessionLockBanner
import com.example.mobiletrust.ui.components.TrustScoreCard
import com.example.mobiletrust.ui.theme.CyberBackground
import com.example.mobiletrust.ui.theme.CyberPrimary
import com.example.mobiletrust.ui.theme.CyberSurface
import com.example.mobiletrust.ui.theme.TextPrimary
import com.example.mobiletrust.ui.theme.TextSecondary
import com.example.mobiletrust.ui.viewmodel.MobileTrustViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MobileTrustViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CyberPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield Icon",
                                tint = CyberPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MobileTrust",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Continuous Mobile Trust Monitoring",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetState() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset State",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.result.sessionStatus == SessionStatus.TERMINATED) {
                SessionLockBanner(onRecoverSession = { viewModel.recoverTerminatedSession() })
            }

            TrustScoreCard(result = uiState.result)

            InformationCards(result = uiState.result)

            ModelInsightCard(
                result = uiState.result,
                metrics = uiState.modelMetrics,
                mlWeight = uiState.config.mlWeight
            )

            NetworkSelector(
                selectedNetwork = uiState.input.networkType,
                onNetworkSelected = { viewModel.onNetworkSelected(it) }
            )

            SecurityControls(
                deviceSecurity = uiState.input.deviceSecurity,
                onDeviceSecurityChanged = { viewModel.onDeviceSecurityChanged(it) },
                failedLoginAttempts = uiState.input.failedLoginAttempts,
                onFailedLoginAttemptsChanged = { viewModel.onFailedLoginAttemptsChanged(it) },
                behaviour = uiState.input.behaviour,
                onBehaviourChanged = { viewModel.onBehaviourChanged(it) },
                userRole = uiState.input.userRole,
                onUserRoleChanged = { viewModel.onUserRoleChanged(it) }
            )

            PolicyConfigCard(
                config = uiState.config,
                onRuleToggled = { id, enabled -> viewModel.onRuleToggled(id, enabled) },
                onMlWeightChanged = { viewModel.onMlWeightChanged(it) },
                onMlWeightCommitted = { viewModel.onMlWeightCommitted() }
            )

            FederatedLearningCard(
                report = uiState.federatedReport,
                isRunning = uiState.isFederatedRunning,
                onRun = { viewModel.runFederatedRound() }
            )

            DemoControls(
                isDemoRunning = uiState.isDemoRunning,
                demoCurrentStep = uiState.demoCurrentStep,
                onRunDemo = { viewModel.runDemoScenario() },
                onReset = { viewModel.resetState() }
            )

            AdminAlertCard(
                alerts = uiState.adminAlerts,
                onAcknowledgeAll = { viewModel.acknowledgeAdminAlerts() }
            )

            AuditLogCard(logs = uiState.logs)

            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.activeAlert?.let { alert ->
            SecurityAlertDialog(
                alert = alert,
                onDismiss = { viewModel.dismissAlert() },
                onReauthenticate = { viewModel.reauthenticateUser() },
                onRecoverSession = { viewModel.recoverTerminatedSession() }
            )
        }
    }
}
