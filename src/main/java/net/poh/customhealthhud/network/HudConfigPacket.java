package net.poh.customhealthhud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.poh.customhealthhud.config.HudConfig;

/** Cliente -> servidor: "guardé esta config, repártela a todos". */
public class HudConfigPacket {
    public final HudConfig config;

    public HudConfigPacket(HudConfig config) {
        this.config = config;
    }

    public HudConfigPacket(FriendlyByteBuf buf) {
        this.config = NetworkHandler.readConfig(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        NetworkHandler.writeConfig(buf, this.config);
    }
}
