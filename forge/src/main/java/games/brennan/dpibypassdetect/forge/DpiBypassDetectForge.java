package games.brennan.dpibypassdetect.forge;

import games.brennan.dpibypassdetect.DpiBypassDetectMod;
import net.minecraftforge.fml.common.Mod;

/** Forge entrypoint. Runs common init; the API is plain static calls. */
@Mod(DpiBypassDetectMod.MOD_ID)
public final class DpiBypassDetectForge {

    public DpiBypassDetectForge() {
        DpiBypassDetectMod.init();
    }
}
