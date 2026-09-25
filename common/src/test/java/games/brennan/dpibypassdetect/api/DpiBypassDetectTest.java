package games.brennan.dpibypassdetect.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pure-logic tests for the bypass-tool name match. Only {@code matchIn} is exercised — the probe
 * proper enumerates the live process table and has no business in a unit test.
 *
 * <p>The interesting cases are all about what a process table actually hands back: casing that
 * differs from the constant, a full path instead of a bare name, and the odd null or blank entry.
 * A false positive here puts a warning in front of a player who has nothing wrong, so the negative
 * cases matter as much as the positive ones.</p>
 */
class DpiBypassDetectTest {

    @Test
    void findsZapretsWorker() {
        assertEquals("winws.exe", DpiBypassDetect.matchIn(List.of("explorer.exe", "winws.exe", "javaw.exe")));
    }

    @Test
    void findsTheSiblingTools() {
        assertEquals("goodbyedpi.exe", DpiBypassDetect.matchIn(List.of("goodbyedpi.exe")));
        assertEquals("zapret.exe", DpiBypassDetect.matchIn(List.of("zapret.exe")));
    }

    @Test
    void matchIsCaseInsensitive() {
        assertEquals("winws.exe", DpiBypassDetect.matchIn(List.of("WINWS.EXE")));
        assertEquals("winws.exe", DpiBypassDetect.matchIn(List.of("WinWS.exe")));
    }

    @Test
    void aFullPathMatchesOnItsFinalSegment() {
        assertEquals("winws.exe", DpiBypassDetect.matchIn(List.of("C:\\zapret\\bin\\winws.exe")));
        assertEquals("winws.exe", DpiBypassDetect.matchIn(List.of("/opt/zapret/winws.exe")));
    }

    @Test
    void anOrdinaryProcessListMatchesNothing() {
        assertNull(DpiBypassDetect.matchIn(List.of(
                "explorer.exe", "javaw.exe", "steam.exe", "Discord.exe", "chrome.exe")));
    }

    /** A name that merely CONTAINS a listed one is not a match — only the whole file name counts. */
    @Test
    void partialNamesDoNotMatch() {
        assertNull(DpiBypassDetect.matchIn(List.of("winws.exe.bak", "notwinws.exe", "winws")));
    }

    @Test
    void nullAndBlankEntriesAreSkippedNotThrownOn() {
        assertEquals("winws.exe", DpiBypassDetect.matchIn(Arrays.asList(null, "", "   ", "winws.exe")));
        assertNull(DpiBypassDetect.matchIn(Arrays.asList(null, "")));
    }

    @Test
    void nullAndEmptyInputsAreSafe() {
        assertNull(DpiBypassDetect.matchIn(null));
        assertNull(DpiBypassDetect.matchIn(List.of()));
    }
}
