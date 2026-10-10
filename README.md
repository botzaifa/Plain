# Plain v2

Assistive-access Android launcher: large Basic app tiles on Home, everything else gated behind TEMP / FULL / ADMIN passwords, enforced by a minimal accessibility service.

## Requirements

- Android 7.0+ (API 24), tested toward API 37
- Sideload the APK (not designed for Play accessibility policy)
- After setup, set Plain as the default Home app and enable the Plain gate accessibility service when you are ready (Home banner and admin Settings; Android 13+ may require **App info → Allow restricted settings** first)

## Recovery

1. **Recovery code** (12 characters, shown once during setup): resets **admin password only** at the Gated password prompt → Forgot password?
2. **Last resort:** boot to safe mode, clear Plain app data (wipes all Plain settings).

## Always allowed while locked

Default dialer, default Clock/alarm app, apps that handle HOME (system recents / OEM launchers), keyboards, and Plain itself. Gated apps may flash briefly before Home is sent.

## Known limits

- Safe mode, adb, power menu / factory reset bypass Plain
- Apps without launcher icons are not in the Gated list
- Split screen and work profiles are unsupported
- Anyone who knows a password can unlock; lockout slows guessing
- Extra third-party HOME apps with launcher icons stay reachable from Recents (remove them if that matters)
- OEM battery savers may kill the accessibility service; use dontkillmyapp.com guidance

## Build

```bash
./gradlew assembleDebug assembleRelease test
```

Release builds minify with R8. Debug builds may shorten TEMP duration via an internal override (not present in release).

## Docs

- [docs/qa.md](docs/qa.md) — device test results
- [docs/api-notes.md](docs/api-notes.md) — API verification and allowlist evidence
