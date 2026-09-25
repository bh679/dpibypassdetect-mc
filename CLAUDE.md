# Product Engineer — DPI Bypass Detect

Sibling mod of the Dungeon Train family. **Optional** client-side library: DT compiles against its
API and shows its own UI when the answer is yes. The detection lives here, not in DT, so DT's own
CurseForge review is never held up by process-list reading.

## Quick Reference

| | |
|---|---|
| Mod id | `dpibypassdetect` |
| Group | `games.brennan.dpibypassdetect` |
| API | `games.brennan.dpibypassdetect.api.DpiBypassDetect` — `detectNow()`, `hasResult()`, `matchIn(...)` |
| Version | `gradle.properties` → `mod_version` |
| Build | `./gradlew build` |
| Tests | `./gradlew :common:test` |
| Key jars | `{fabric,forge,neoforge}/build/libs/dpibypassdetect-<loader>-<v>.jar` |
| Release | `gh workflow run release.yml -f tag=v<version>` (creates the tag; never tag manually) |
| Repo | `bh679/dpibypassdetect-mc` |

## Structure

- `common/` — all logic. `ProcessNames` (session-cached OSHI name snapshot) + `api/DpiBypassDetect`
  (match list, one-shot cached probe, `-Ddpibypassdetect.test` override). No mixins, events, config,
  registries or networking.
- `fabric/`, `forge/`, `neoforge/` — thin entrypoints (`DpiBypassDetectMod.init()` logs one line).

## Rules

- **Keep the match list short and whole-name only.** It decides whether another mod nudges or
  warns a player; a false positive is worse than a miss.
- **Names only, nothing leaves the machine.** Don't add command-line, window-title, file, registry
  or network checks. The README and store listing promise this.
- The public API is consumed by DT via `compileOnly` — keep `api/DpiBypassDetect` source-compatible; rename
  = MAJOR bump.

## Standards

SemVer in `gradle.properties`: PATCH every commit, MINOR on release. bh679 Gate workflow applies
(see `~/.claude/` rules/playbooks). Releases only via release.yml dispatch — it creates the tag,
GitHub Release, and publishes to Modrinth/CurseForge when `MODRINTH_PROJECT_ID`/
`CURSEFORGE_PROJECT_ID` vars + tokens are set.

Dungeon Train consumes the **neoforge** jar via its shared `bh679` Ivy repo: asset name MUST stay
`dpibypassdetect-neoforge-<v>.jar` (flat, no `+mc` suffix).
