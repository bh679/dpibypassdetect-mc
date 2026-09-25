package games.brennan.dpibypassdetect.neoforge;

import games.brennan.dpibypassdetect.DpiBypassDetectMod;
import net.neoforged.fml.common.Mod;

/** NeoForge entrypoint. Runs common init; the API is plain static calls. */
@Mod(DpiBypassDetectMod.MOD_ID)
public final class DpiBypassDetectNeoForge {

    public DpiBypassDetectNeoForge() {
        DpiBypassDetectMod.init();
    }
}
