# v3 Update — Sectioned Grid + Home Customisation + Quick Settings

Geïnspireerd op drie referentiebeelden. Per beeld is de meest waardevolle feature gedestilleerd en geïntegreerd.

## Image 1 → Kleur-gecodeerd sectie-grid

Vervangt de 2×2 fotocontacten-grid op het hoofdscherm met **4 kleur-gecodeerde secties** van ieder 3 tegels:

| Sectie | Tegels | Achtergrond |
|---|---|---|
| Bellen | Recent · Bellen · Contacten | Zacht groen (`#C8E6C9`) |
| Berichten | WhatsApp · Assistent · Berichten | Zacht teal (`#B2DFDB`) |
| Media | Foto's · Camera · Video | Zacht blauw (`#BBDEFB`) |
| Hulpmiddelen | Medicijnen · Alle apps · Wekker | Licht grijs (`#E0E0E0`) |

Kleur per sectie geeft cognitieve houvast: "wat voor soort knop tik ik?" Alle achtergronden zijn AA+ tegen de tekstkleur (≥14:1).

## Image 2 → Home Customisation

Nieuw scherm `SettingsRoute`, bereikbaar via tandwiel-icoon rechtsboven. Bevat:

- **Toggle:** Sectietitels tonen/verbergen
- **Slider:** Aantal kolommen (2–4)
- **Slider:** Icoongrootte (64–128 dp)
- **Slider:** Tekstgrootte (18–32 sp, harde ondergrens)
- **Toggle:** Hoog contrast modus
- **Live voorbeeld** dat direct verandert bij slider-beweging

Wijzigingen worden persistent opgeslagen via Jetpack DataStore Preferences. Geen restart nodig — `HomeViewModel` observeert de preferences-flow en update direct.

## Image 3 → Snelinstellingen

Nieuw scherm `QuickSettingsRoute`, bereikbaar via "tune" icoon naast tandwiel. **2×3 grid** met grote toggle-tegels:

| Tegel | Gedrag |
|---|---|
| Wifi | Opent Settings.Panel.ACTION_WIFI (Android 10+) — toggleable zonder app te verlaten |
| Bluetooth | Opent Bluetooth-instellingenpaneel |
| Helderheid | Opent Display-instellingen |
| Zaklamp | **Direct toggle** via `CameraManager.setTorchMode()` — geen permissie nodig |
| Vliegmodus | Opent vliegmodus-instellingen (Android 4.2+ verbiedt programmatisch toggle) |
| Geluid | Opent geluidsinstellingen |

Reden voor "open panel" ipv direct toggle: Google verbiedt sinds Android 10 dat apps Wifi/BT/etc. zonder gebruikersbevestiging schakelen. Het Settings Panel is de officiële, veilige route.

## Architectuur-aanpassingen

**Nieuw:** `data/preferences/LauncherPreferences.kt` — DataStore wrapper met min/max coercion. Hard floor van 18sp voor tekstgrootte (EAA-eis blijft gegarandeerd).

**Nieuw:** `ui/settings/` en `ui/quicksettings/` — beide met MVI-pattern (Contract/State/Intent/Effect, ViewModel).

**HomeViewModel** is nu een `AndroidViewModel` (heeft Application nodig voor DataStore) en combineert clock-loop + 4 preference-flows via `combine()`.

**MainActivity** heeft een tile-intent-resolver die `TileIntent` mapt naar echte Android Intents (`ACTION_DIAL`, `MediaStore.ACTION_IMAGE_CAPTURE`, etc.) met graceful fallback bij `ActivityNotFoundException`.

## Bestanden in deze update

```
app/build.gradle.kts                                          # ← bijgewerkt: + DataStore dep
app/src/main/java/com/inclusion/seniorlauncher/
  MainActivity.kt                                             # ← bijgewerkt: + nav routes, intent resolver
  data/preferences/LauncherPreferences.kt                     # ← nieuw
  ui/theme/Color.kt                                           # ← bijgewerkt: + sectie-kleuren
  ui/home/HomeContract.kt                                     # ← bijgewerkt: AppSection + HomeTile + TileIntent
  ui/home/HomeViewModel.kt                                    # ← bijgewerkt: AndroidViewModel + prefs
  ui/home/HomeScreen.kt                                       # ← herwerkt: sectioned grid
  ui/settings/SettingsContract.kt                             # ← nieuw
  ui/settings/SettingsViewModel.kt                            # ← nieuw
  ui/settings/SettingsScreen.kt                               # ← nieuw
  ui/quicksettings/QuickSettingsContract.kt                   # ← nieuw
  ui/quicksettings/QuickSettingsViewModel.kt                  # ← nieuw
  ui/quicksettings/QuickSettingsScreen.kt                     # ← nieuw
```

13 bestanden — 7 nieuw, 6 bijgewerkt.

## Commit-stappen

```bash
cd senior-launcher-v2
# Pak de zip uit, kopieer de inhoud van v3/ over de bestaande bestanden
# (verstrek conflicts negeren — alles is bedoeld om te vervangen)

git add .
git commit -m "feat: add sectioned home grid, home customisation screen, quick settings panel

- Replace 2x2 photo grid with 4 colour-coded category sections (Image 1)
- Add Home Customisation screen with title toggle + 3 sliders + live preview (Image 2)
- Add Quick Settings panel with 6 large toggle tiles, flashlight via CameraManager (Image 3)
- Add DataStore-backed preferences with persistent customisation
- Refactor HomeViewModel to AndroidViewModel observing preference flows
- Add tile intent resolver mapping TileIntent to Android Intents"

git push
```
