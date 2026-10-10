# Plain

Plain is a small Android launcher for assistive access and digital focus. Home shows a few **Basic** apps as large tiles. Every other app is **gated** behind a password. A minimal accessibility service sends you back Home when a locked app opens.

It is built with Kotlin and plain Android Views (no Compose, no database, almost no dependencies) so it runs well on cheap, older phones. Package: `app.plain.launcher`.

**Made for three uses:** a digital-detox phone, a calm and uncluttered phone for older adults, and a restricted phone for children with parental passwords.

## What you get

- **Setup wizard:** three passwords (temporary, full, admin), a one-time recovery code, and your choice of Basic apps. Becoming the Home app and turning on the gate are **not** forced during setup; you do them when you are ready.
- **Home:** a clock, a grid of large Basic app tiles, **More apps**, and a Lock button after an unlock. If the device has a system lock screen (PIN, pattern and so on), Android shows it before Home.
- **More apps:** asks for a password only. While unlocked, it opens the list of gated apps.
- **Admin:** long-press the clock and enter the admin password. Manage Basic apps, passwords, the recovery code, the Home role, accessibility, and system links.
- **Enforcement:** while locked, any launchable app that is not Basic is sent Home. Picture-in-picture windows of gated apps are dismissed while the gate is running.
- **Emergency-safe:** the default dialer is always a Basic app and cannot be removed.
- New installs are gated automatically until you add them to Basic.

## Setup

1. Sideload the APK (Plain is not aimed at Play Store accessibility policy).
2. Run the wizard: set the three passwords, **write down the recovery code**, and pick your Basic apps.
3. Set Plain as the default **Home** app (use the Home banner or Admin).
4. Enable **Plain gate** under Accessibility (from the Home banner or Admin).
5. On Android 13+ sideloads you may need **App info → Allow restricted settings** before the service can be enabled.
6. Set Plain's battery use to unrestricted. OEM skins often kill accessibility services otherwise.

## Requirements

- Android 7.0 or newer (API 24), built and tested up to API 37
- Plain as the default Home app and **Plain gate** enabled for full behavior. Without the gate, Plain still works as a launcher, but locked apps are only hidden from Home, not blocked, and the temporary access cannot close apps when it ends.

## Passwords

| Level | What it does | Minimum length |
|--------|-------------|----------------|
| **Temporary** | Opens gated apps for 10 minutes, with a toast one minute before the end, then sends you Home. Ends at reboot. | 4 |
| **Full** | Gated apps stay open until you tap **Lock**. Survives reboot. | 6 |
| **Admin** | Opens Admin settings. Also unlocks everything. | 6 |

All three passwords must be different. They are stored only as salted, iterated hashes. Wrong attempts trigger a staged lockout (30 seconds, 1 minute, 5 minutes, 15 minutes, then 1 hour) that survives restarts and reboots.

## Recovery

- During setup you receive a **recovery code** once. Store it offline. Anyone who has it can reset the admin password.
- <!-- TODO: say where the recovery code is entered in the app -->
- In **Admin → New recovery code** you can rotate the code. It is shown once after generation, and the old code stops working.
- **Last resort:** boot into safe mode, then clear Plain's app data. This wipes all Plain settings.

## Always allowed while locked

Plain itself, the default dialer, the default clock/alarm app (so a ringing alarm can always be dismissed), packages that handle HOME (recents and OEM launchers), and enabled keyboards. A gated app may be visible for a fraction of a second before Home is sent.

## Privacy

Plain has no network access, no accounts and no analytics. It declares no permissions beyond the accessibility service, and Android backups are disabled. The accessibility service only observes which app is in front. It does not read screen content.

## Known limits

- Plain is a strong nudge, not tamper-proof security.
- Safe mode, adb, the power menu, and factory reset bypass Plain.
- Apps without launcher icons do not appear in the gated list.
- Split screen and work profiles are unsupported.
- Anyone who knows a password can unlock. Lockout only slows guessing.
- Extra third-party HOME apps with launcher icons may remain reachable from Recents.
- OEM battery savers can stop the accessibility service. See [dontkillmyapp.com](https://dontkillmyapp.com).

## Build

```bash
./gradlew assembleDebug assembleRelease test
```

Release builds use R8. Debug builds may use a shorter temporary-unlock duration for testing; release builds never do.

## Docs

- [docs/qa.md](docs/qa.md): manual QA checklist and device log
- [docs/api-notes.md](docs/api-notes.md): API behavior and allowlist notes

---

Made by [botzaifa](https://botzaifa.com). For more info visit [botzaifa.com](https://botzaifa.com).
