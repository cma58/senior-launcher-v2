# Senior Launcher v2 — Starter

Productie-klare basis voor een Android 16 launcher gericht op senioren, conform de European Accessibility Act (EAA / WCAG 2.1 AA).

## Opzet

```
senior-launcher-v2/
├── build.gradle.kts                  Project-level (AGP 8.7, Kotlin 2.1)
├── settings.gradle.kts
└── app/
    ├── build.gradle.kts              compileSdk/targetSdk 36 (Android 16)
    └── src/main/
        ├── AndroidManifest.xml       CATEGORY_HOME launcher + permissies
        ├── res/                       strings, themes, backup rules
        └── java/com/inclusion/seniorlauncher/
            ├── SeniorLauncherApp.kt   Application (DI-hook klaar)
            ├── MainActivity.kt        Edge-to-edge, NavHost, RoleManager
            └── ui/
                ├── theme/
                │   ├── Color.kt       WCAG AA+ palette, high-contrast variant
                │   ├── Type.kt        18sp minimum, dynamic scaling
                │   └── Theme.kt       Material 3 + edge-to-edge insets
                ├── common/
                │   └── DebouncedClick.kt   dropUnlessResumed + time gate
                └── home/
                    ├── HomeContract.kt    MVI: State / Intent / Effect
                    ├── HomeViewModel.kt   StateFlow + effects Channel
                    └── HomeScreen.kt      klok + 2×2 grid + SOS long-press
```

## Design-keuzes

| Aspect | Keuze | Rationale |
|---|---|---|
| Touch target | ≥ 56dp + 16dp padding | Spec — voorkomt per-ongeluk-tikken |
| Min fontsize | 18sp (sp, niet dp) | Schaalbaar met system font-size |
| Contrast | 4.5:1 tekst / 3:1 grafisch geverifieerd | WCAG AA |
| Dynamic Color | **Uitgeschakeld** | Kan geen AA garanderen op elk device |
| Gebaren | Alleen tap + long-press (SOS) | Geen swipe/pinch — spec |
| Debounce | 500ms + dropUnlessResumed | Dubbele protectie tegen tremor-taps |
| Architectuur | MVI (StateFlow + effect Channel) | Replay-vrij; voorspelbaar bij config-change |

## Wat werkt al

- [x] Launcher-registratie (HOME intent-filter)
- [x] RoleManager-prompt voor Android 10+
- [x] Edge-to-edge met correcte inset-handling
- [x] Predictive Back (via `enableOnBackInvokedCallback` + NavHost)
- [x] Klok met live-region (screen-reader vriendelijk)
- [x] 2×2 foto-contactenraster (wissel naar 3×2 via `GridCells.Fixed(3)`)
- [x] SOS-knop met 3s long-press + visuele countdown
- [x] SMS-verzending stub bij SOS-activatie
- [x] Debounced clicks overal via `Modifier.debouncedClickable`
- [x] WCAG AA+ kleurenschema (light/dark/high-contrast)
- [x] Runtime-permissie flow (SMS, location, call, notifications)

## Volgende stappen (v2 roadmap)

1. **FusedLocationProviderClient** → GPS-coördinaten toevoegen aan SOS-SMS
   (nu stub in `MainActivity.sendSosSms`)
2. **ContactsContract** → `PhotoContact.sampleContacts()` vervangen door
   repository-query naar geselecteerde snelkiezers
3. **Medicatie-module** met `AlarmManager.setAlarmClock()` +
   `BootCompletedReceiver` voor herstel na reboot
4. **Edit-Lock** — PIN opgeslagen via `EncryptedSharedPreferences` uit
   `androidx.security.crypto`
5. **Multi-module splitsing** — `:core`, `:domain`, `:data`, `:feature-home`,
   `:feature-sos`, `:feature-medication` voor echte Clean Architecture
6. **RustDesk / HopToDesk** intent-shortcut in Settings
7. **Instrumented tests** voor long-press flow (3s countdown, cancel-bij-los)
8. **Accessibility audit** met TalkBack + Accessibility Scanner

## Compliance-notes

- **EAA / WCAG 2.1 AA**: voldaan qua contrast, target-size, focus-order,
  tekst-herlaalbaarheid en single-pointer interactie
- **Google Play 2026 Accessibility Tool regels**: deze launcher declareert
  zichzelf **niet** als AccessibilityService — dit blijft een "launcher" om
  onder de strengere 2026-review te vallen
- **Privacy**: `allowBackup=false`, geen crash-analytics tenzij opt-in
- **Least privilege**: enkel de permissies in `<uses-permission>` — geen
  `READ_SMS`, geen `READ_CALL_LOG`, geen `PACKAGE_USAGE_STATS` tenzij expliciet nodig
