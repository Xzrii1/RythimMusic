<div align="center">

<img src="https://raw.githubusercontent.com/Yamzzdev/Rythim-Music/main/assets/rythim-icon.png" alt="Rythim Music" width="140" />

# Rythim Music

**A sleek YouTube Music client for Android — no ads, no subscriptions, just music.**

<br/>

[![Latest Release](https://img.shields.io/github/v/release/Yamzzdev/Rythim-Music?style=for-the-badge&color=a78bfa&labelColor=0d1117&logo=github)](https://github.com/Yamzzdev/Rythim-Music/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Yamzzdev/Rythim-Music/total?style=for-the-badge&color=a78bfa&labelColor=0d1117)](https://github.com/Yamzzdev/Rythim-Music/releases)
[![License](https://img.shields.io/github/license/Yamzzdev/Rythim-Music?style=for-the-badge&color=a78bfa&labelColor=0d1117)](LICENSE)
[![Build](https://img.shields.io/github/actions/workflow/status/Yamzzdev/Rythim-Music/build.yml?branch=main&style=for-the-badge&color=a78bfa&labelColor=0d1117&logo=github-actions&logoColor=white)](https://github.com/Yamzzdev/Rythim-Music/actions)

<br/>

[![Website](https://img.shields.io/badge/Website-rythimapp.my.id-a78bfa?style=flat-square&labelColor=0d1117)](https://rythimapp.my.id)
[![Telegram](https://img.shields.io/badge/Telegram-@yamzzstore-2CA5E0?style=flat-square&logo=telegram&logoColor=white&labelColor=0d1117)](https://t.me/yamzzstore)
[![TikTok](https://img.shields.io/badge/TikTok-@ymzzzoff-black?style=flat-square&logo=tiktok&labelColor=0d1117)](https://tiktok.com/@ymzzzoff)
[![Donate](https://img.shields.io/badge/Donate-Saweria-a78bfa?style=flat-square&labelColor=0d1117)](https://saweria.co/FellMD)

<br/>

[**Download APK**](#-download) · [**Features**](#-features) · [**Screenshots**](#-screenshots) · [**Build**](#-build-from-source) · [**Credits**](#-credits)

</div>

---

## 📱 Screenshots

> Coming soon — contributors welcome!

---

## ✨ Features

### Playback & Audio
- Stream any song, album, or playlist from YouTube Music
- Background playback with lockscreen controls
- Download & cache tracks for offline listening
- 10-band Equalizer with AutoEQ profiles
- Audio normalization, crossfade, skip silence
- Sleep timer, tempo & pitch control

### Lyrics & Discovery
- Live synced lyrics from multiple providers (LRCLib, KuGou, BetterLyrics, Paxsenix)
- AI-powered lyrics translation
- Personalized home feed — quick picks, new releases, charts
- Music recognition via ShazamKit
- Mood & genre browsing

### Library & Account
- Full YouTube Music account sync
- Import/export playlists
- Local file playback
- Podcast support
- Backup & restore

### Interface
- Dynamic album-art color theming
- Material 3 design with 19+ color palettes
- Pure black AMOLED mode
- Home screen widgets
- Android Auto support
- Slim & compact navigation options

### Social
- Listen Together (coming soon)
- Last.fm scrobbling
- Discord Rich Presence (GMS build)

---

## 📥 Download

| Build | Description | Link |
|---|---|---|
| **FOSS** | Lightweight, no proprietary deps | [Latest Release](https://github.com/Yamzzdev/Rythim-Music/releases/latest) |
| **Nightly** | Latest CI build from `main` | [GitHub Actions](https://github.com/Yamzzdev/Rythim-Music/actions) |

> [!NOTE]
> YouTube Music must be available in your region. If not, use a VPN pointed to a supported country.

---

## 🔨 Build from Source

**Requirements:** JDK 17+, Android SDK (compileSdk 37)

```bash
# Clone
git clone https://github.com/Yamzzdev/Rythim-Music.git
cd Rythim-Music

# FOSS debug build (recommended)
./gradlew :app:assembleFossDebug

# Output
app/build/outputs/apk/foss/debug/app-foss-debug.apk
```

### Flavors

| Flavor | Cast | Discord RPC | Updater |
|--------|------|-------------|---------|
| `foss` | — | — | ✓ |
| `gms` | ✓ | ✓ | ✓ |
| `izzy` | — | — | — |

---

## 🤝 Contributing

Pull requests are welcome! Please:
- Branch off `main` using `fix/`, `feat/`, `ref/`, `docs/` prefix
- Follow Kotlin/Android best practices
- New strings go in `metrolist_strings.xml`, not `strings.xml`
- Do **not** bump the version — that's handled by the maintainer

---

## ❤️ Support the Project

Rythim Music is free and open source. If you enjoy it, consider supporting:

[![Saweria](https://img.shields.io/badge/Donate%20via-Saweria-a78bfa?style=for-the-badge&logo=ko-fi&logoColor=white&labelColor=0d1117)](https://saweria.co/FellMD)

---

## 🏆 Credits

**Developed & maintained by [Yamzzdev](https://github.com/Yamzzdev)**

| Role | Person |
|------|--------|
| Lead Developer | [Yamzzdev](https://github.com/Yamzzdev) |
| Collaborator | [Oxyx](https://github.com/yamzzdev) |

Built on top of [Metrolist](https://github.com/MetrolistGroup/Metrolist) and [InnerTune](https://github.com/z-huang/InnerTune) — thank you to all original contributors.

---

<div align="center">

**Rythim Music** is not affiliated with YouTube, Google LLC, or any music labels.<br/>
All trademarks belong to their respective owners.

<br/>

Made with ♥ by **Yamzzdev** · [rythimapp.my.id](https://rythimapp.my.id)

</div>
