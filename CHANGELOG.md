# Changelog

All notable changes to **Rythim Music** are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [13.5.0] — 2026-06-08

First public release on this repository.

### Added

- **AI Song Recommendations** — sparkle FAB on the home screen opens a
  bottom sheet where you describe a mood, vibe, or situation. The AI
  returns 10 songs, each resolved through YouTube Music search. Tap one
  to play with the rest queued, or "Putar semua" to play the full list.
  - Provider picker inside the sheet (OpenRouter / OpenAI / Perplexity /
    Claude / Gemini / XAi / Mistral / Custom). Switching providers
    rewrites the base URL and default model in one transaction so the
    settings can never drift.
  - Inline API-key editor — no need to leave the home screen to set up.
  - Output language follows your AI translation target language.

- **AI Song Summary** — in the lyrics menu, get a 3–4 sentence,
  AI-generated insight about the current track. Same provider matrix
  as the recommendation feature, including a dedicated Anthropic
  Messages API path for Claude.

- **AI Lyrics Translation** — translate lyrics into any language using
  any supported provider, with a configurable system prompt.

- **Spotify-style mini player** — locked as the default mini-player
  design.

- **New app icon** — soft purple waveform on a dark gradient,
  with monochrome variant for themed icons on Android 13+.

### Changed

- **Big-player controls reordered** to a Spotify-like flow:
  `[shuffle] [prev] [play] [next] [repeat]`.
- **Default big-player design** is now the legacy layout (the new design
  toggle was removed).
- **Listen Together** in the player menu is now gated as "Coming Soon"
  while the feature is still in active development.
- **Welcome / force-login screen removed** — the app no longer blocks
  the home screen behind a Google sign-in prompt. Login stays available
  from Settings.
- **Settings cleanup** — removed the conflicting "new mini-player" and
  "new player" design toggles (they had inconsistent defaults across
  call sites, which surfaced as occasional UI flips).

### Fixed

- `MediaMetadata.isLocal` reference in the mini-player heart button
  (the property lives on `SongEntity`).
- Welcome screen was leaking the home page through it because of a
  semi-transparent gradient stop.
- 401 "Missing Authentication header" on Gemini / OpenAI when
  `aiProvider`, `openRouterBaseUrl`, and `openRouterModel` had drifted
  apart in DataStore.

### Credits

Forked from [Metrolist](https://github.com/MetrolistGroup/Metrolist),
which is itself based on [InnerTune](https://github.com/z-huang/InnerTune)
by Z-Huang. Thank you to all upstream contributors and the open-source
lyrics & music recognition communities (LRCLib, KuGou, Better Lyrics,
Paxsenix, ShazamKit).

---

[13.5.0]: https://github.com/Yamzzdev/RythimMusic/releases/tag/v13.5.0
