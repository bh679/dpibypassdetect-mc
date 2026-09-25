package games.brennan.dpibypassdetect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One session-cached snapshot of the <b>names</b> of the processes running on this machine.
 *
 * <p>Names only. No command line, no arguments, no elevation, no file or registry key — and the
 * snapshot never leaves this mod except to {@link DpiBypassDetect#matchIn}. Enumeration goes through
 * <b>OSHI</b>, which ships with Minecraft itself (vanilla's crash/system report uses it), so there
 * is no extra dependency. Process names come from {@code NtQuerySystemInformation} on Windows and
 * from the platform equivalents elsewhere, all readable without elevation.</p>
 *
 * <p>Blocking (tens of milliseconds); call it off the render thread. Any failure reads as an empty
 * table.</p>
 */
public final class ProcessNames {

    private static final Logger LOGGER = LoggerFactory.getLogger("DpiBypassDetect");

    /** Session cache: immutable list of names, or null for "not yet taken". */
    private static List<String> cached = null;

    private ProcessNames() {}

    /** The snapshot, taken on first call and cached for the session. Never null, never throws. */
    public static synchronized List<String> snapshot() {
        if (cached == null) {
            cached = enumerate();
        }
        return cached;
    }

    /** Reads the live process table into an immutable list; anything unexpected reads as empty. */
    private static List<String> enumerate() {
        try {
            List<String> names = new ArrayList<>();
            for (oshi.software.os.OSProcess process
                    : new oshi.SystemInfo().getOperatingSystem().getProcesses()) {
                names.add(process.getName());
            }
            return List.copyOf(names.stream().filter(n -> n != null).toList());
        } catch (Throwable t) {
            // Best-effort courtesy for the calling mod, never a reason to disturb the client.
            LOGGER.debug("[DpiBypassDetect] Could not enumerate running processes", t);
            return List.of();
        }
    }

    /**
     * Lowercased final path segment of {@code raw}, handling both separators — what a process table
     * hands back differs by platform and by OSHI version, and a match should not care.
     */
    public static String baseName(String raw) {
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        int cut = Math.max(lower.lastIndexOf('/'), lower.lastIndexOf('\\'));
        return cut < 0 ? lower : lower.substring(cut + 1);
    }
}
