package games.brennan.dpibypassdetect.fabric;

import games.brennan.dpibypassdetect.DpiBypassDetectMod;
import net.fabricmc.api.ModInitializer;

/** Fabric entrypoint. Runs common init; the API is plain static calls. */
public final class DpiBypassDetectFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        DpiBypassDetectMod.init();
    }
}
