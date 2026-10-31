package net.poh.customhealthhud.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.poh.customhealthhud.client.HudConfigScreen;

@Environment(EnvType.CLIENT)
public class HudCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("hud")
                        .requires(source -> source.hasPermission(4))
                        .executes(context -> {
                            Minecraft.getInstance().execute(() ->
                                    Minecraft.getInstance().setScreen(new HudConfigScreen()));
                            return 1;
                        })));
    }
}
