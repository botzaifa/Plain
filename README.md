# Plain

Plain is a standalone Android launcher for assistive access and digital focus. Home shows a few **Basic** apps as large tiles. Every other **launchable** app is **gated** behind a password. A minimal accessibility service sends you back Home when a locked launchable app opens in the foreground.

Built with Kotlin and Android Views (no Compose). Package: `app.plain.launcher`.

**Made for:** digital-detox phones, calm setups for older adults, and restricted phones for children with separate passwords.

![Plain icon](assets/plain-icon.svg)

License: [MIT](LICENSE). Suggested GitHub topics: `android`, `launcher`, `digital-wellbeing`, `accessibility`, `kotlin`.

---

## Screenshots

Captured on the Android emulator (API 37). Full-resolution PNGs live in [`assets/screenshots/`](assets/screenshots/); README uses smaller previews in [`assets/screenshots/preview/`](assets/screenshots/preview/).

### First-time setup (wizard)

| Welcome | Passwords |
|:---:|:---:|
| ![Wizard welcome](assets/screenshots/preview/wizard-welcome.png) | ![Wizard passwords](assets/screenshots/preview/wizard-passwords.png) |

| Recovery code | Basic apps | Done |
|:---:|:---:|:---:|
| ![Wizard recovery](assets/screenshots/preview/wizard-recovery.png) | ![Wizard basic apps](assets/screenshots/preview/wizard-basic-apps.png) | ![Wizard done](assets/screenshots/preview/wizard-done.png) |

### Home and setup cards

When Home role or Plain gate still needs attention, a setup card appears under the clock (not a yellow banner).

| Default Home app | Plain gate off |
|:---:|:---:|
| ![Choose Home app card](assets/screenshots/preview/home-setup-card.png) | ![Open Accessibility card](assets/screenshots/preview/home-setup-a11y.png) |

**Home when everything is configured** — clock, Basic tiles, **More apps**, and **Lock**:

![Plain Home](assets/screenshots/preview/home-daily.png)

### Unlock and Admin

| More apps password | Gated app list | Admin settings |
|:---:|:---:|:---:|
| ![Password dialog](assets/screenshots/preview/password-dialog.png) | ![Gated apps](assets/screenshots/preview/gated-apps.png) | ![Admin settings](assets/screenshots/preview/admin-settings.png) |

---

## Install

### From source

```bash
git clone https://github.com/botzaifa/Plain.git
cd Plain
./gradlew assembleDebug
```

The debug APK is at `app/build/outputs/apk/debug/app-debug.apk`. Copy it to the phone and open it to install (allow installs from unknown sources if the system asks).

For a release build:

```bash
./gradlew assembleRelease
```

Release output: `app/build/outputs/apk/release/app-release.apk`.

Pre-built APKs may be attached to [GitHub Releases](https://github.com/botzaifa/Plain/releases) when published.

### On the phone

1. Install the APK.
2. Open **Plain** from the app drawer once. The setup wizard starts automatically on first launch.

Plain is meant to be **sideloaded** (not distributed through Play Store accessibility rules).

---

## First-time setup (wizard)

The wizard runs once. You can use Plain as a normal app before making it Home or turning on the gate.

| Step | What you do |
|------|-------------|
| **Welcome** | Read the intro, tap **Next**. |
| **Passwords** | Create three **different** passwords (temporary, full, admin). Each has a confirm field. Scroll if you have a smaller device and the keyboard covers the admin section. Tap **Next** when all match and meet minimum lengths. |
| **Recovery code** | A 12-character code appears **once**. Write it down offline. Check **I wrote it down**, then **Next**. |
| **Basic apps** | Tap a row (not only the checkbox) to add apps to Home. Selected apps move to the top with a short animation; use **↑** / **↓** to reorder. The default dialer stays required. Tap **Next**. |
| **Done** | Tap **Done** to open Home. |

Defaults for Basic apps when you first pick apps: Phone → Camera → Gallery → Messages (only if installed and launchable).

You are **not** asked to set Home or accessibility during the wizard on purpose. Do that when you are ready (below).

---

## Enable full protection (recommended)

For Plain to block gated apps and shortcuts (for example double-press power → camera), complete these steps.

### 1. Default Home app

- On **Home**, if you see the setup card (*Plain isn't your Home app*), tap **Choose Home app** and select **Plain** as the default launcher.
- Or: **long-press the clock** → admin password → **Default home app** → select Plain.

Press the system **Home** button and confirm Plain stays as Home.

### 2. Plain gate (accessibility)

- Use the setup card on Home if Plain gate is off, or open **Admin → Open accessibility settings**.
- Enable **Plain gate**.

**If you see “App was denied access” or “Controlled by restricted setting”** (common on Android 13+ when Plain is sideloaded):

1. In Plain: **Admin → App info** (or tap **Show steps** on the Home setup card).
2. Open the **⋮** menu (top right; some phones say **More**).
3. Tap **Allow restricted settings** and confirm.
4. Go back to **Accessibility → Downloaded apps → Plain gate** and turn it on.

Plain cannot bypass this screen; only that App info step unlocks the toggle. If Settings shows *malfunctioning*, toggle the service off and on, ensure battery is unrestricted (step 3), and reinstall the latest APK.

### 3. Battery (OEM)

Set Plain to **Unrestricted** / **Don't optimize** battery use. See [dontkillmyapp.com](https://dontkillmyapp.com) for your device.

### 4. Optional: black wallpaper

**Admin → Set black wallpaper** applies a solid black system wallpaper (no extra permission prompt beyond install-time `SET_WALLPAPER`).

---

## Daily use

### Home screen

- **Clock / date** at the top.
- **Basic apps** — large tiles; tap to open. Always available while locked (except you still need your device lock screen if the phone is locked).
- **More apps** — opens a password dialog (password only, no extra buttons). Enter **temporary**, **full**, or **admin** password:
  - **Temporary** — gated apps work for **10 minutes**, then you are sent Home; warning toast at 1 minute left; does not survive reboot.
  - **Full** — gated apps stay open until you tap **Lock**; survives reboot.
  - **Admin** — same as full unlock for apps, plus Admin access if you long-press the clock afterward.
- **Lock** — appears after an unlock; tap to lock again immediately.
- **Setup card** — under the clock when Home role or Plain gate still needs attention; use the button on the card.

### Gated apps

After unlocking via **More apps**, the alphabetical list shows every launchable app that is **not** Basic. Tap one to open it. Leaving Plain enforcement active while **locked** closes non-Basic apps (including many shortcut launches) and returns you Home.

### Device lock screen

Plain does not replace your PIN, pattern, or fingerprint. Android shows the system lock screen first when the device is locked.

### Admin (hidden entry)

**Long-press the clock** on Home → enter **admin password**. If you forgot it, tap **Forgot password?** on that dialog and enter your **recovery code** to set a new admin password (see [Recovery](#recovery)).

| Admin action | Purpose |
|--------------|---------|
| **Basic apps** | Change which apps appear on Home and their order. |
| **Restore default Basic apps** | Reset Basic list to Phone, Camera, Gallery, Messages (with dialer enforced). |
| **Change passwords** | Set new temporary, full, and admin passwords (all must stay distinct). |
| **New recovery code** | Generate a new code; shown once; old code stops working. |
| **Open accessibility settings** | Enable or fix **Plain gate**. |
| **Default home app** | System screen to pick the default launcher. |
| **App info** | Restricted settings, permissions, force-stop. |
| **Set black wallpaper** | Solid black wallpaper. |

There is no Admin tile on Home by design.

---

## Passwords (reference)

| Level | What it does | Min length |
|--------|----------------|------------|
| **Temporary** | Gated apps for 10 minutes, then Home; toast 1 minute before end; ends on reboot. | 4 |
| **Full** | Gated apps until **Lock**; survives reboot. | 6 |
| **Admin** | Admin settings; also unlocks gated apps like full. | 6 |

Wrong attempts: after 5 failures, lockout escalates (30 s → 1 min → 5 min → 15 min → 1 h). Lockout survives app restart.

---

## Recovery

- **Forgot admin password** — **Long-press the clock** → **Forgot password?** → enter your recovery code → set and confirm a **new admin password** (must differ from temporary, full, and the old admin password). A new recovery code is shown once.
- **Setup code** — shown once in the wizard; store offline in a safe place.
- **Rotate code** — Admin → **New recovery code** (shown once; the previous code stops working).
- **Last resort** — boot safe mode, then clear Plain app data (wipes all Plain settings).

---

## What stays allowed while locked

While locked, Plain sends Home only for **launchable** apps that are not Basic and not covered by unlock. **Non-launchable** foreground packages (in-call UI, keyboards, document pickers, Google Play services dialogs, and similar system helpers) are left alone so cheap OEM phones stay usable.

Also allowed: Plain itself, the **default dialer**, the **default clock/alarm app**, apps that handle **HOME** (recents / OEM launchers), **enabled keyboards** (when the OS exposes them), and a small set of **interaction packages** for share flows from Basic apps.

Gated launchable apps may flash briefly before Home.

---

## Privacy

- No network, accounts, or analytics in the app logic.
- **Plain gate** only uses window/package events, not screen content.
- Declared permissions: **SET_WALLPAPER** (optional black wallpaper) and the accessibility service binding.
- `android:allowBackup="false"`.

---

## Known limits

- Not tamper-proof: safe mode, adb, power menu, factory reset bypass Plain.
- Apps without launcher icons are not listed under gated apps and are not sent Home by the gate (they are not launchable).
- Split screen and work profiles are unsupported.
- Extra third-party HOME apps may remain reachable from Recents.
- Hardware shortcuts (camera, etc.) are mitigated when the gate is **running**; if the service is off, only hiding on Home applies.

---

## Build and test

```bash
./gradlew assembleDebug assembleRelease test
```

Debug builds may shorten temporary unlock duration via an internal override; release builds do not.

---

## Docs

- [docs/qa.md](docs/qa.md) — manual QA checklist
- [docs/api-notes.md](docs/api-notes.md) — API and allowlist notes

---

Made by [botzaifa](https://botzaifa.com). For more info visit [botzaifa.com](https://botzaifa.com).
