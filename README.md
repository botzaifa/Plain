# Plain

Text-only Android launcher for intentional, tiered app access.

## Device

**Primary phone:** not connected during development. Run `adb shell getprop` when the physical device is available and update this section before M1 sign-off.

**Emulator (dev builds):** SDK `37` (`37.1`), release `17`, Google `sdk_gphone16k_x86_64` (Oct 2026).

## Architecture

| Layer | Role |
| --- | --- |
| `apps/` | Discovery, slots, launch targets |
| `data/` | Room, DataStore, repositories |
| `home/`, `search/`, `gate/`, `settings/` | Compose screens |
| `ui/` | Theme and optional glass |

## Honest scope

Plain only nudges what you open through Home. Recents, notifications, and system Settings remain full escape hatches.

## Switch Home app back

Settings → Apps → Default apps → Home app.

## Gate passes

A successful pass means `LauncherApps.startMainActivity` returned without throwing, not that the target app finished opening.

## Cold start

TBD on minified release (`adb shell am start-activity -W -S` × 10).
