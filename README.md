# DPI Bypass Detect

A tiny **client-side library mod** for Minecraft 1.21.1 (Fabric, Forge, NeoForge) that answers one
question for other mods: is a DPI-bypass tool (zapret, GoodbyeDPI) running? Those tools can silently break a mod's online features, and knowing about it lets the mod tell the player why.

It does nothing on its own. Another mod calls it, and decides what (if anything) to show.
[Dungeon Train](https://github.com/bh679/dungeon-train-mc) is the first user.

## What it looks at, and what it does not

- **Running process names only**: the same list your OS task manager shows. The only names it
  looks for are: `winws.exe` (zapret), `goodbyedpi.exe`, `zapret.exe`.
- No command lines, no window titles, no files, no registry, no network, no elevation.
- The result **never leaves your machine**. The only trace is one line in the game log.
- Windows only. The tools it looks for only exist on Windows, so elsewhere it doesn't look at all.
- Checked once per game session, on request, off the render thread. Any failure reads as "not
  detected".

Process names are read through OSHI, which ships with Minecraft itself (vanilla's system report
uses it), so there are no extra dependencies.

## For mod developers

```java
import games.brennan.dpibypassdetect.api.DpiBypassDetect;

// Off the render thread — the first call enumerates processes (tens of ms), then it is cached.
String found = DpiBypassDetect.detectNow();   // matched process name, or null
boolean ready = DpiBypassDetect.hasResult();  // has the one-shot probe run yet?
```

Declare it as an **optional** dependency and guard calls with your loader's "is mod loaded"
check. For UI testing on a machine without the software, launch with
`-Ddpibypassdetect.test=<process name>` to force a match.

## Licence

PolyForm Shield 1.0.0. See [LICENSE](LICENSE).
