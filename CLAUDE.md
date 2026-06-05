# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Rythim Music is a YouTube Music client for Android, forked from Metrolist (itself based on InnerTune). Written in Kotlin with Jetpack Compose, Material 3, and GPL-3.0 license.

- **Package**: `com.rythim.music`
- **Version**: 13.5.0 (versionCode 148), minSdk 26, targetSdk 36, compileSdk 37
- **Owner**: Yamzzdev (GitHub: `@Yamzzdev`, TikTok: `@ymzzzoff`, website: `yamzzprofile.eu.cc`)
- **Donation**: `saweria.co/FellMD`

## Build commands

```bash
# FOSS debug (default, recommended for development)
./gradlew :app:assembleFossDebug

# FOSS release (what CI builds)
./gradlew :app:assembleFossRelease

# Clean build
./gradlew clean :app:assembleFossDebug
```

APK output: `app/build/outputs/apk/foss/debug/app-foss-debug.apk`

## Product flavors

Three flavors in `app/build.gradle.kts` (dimension: `variant`):

| Flavor | Cast | Discord RPC | Updater | Notes |
|--------|------|-------------|---------|-------|
| `foss` | No | No | Yes | **Default**. No proprietary deps. |
| `gms` | Yes | Yes | Yes | Needs CMake + Discord SDK AAR. CI broken. |
| `izzy` | No | No | No | F-Droid compliant. Minimal. |

Flavor-specific sources live in `app/src/{foss,gms,izzy}/`.

## Module architecture

```
app/            Main Android app (Compose UI, Media3 player, Room DB, Hilt DI)
innertube/      YouTube Music API client (InnerTube parser, browse/search/player endpoints)
kugou/          KuGou lyrics provider (Chinese music platform)
lrclib/         LRCLib lyrics provider (community-synced lyrics database)
betterlyrics/   Better Lyrics integration (syllable-synced karaoke lyrics)
shazamkit/      Music recognition via Shazam API
paxsenix/       Paxsenix lyrics API provider
lastfm/         Last.fm scrobbling integration
metroproto/     Protobuf schema (listentogether.proto — auto-generates Java/Kotlin at build time)
```

## Key architecture details

- **DI**: Hilt (`AppModule.kt` in `di/`). ViewModels use `@HiltViewModel`.
- **Database**: Room with KSP for compile-time code gen. Schema at `app/schemas/`.
- **Network**: Ktor client (CIO engine) + Guava futures bridge. YouTube Music uses InnerTube protobuf protocol.
- **Playback**: Media3 ExoPlayer via `MusicService.kt` (foreground service). `PlayerConnection.kt` bridges service and Compose UI.
- **Theme**: Custom `RythimTheme` in `ui/theme/Theme.kt`. Default seed color `#A78BFA` (purple). Uses MaterialKolor + Palette. Supports dynamic colors, pure black, and 19 manual palettes.
- **Protobuf**: `metroproto/listentogether.proto` is compiled at build time by `GenerateProtoTask` (downloads protoc, generates lite Java/Kotlin). If proto file missing, generation is skipped with a warning.
- **Strings**: New app strings go in `app/src/main/res/values/metrolist_strings.xml`, NOT `strings.xml`. Translation files are `values-{lang}/metrolist_strings.xml`.
- **CI**: GitHub Actions in `.github/workflows/build.yml` with two jobs: `foss` and `gms`. Only `foss` is expected to pass.

## Code conventions

6. All string edits go to `metrolist_strings.xml`, not `strings.xml`. Do not edit translation XMLs directly.
7. Follow Kotlin/Android best practices.
8. Comments only for complex/non-obvious logic.
9. Prioritize performance, battery efficiency, and maintainability.
10. Do NOT bump the app version — only core team does manual version bumps.

## Git workflow

- Branch naming: `fix/`, `feat/`, `ref/`, `docs/`, `chore/` prefix
- Commit format: `type(scope): short description`
- Always branch from latest `main`
- Owner uses `git push origin main` directly (no PR workflow for solo dev)
