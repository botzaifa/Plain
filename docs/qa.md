# QA log

| Device | API | Build | Date | Result |
|--------|-----|-------|------|--------|
| AVD `Plain_API24_1GB` (hw.ramSize=1024) | 24 | debug | | pending |
| Emulator Plain_API24_1GB | 24 | release | | pending |
| Pixel / API 37 emulator | 37 | release | | pending |
| Second-hand phone | | release | | pending |

## Manual checklist

- [ ] Wizard completes without Home/a11y steps; banner on Home prompts Home then accessibility
- [ ] Basic grid launches, More opens Gated after password
- [ ] TEMP expires, FULL until Lock, reboot ends TEMP not FULL
- [ ] Long-press clock → Admin (no Admin tile on Home)
- [ ] Device lock screen shown before Home when keyguard is active
- [ ] Picture-in-picture dismissed for gated apps while locked
- [ ] Recovery code resets admin only
- [ ] LOCKED: gated app from notification / Recents → Home
- [ ] Alarm dismiss with Clock not Basic
- [ ] WhatsApp attach photo/document while LOCKED
- [ ] Share sheet from Basic app
- [ ] Loop guard warning in settings after forced loop
