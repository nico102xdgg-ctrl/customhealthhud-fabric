package net.poh.customhealthhud;

import net.fabricmc.api.ModInitializer;
import net.poh.customhealthhud.network.NetworkHandler;

public class CustomHealthHudMod implements ModInitializer {
    public static final String MOD_ID = "customhealthhud";

    @Override
    public void onInitialize() {
        NetworkHandler.register();
    }
}
