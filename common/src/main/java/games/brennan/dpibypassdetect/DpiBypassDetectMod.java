package games.brennan.dpibypassdetect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common init. The mod does nothing on its own — it only answers {@link DpiBypassDetect} when another mod
 * asks — so this owns the id and announces the mod at startup.
 */
public final class DpiBypassDetectMod {

    public static final String MOD_ID = "dpibypassdetect";
    public static final Logger LOGGER = LoggerFactory.getLogger("DpiBypassDetect");

    private DpiBypassDetectMod() {}

    public static void init() {
        LOGGER.info("[DpiBypassDetect] initialised — idle until another mod asks");
    }
}
