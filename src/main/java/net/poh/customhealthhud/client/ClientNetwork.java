package net.poh.customhealthhud.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.poh.customhealthhud.network.HudConfigPacket;
import net.poh.customhealthhud.network.NetworkHandler;

@Environment(EnvType.CLIENT)
public class ClientNetwork {
    public static void sendToServer(HudConfigPacket packet) {
        // Si el servidor no tiene el mod, no hay nada que enviar (solo se guarda local).
        if (!ClientPlayNetworking.canSend(NetworkHandler.CONFIG_PACKET)) {
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ClientPlayNetworking.send(NetworkHandler.CONFIG_PACKET, buf);
    }
}
