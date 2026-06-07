<div align="center">

<img src="https://raw.githubusercontent.com/Yamzzdev/RythimMusic/main/assets/rythim-icon.png" alt="Rythim Music" width="140" />

# Rythim Music

**A sleek YouTube Music client for Android — no ads, no subscriptions, just music.**

<br/>

[![Latest Release](https://img.shields.io/github/v/release/Yamzzdev/RythimMusic?style=for-the-badge&color=a78bfa&labelColor=0d1117&logo=github)](https://github.com/Yamzzdev/RythimMusic/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Yamzzdev/RythimMusic/total?style=for-the-badge&color=a78bfa&labelColor=0d1117)](https://github.com/Yamzzdev/RythimMusic/releases)
[![License](https://img.shields.io/badge/License-GPL%20v3-a78bfa?style=for-the-badge&labelColor=0d1117)](LICENSE)

<br/>

[![Website](https://img.shields.io/badge/Website-rythimapp.my.id-a78bfa?style=flat-square&labelColor=0d1117)](https://rythimapp.my.id)
[![WhatsApp Channel](https://img.shields.io/badge/WhatsApp-Channel-25D366?style=flat-square&logo=whatsapp&logoColor=white&labelColor=0d1117)](https://whatsapp.com/channel/0029Vb7ho1XInlqVcTQ6Oc25)
[![Telegram](https://img.shields.io/badge/Telegram-@rythimapp-2CA5E0?style=flat-square&logo=telegram&logoColor=white&labelColor=0d1117)](https://t.me/rythimapp)
[![TikTok](https://img.shields.io/badge/TikTok-@ymzzzoff-black?style=flat-square&logo=tiktok&labelColor=0d1117)](https://tiktok.com/@ymzzzoff)
[![Donate](https://img.shields.io/badge/Donate-Saweria-a78bfa?style=flat-square&labelColor=0d1117)](https://saweria.co/FellMD)

<br/>

[**Download APK**](#-download) · [**Features**](#-features) · [**Build**](#-build-from-source) · [**Credits**](#-credits)

</div>

---

## ✨ Features

### Playback & Audio
- Stream any song, album, or playlist from YouTube Music
- Background playback with lockscreen controls
- Download & cache tracks for offline listening
- 10-band equalizer with AutoEQ profiles
- Audio normalization, crossfade, skip silence
- Sleep timer, tempo & pitch control

### Lyrics & AI
- Live synced lyrics (LRCLib, KuGou, BetterLyrics, Paxsenix)
- **AI lyrics translation** — multi-provider (OpenRouter, OpenAI, Claude, Gemini, XAi, Mistral, Perplexity, DeepL, Custom)
- **AI song summary** — get a short, AI-generated insight about any track
- **AI song recommendations** — describe a mood and get a curated queue (sparkle button on home)

### Library & Account
- Full YouTube Music account sync
- Import / export playlists (M3U, CSV)
- Local file playback
- Podcast support
- Backup & restore

### Interface
- Dynamic album-art color theming
- Material 3 design with 19+ color palettes
- Pure black AMOLED mode
- Home screen widgets (player, turntable, music recognizer, playlists)
- Android Auto support
- Slim & compact navigation options

### Recognition & Scrobbling
- Music recognition via ShazamKit
- Last.fm scrobbling
- Listen Together (coming soon)
- Discord Rich Presence (GMS build)

---

## 📥 Download

| Build | Description | Link |
|---|---|---|
| **FOSS** | Lightweight, no proprietary deps | [Latest Release](https://github.com/Yamzzdev/RythimMusic/releases/latest) |

> [!NOTE]
> YouTube Music must be available in your region. If not, use a VPN pointed to a supported country.

---

## 🔨 Build from Source

**Requirements:** JDK 17+, Android SDK (compileSdk 37)

```bash
# Clone
git clone https://github.com/Yamzzdev/RythimMusic.git
cd RythimMusic

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

## ❤️ Support the Project

Rythim Music is free and open source. If you enjoy it, consider supporting:

[![Saweria](https://img.shields.io/badge/Donate%20via-Saweria-a78bfa?style=for-the-badge&logo=ko-fi&logoColor=white&labelColor=0d1117)](https://saweria.co/FellMD)

---

## 🏆 Credits

**Developed & maintained by [Yamzzdev](https://github.com/Yamzzdev)**

| Role | Person |
|------|--------|
| Lead Developer | [Yamzzdev](https://github.com/Yamzzdev) |
| Collaborator | Oxyx |

### Upstream

- **[InnerTune](https://github.com/z-huang/InnerTune)** by Z-Huang — the original YouTube Music client this whole lineage is built on
- **[Metrolist](https://github.com/MetrolistGroup/Metrolist)** — direct fork base

### Libraries & Services

- **Media3 / ExoPlayer** — playback engine
- **Jetpack Compose & Material 3** — UI
- **InnerTube parser** — YouTube Music API client
- **LRCLib · KuGou · Better Lyrics · Paxsenix** — lyrics providers
- **ShazamKit** — music recognition
- **Last.fm API** — scrobbling

Thank you to all original contributors and the open-source community.

---

<div align="center">

**Rythim Music** is not affiliated with YouTube, Google LLC, or any music labels.<br/>
All trademarks belong to their respective owners.

<br/>

Made with ♥ by **Yamzzdev** · [rythimapp.my.id](https://rythimapp.my.id)

</div>
