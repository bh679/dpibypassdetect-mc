package games.brennan.dpibypassdetect.api;

import games.brennan.dpibypassdetect.ProcessNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Public API: is a <b>DPI-bypass tool</b> — zapret, GoodbyeDPI and the like — running on this
 * machine?
 *
 * <p><b>Why this exists.</b> Players behind national DPI filtering commonly run
 * <a href="https://github.com/Flowseal/zapret-discord-youtube">zapret</a> to reach Discord and
 * YouTube. It intercepts outbound traffic at the kernel-driver level (WinDivert) and, on several of
 * its presets, mangles packets on ports above 1023 — which can silently break a mod's HTTPS calls.
 * A mod whose online features fail quiet can use this to turn that silence into one sentence the
 * player can act on (Dungeon Train does, on its title screen). This mod does nothing on its own; it
 * only answers when asked.</p>
 *
 * <h3>What it looks at, and what it does not</h3>
 * <p>Running process <b>names</b>, matched against {@link #BYPASS_PROCESSES}. Nothing else. No
 * subprocess is spawned, no command line or argument is read, no elevation is asked for, no file or
 * registry key is inspected — and the result never leaves this machine. The only trace is a single
 * log line.</p>
 *
 * <p>Enumeration is {@link ProcessNames}' OSHI snapshot. It is also the only option that works:
 * zapret runs elevated, and {@code ProcessHandle.allProcesses()} returns no command for an elevated
 * process when asked from a medium-integrity JVM. Process names come from
 * {@code NtQuerySystemInformation} and are readable without elevation.</p>
 *
 * <p>Windows-only — WinDivert is a Windows driver, and there is nothing to find anywhere else. The
 * probe is one-shot per session ({@link #detectNow()} caches), belongs off the render thread, and
 * treats any failure as "not detected".</p>
 */
public final class DpiBypassDetect {

    private static final Logger LOGGER = LoggerFactory.getLogger("DpiBypassDetect");

    /**
     * Process names that mean a DPI-bypass tool is active, lowercase.
     *
     * <p>{@code winws.exe} is zapret's worker — the one every Flowseal preset launches, and a name
     * distinctive enough that a false positive is hard to construct. The other two cover the sibling
     * tools the same players switch between. Deliberately short: callers use this to <i>mention</i>
     * something, so a name that could plausibly belong to unrelated software does not belong on it.</p>
     */
    public static final List<String> BYPASS_PROCESSES = List.of(
            "winws.exe",        // zapret (Flowseal's bundle, and upstream zapret)
            "goodbyedpi.exe",   // GoodbyeDPI
            "zapret.exe");      // zapret, when packaged under its own name

    /**
     * Dev/QA override: forces {@link #detectNow()} to report this process name without looking at
     * anything, so a caller's warning can be seen on a machine that isn't running the tool —
     * {@code -Ddpibypassdetect.test=winws.exe}.
     */
    public static final String TEST_OVERRIDE_PROPERTY = "dpibypassdetect.test";

    /** Session cache: the matched name, {@code ""} for "looked, found nothing", null for "not yet". */
    private static String cached = null;

    private DpiBypassDetect() {}

    /**
     * The bypass tool's process name if one is running, else {@code null}. Runs the probe on first
     * call and caches it for the session — the answer can change under a running game, but not in a
     * way worth re-scanning the process table on a timer for.
     *
     * <p>Blocking, and can take tens of milliseconds. Call it off the render thread.</p>
     */
    public static synchronized String detectNow() {
        if (cached == null) {
            cached = probe();
            LOGGER.info("[DpiBypassDetect] DPI-bypass check: {}",
                    cached.isEmpty() ? "no bypass tool running" : "found " + cached);
        }
        return cached.isEmpty() ? null : cached;
    }

    /** Whether the probe has already run this session, so callers can avoid blocking on it. */
    public static synchronized boolean hasResult() {
        return cached != null;
    }

    /** The probe proper. Never throws; anything unexpected reads as "nothing found". */
    private static String probe() {
        String override = System.getProperty(TEST_OVERRIDE_PROPERTY, "").trim();
        if (!override.isEmpty()) {
            LOGGER.info("[DpiBypassDetect] DPI-bypass check: forced by -D{}", TEST_OVERRIDE_PROPERTY);
            return override;
        }
        if (!isWindows()) return "";
        String match = matchIn(ProcessNames.snapshot());
        return match == null ? "" : match;
    }

    /** Whether this is a Windows client — the only platform WinDivert-based tools run on. */
    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * Pure form: the first {@link #BYPASS_PROCESSES} entry present in {@code processNames}, or
     * {@code null}. Case-insensitive, and tolerant of a full path in place of a bare name, since
     * what a process table hands back differs by platform and by OSHI version. Null and blank
     * entries are skipped rather than thrown on — this reads live system data, which is never owed
     * to be well-formed.
     */
    public static String matchIn(Collection<String> processNames) {
        if (processNames == null) return null;
        for (String raw : processNames) {
            if (raw == null || raw.isBlank()) continue;
            String name = ProcessNames.baseName(raw);
            for (String candidate : BYPASS_PROCESSES) {
                if (candidate.equals(name)) return candidate;
            }
        }
        return null;
    }
}
