package net.poh.customhealthhud.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.poh.customhealthhud.config.HudConfig;

public class NetworkHandler {
    /** Cliente -> servidor (en Forge era el mensaje #0 del canal customhealthhud:main). */
    public static final ResourceLocation CONFIG_PACKET = new ResourceLocation("customhealthhud", "hud_config");
    /** Servidor -> clientes (en Forge era el mensaje #1). */
    public static final ResourceLocation SYNC_PACKET = new ResourceLocation("customhealthhud", "sync_hud_config");

    /** Lado servidor (y servidor integrado). */
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CONFIG_PACKET, (server, player, handler, buf, responseSender) -> {
            HudConfigPacket packet = new HudConfigPacket(buf);
            server.execute(() -> {
                System.out.println("[CustomHealthHUD] Sincronizando configuración a todos los clientes...");
                sendToAllPlayers(server, new SyncHudConfigPacket(packet.config));
                System.out.println("[CustomHealthHUD] Configuración sincronizada exitosamente!");
            });
        });
    }

    public static void sendToAllPlayers(MinecraftServer server, SyncHudConfigPacket packet) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendToPlayer(player, packet);
        }
    }

    public static void sendToPlayer(ServerPlayer player, SyncHudConfigPacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ServerPlayNetworking.send(player, SYNC_PACKET, buf);
    }

    // Mismo orden de campos que la versión Forge.
    static void writeConfig(FriendlyByteBuf buf, HudConfig c) {
        buf.writeInt(c.heartX);
        buf.writeInt(c.heartY);
        buf.writeFloat(c.heartScale);
        buf.writeInt(c.heartSpacing);
        buf.writeBoolean(c.showHotbar);
        buf.writeBoolean(c.showExpBar);
        buf.writeBoolean(c.showExpNumber);
        buf.writeBoolean(c.showFood);
        buf.writeBoolean(c.showArmor);
        buf.writeBoolean(c.showMountHealth);
        buf.writeBoolean(c.showAir);
        buf.writeBoolean(c.showPlayerHealth);
        buf.writeBoolean(c.showVanillaPlayerHealth);
        buf.writeInt(c.expBarX);
        buf.writeInt(c.expBarY);
        buf.writeInt(c.expNumberX);
        buf.writeInt(c.expNumberY);
        buf.writeFloat(c.expNumberScale);
        buf.writeInt(c.heartColorR);
        buf.writeInt(c.heartColorG);
        buf.writeInt(c.heartColorB);
        buf.writeInt(c.hotbarColorR);
        buf.writeInt(c.hotbarColorG);
        buf.writeInt(c.hotbarColorB);
        buf.writeInt(c.expBarColorR);
        buf.writeInt(c.expBarColorG);
        buf.writeInt(c.expBarColorB);
        buf.writeInt(c.expNumberColorR);
        buf.writeInt(c.expNumberColorG);
        buf.writeInt(c.expNumberColorB);
    }

    static HudConfig readConfig(FriendlyByteBuf buf) {
        HudConfig c = new HudConfig();
        c.heartX = buf.readInt();
        c.heartY = buf.readInt();
        c.heartScale = buf.readFloat();
        c.heartSpacing = buf.readInt();
        c.showHotbar = buf.readBoolean();
        c.showExpBar = buf.readBoolean();
        c.showExpNumber = buf.readBoolean();
        c.showFood = buf.readBoolean();
        c.showArmor = buf.readBoolean();
        c.showMountHealth = buf.readBoolean();
        c.showAir = buf.readBoolean();
        c.showPlayerHealth = buf.readBoolean();
        c.showVanillaPlayerHealth = buf.readBoolean();
        c.expBarX = buf.readInt();
        c.expBarY = buf.readInt();
        c.expNumberX = buf.readInt();
        c.expNumberY = buf.readInt();
        c.expNumberScale = buf.readFloat();
        c.heartColorR = buf.readInt();
        c.heartColorG = buf.readInt();
        c.heartColorB = buf.readInt();
        c.hotbarColorR = buf.readInt();
        c.hotbarColorG = buf.readInt();
        c.hotbarColorB = buf.readInt();
        c.expBarColorR = buf.readInt();
        c.expBarColorG = buf.readInt();
        c.expBarColorB = buf.readInt();
        c.expNumberColorR = buf.readInt();
        c.expNumberColorG = buf.readInt();
        c.expNumberColorB = buf.readInt();
        return c;
    }
}
