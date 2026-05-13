# GitHub Actions setup — wat je moet doen

## ⚠️ Voordat je de workflow activeert

De CI zal alleen succesvol een APK bouwen als deze drie blockers zijn opgelost. **Zonder deze fixes faalt de eerste build.**

1. **`v2-fixes`** moet toegepast zijn (uit eerder bericht):
   - `EXPRESS_NOTIFICATION_PRIORITY` permissie uit Manifest weg
   - Launcher icons (`mipmap-anydpi-v26/`) aanwezig
   - Receiver stubs aanwezig (`medication/MedicationAlarmReceiver.kt`, `BootCompletedReceiver.kt`)

2. **`v3-update`** toegepast (voor DataStore-features):
   - `app/build.gradle.kts` met `androidx.datastore:datastore-preferences` dep
   - Alle nieuwe Kotlin-bestanden

3. **Oude workflow verwijderen**:
   - Ga naar je repo → `.github/workflows/`
   - Verwijder het bestaande `ci.yml` (of hoe het oude bestand ook heet)
   - Voeg dit nieuwe `android.yml` toe

## Installatie-stappen

### Optie A — Via GitHub website
1. Ga naar https://github.com/cma58/senior-launcher-v2
2. Klik op **Actions** in de top-nav → klik **set up a workflow yourself** (of bewerk de bestaande)
3. Kopieer de inhoud van `.github/workflows/android.yml` (uit deze zip)
4. Bestandsnaam: `android.yml`
5. Pad: `.github/workflows/android.yml`
6. **Verwijder eventuele oude workflow-bestanden** in dezelfde map
7. Commit: "ci: add Android build & APK release workflow"

### Optie B — Via terminal
```bash
cd senior-launcher-v2

# Verwijder de oude starter workflow (pas naam aan als nodig)
rm .github/workflows/ci.yml

# Kopieer de nieuwe
mkdir -p .github/workflows
cp /pad/naar/uitgepakte/zip/.github/workflows/android.yml .github/workflows/

git add .github/workflows/
git commit -m "ci: replace starter workflow with Android APK builder"
git push
```

## Wat gebeurt er na de push

Zodra je dit pusht naar `main`:

1. **GitHub Actions start automatisch** (zie tab "Actions" in je repo)
2. Eerste run duurt ~6-8 minuten (volgende runs ~3 min dankzij caching)
3. Bij succes verschijnt:
   - **Workflow artifact** "senior-launcher-buildN-xxxxxxx-debug.apk" (klik op de run → Artifacts)
   - **GitHub Release** met tag `latest` op je repo's Releases pagina
4. Bij falen krijg je een e-mail met de error log

## Hoe gebruikers de APK downloaden

**Permalink naar nieuwste build:**
```
https://github.com/cma58/senior-launcher-v2/releases/latest
```

Of via de specifieke commit-build als workflow artifact (alleen voor 30 dagen).

## Een echte versie publiceren

Als je klaar bent voor een echte versie:
```bash
git tag v2.1.0
git push --tags
```

Dat triggert de "Create versioned release" stap — maakt een schone GitHub Release met auto-gegenereerde release notes uit alle commits sinds de vorige tag.

## Veelvoorkomende eerste-keer-problemen

| Foutmelding in Actions log | Oorzaak | Oplossing |
|---|---|---|
| `resource mipmap/ic_launcher not found` | v2-fixes niet toegepast | Pas eerst senior-launcher-v2-fixes.zip toe |
| `Class .medication.MedicationAlarmReceiver not found` | Receiver stubs ontbreken | Idem |
| `Unresolved reference: datastore` | v3-update niet toegepast | Pas senior-launcher-v3.zip toe |
| `keystore.jks not found` | Probeer je release te bouwen zonder keystore | Niet doen — wij bouwen alleen debug |
| `Permission denied: gradlew` | gradlew is niet uitvoerbaar | Workflow doet `chmod +x` automatisch |

## Later: release-signing voor Play Store

Wanneer je naar Play Store wil, vraag het me — dan loop ik je door deze stappen:
1. Een keystore genereren (`keytool`)
2. De 4 GitHub Secrets aanmaken
3. `signingConfigs` toevoegen in `app/build.gradle.kts`
4. De extra workflow step uit het commentaarblok activeren

Voor nu is **debug APK** prima voor testen, demo's aan mantelzorgers, en zelf-installatie.
