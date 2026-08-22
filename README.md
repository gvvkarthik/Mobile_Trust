# MobileTrust

An Android app that watches how trustworthy your device and network are, and cuts your session off when they stop being trustworthy.

The idea comes from field work — military operations, disaster response — where someone walks out of a secure building and their phone silently falls back to a public hotspot. Their session keeps running as if nothing happened. That gap between "the network changed" and "the system noticed" is the window an attacker wants. MobileTrust closes it by re-scoring trust on every context change and acting on the result immediately.

Everything runs on the phone. No server, no login, no internet permission.

## What it does

You get a trust score from 0 to 100. It moves when your network changes, when your device looks compromised, when logins fail, when behaviour looks off, or when you hop between networks too often.

Two things produce that score and they get averaged:

- **A rule score** — plain arithmetic. Start at 100, subtract penalties. Easy to explain to anyone, which matters when you're justifying why someone got locked out.
- **An ML score** — a logistic regression model, trained offline, running on-device in well under a millisecond. It gives a probability that trust is degrading, and that probability becomes a score.

You can slide the balance between them anywhere from all-rules to all-ML while the app runs and watch the number change.

Below a threshold, the app stops warning and starts acting: show a warning, force re-authentication, or terminate the session. On top of the score thresholds there's a rule engine you can reconfigure live — toggle "block public networks for unapproved roles" off and watch the same context produce a different verdict.

Everything that happens lands in an audit log, timestamped, newest first, with the reason attached. If the session gets terminated you'll see exactly which rule did it.

## The model

I didn't download a dataset. There isn't a public corpus of "mobile device trust telemetry", and the brief required synthetic data anyway. So the app generates its own.

`SyntheticDataset` runs a seeded linear congruential generator to produce device contexts — network type, device integrity, failed logins, behaviour flag, transition count. Each context is scored by a hidden formula that includes an interaction term (a compromised device on public Wi-Fi is worse than the sum of its parts) plus Gaussian noise. If the hidden score lands below 55, the sample is labelled "degraded". The model never sees that formula; it only sees five normalised features and a label, and has to work backwards.

Training is full-batch gradient descent — 2,400 samples, 900 epochs. I ran it offline and baked the resulting six numbers into `PretrainedTrustModel`, so the app starts instantly instead of training on launch. The same trainer still ships in the app, which is what makes the federated demo real rather than theatre.

On 800 held-out samples it gets **94.6% accuracy** (precision 94.9%, recall 96.3%). The brief asked for 70%. A model that always guessed the majority class would get 60.8%, so it is genuinely learning the interaction, not just the base rate.

Because the generator is seeded, all of this is reproducible. There's a test that retrains from scratch and fails if the result drifts more than two points from the baked weights.

## Federated learning

Hit the button and four simulated clients each train on their own private shard. Only the weights come back; the samples never leave. Those weights get averaged (FedAvg), the global model is evaluated, and — this part matters — it gets promoted into the live engine. Your trust score is then being computed by a model that was assembled from four devices that never shared data with each other.

## Security posture

Some deliberate choices worth knowing about:

Terminating a session actually freezes it. You cannot switch to a friendlier network, mark your device clean, or disable the rule that locked you out — those attempts are rejected and written to the audit log. The only way forward is explicit re-verification.

Reset does not erase history. An earlier version wiped the audit log on reset, which meant anyone could destroy the evidence trail with one tap. Now reset restores the trust context but the log and the admin alerts survive.

The app declares no permissions at all. No internet, no network state, nothing. Backup is off. Release builds run through R8 with shrinking and obfuscation — that also takes the APK from 19 MB down to about 1.1 MB.

What it does *not* do: verify real credentials. The brief ruled out authentication frameworks, so "re-authenticate" is a modelled step, not a biometric prompt. The audit log lives in memory and dies with the process, because persistent storage was also ruled out. And the random number generator behind the synthetic data is a plain LCG — fine for generating fake telemetry, completely unsuitable for anything cryptographic. Don't reuse it.

## Layout

```
data/model      the vocabulary — inputs, results, risk levels, policy rules, alerts
domain/ml       dataset generation, the model, training, evaluation, federated averaging
domain/predictor  rule scoring, ML scoring, and the hybrid that blends them
domain/engine   TrustEngine — the one entry point that scores and decides
security        the rule engine, the alert dispatcher, the audit logger
ui              one dashboard screen, twelve components, the ViewModel
```

`TrustEngine.evaluate(input, config)` is the whole thing in one call. It returns the blended score, both source scores, the degradation probability, which rules fired, the penalty breakdown, and how long inference took.

Kotlin, Jetpack Compose, Material 3, MVVM, StateFlow. Nothing outside AndroidX.

## Running it

```bash
./gradlew testDebugUnitTest
```

42 tests. They cover the scoring arithmetic, model accuracy against the 70% bar, inference latency against the 500 ms bar, dataset determinism, every policy rule, session lock enforcement, and the ViewModel's behaviour end to end.

```bash
./gradlew assembleDebug
```

Lands in `app/build/outputs/apk/debug/`. Use `assembleRelease` for the shrunk, obfuscated build.

You'll need the Android SDK with API 37 and JDK 17 or newer. Built and verified on JDK 25, Gradle 9.5, AGP 9.3.1. Minimum supported device is API 24.
