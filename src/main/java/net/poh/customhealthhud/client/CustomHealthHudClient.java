package net.poh.customhealthhud.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.poh.customhealthhud.command.HudCommand;
import net.poh.customhealthhud.config.HudConfig;
import net.poh.customhealthhud.network.NetworkHandler;
import net.poh.customhealthhud.network.SyncHudConfigPacket;

@Environment(EnvType.CLIENT)
public class CustomHealthHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudConfig.load();

        CustomHealthOverlay.register();
        HudCommand.register();

        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.SYNC_PACKET, (client, handler, buf, responseSender) -> {
            SyncHudConfigPacket packet = new SyncHudConfigPacket(buf);
            client.execute(() -> {
                HudConfig.getInstance().copyFrom(packet.config);
                HudConfig.getInstance().save();
            });
        });
    }
}
