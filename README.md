# MobileTrust 🛡️
> Continuous Mobile Device Trust & Network Transition Monitoring System

MobileTrust is a cybersecurity-focused Android application designed for zero-trust continuous device and network authentication. It continuously evaluates the trustworthiness of a mobile device as it transitions across network environments and experiences anomalous security events, enforcing automated security policies in real-time.

---

## 🚀 Key Features

- **Dynamic Trust Prediction Engine**: Deterministic baseline (100) scoring model factoring network trust, device integrity, brute-force login telemetry, and user behavioral analytics.
- **Continuous Network Transition Simulation**: Instant re-evaluation when transitioning between:
  - **Secure Wi-Fi** (-5 penalty)
  - **Mobile 4G** (-15 penalty)
  - **Public Wi-Fi** (-35 penalty)
- **Granular Risk Brackets**:
  - `LOW` (71 – 100)
  - `MEDIUM` (41 – 70)
  - `HIGH` (21 – 40)
  - `CRITICAL` (0 – 20)
- **Automated Security Policy Engine**:
  - **LOW**: `ALLOW ACCESS` (Session Active)
  - **MEDIUM**: `SHOW SECURITY WARNING` (Session Warning)
  - **HIGH**: `REQUIRE RE-AUTHENTICATION` (Re-auth Required)
  - **CRITICAL**: `TERMINATE SESSION` (Session Terminated)
- **Reverse-Chronological Audit Logger**: Real-time in-memory audit log tracking network switches, score changes, risk shifts, and policy triggers.
- **Automated Hackathon Demo Mode**: 3-step automated live demonstration simulating network transitions and security anomalies with live progress indicators.
- **100% Offline & Private**: Zero external cloud services, zero Firebase dependencies, entirely local on-device processing.

---

## 🛠️ Architecture & Tech Stack

- **Platform**: Android (Min SDK 24, Target SDK 37)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose & Material 3 (Cyber Dark Aesthetic)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture separation
- **State Management**: Kotlin StateFlow + Kotlin Coroutines

```
com.example.mobiletrust
├── data
│   └── model
│       ├── NetworkType.kt
│       ├── TrustInput.kt
│       ├── TrustResult.kt
│       ├── RiskLevel.kt
│       ├── SessionStatus.kt
│       ├── DeviceSecurityStatus.kt
│       ├── BehaviourStatus.kt
│       ├── SecurityAction.kt
│       └── AuditLogEntry.kt
├── domain
│   └── predictor
│       ├── TrustPredictor.kt
│       └── RuleBasedTrustPredictor.kt
├── security
│   ├── SecurityPolicyEngine.kt
│   └── AuditLogger.kt
├── ui
│   ├── components
│   │   ├── TrustScoreCard.kt
│   │   ├── InformationCards.kt
│   │   ├── NetworkSelector.kt
│   │   ├── SecurityControls.kt
│   │   ├── DemoControls.kt
│   │   ├── AuditLogCard.kt
│   │   └── SecurityAlertDialog.kt
│   ├── screens
│   │   └── DashboardScreen.kt
│   ├── theme
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel
│       └── MobileTrustViewModel.kt
└── MainActivity.kt
```

---

## 🧪 Testing & Verification

Unit tests cover scoring formulas, clamping, policy mapping, and ViewModel state mutations:
```bash
./gradlew testDebugUnitTest
```

Build the debug APK:
```bash
./gradlew assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`
