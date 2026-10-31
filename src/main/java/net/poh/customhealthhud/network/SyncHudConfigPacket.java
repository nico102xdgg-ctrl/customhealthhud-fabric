package net.poh.customhealthhud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.poh.customhealthhud.config.HudConfig;

/** Servidor -> todos los clientes: aplica esta config y guárdala. */
public class SyncHudConfigPacket {
    public final HudConfig config;

    public SyncHudConfigPacket(HudConfig config) {
        this.config = config;
    }

    public SyncHudConfigPacket(FriendlyByteBuf buf) {
        this.config = NetworkHandler.readConfig(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        NetworkHandler.writeConfig(buf, this.config);
    }
}
