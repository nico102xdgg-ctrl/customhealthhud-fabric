package net.poh.customhealthhud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.poh.customhealthhud.config.HudConfig;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class CustomHealthOverlay {
    private static final ResourceLocation GUI_ICONS = new ResourceLocation("minecraft:textures/gui/icons.png");

    public static void register() {
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            renderHealth(graphics, width, height);
            renderExpBar(graphics, width, height);
            renderExpNumber(graphics, width, height);
        });
    }

    private static boolean shouldRender(Minecraft mc, LocalPlayer player) {
        if (player == null || mc.gameMode == null) {
            return false;
        }
        GameType mode = mc.gameMode.getPlayerMode();
        return mode != GameType.CREATIVE && mode != GameType.SPECTATOR;
    }

    private static void renderHealth(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!shouldRender(mc, player)) {
            return;
        }
        HudConfig cfg = HudConfig.getInstance();
        if (!cfg.showPlayerHealth) {
            return;
        }

        int maxHealth = (int) Math.ceil((double) player.getMaxHealth());
        int health = (int) Math.ceil((double) player.getHealth());
        int absorption = (int) player.getAbsorptionAmount();
        boolean hurt = player.hurtTime > 0;
        boolean hardcore = mc.level.getLevelData().isHardcore();

        int spacing = (int) ((float) cfg.heartSpacing * cfg.heartScale);
        int heartCount = Math.max(maxHealth, absorption) / 2;
        int startX = (screenWidth / 2 - (heartCount * spacing) / 2) + cfg.heartX;
        int baseY = screenHeight + cfg.heartY;

        RenderSystem.enableBlend();
        for (int i = 0; i < heartCount; i++) {
            int x = startX + i * spacing;
            boolean half = i == health / 2 && health % 2 != 0;
            float shake = hurt ? (float) Math.sin((double) mc.gui.getGuiTicks() * 0.5) * 10.0f : 0.0f;

            int drawX = x;
            int drawY = baseY;
            if (health <= 8 && i < health / 2) {
                float intensity = (float) (8 - health) * 0.5f;
                drawX = (int) ((float) drawX + (float) Math.sin((double) ((mc.gui.getGuiTicks() + i * 5) * 2)) * intensity);
                drawY = (int) ((float) drawY + (float) Math.cos((double) ((mc.gui.getGuiTicks() + i * 5) * 2)) * intensity);
            }

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate((float) drawX + 4.5f, (float) drawY + 4.5f, 0.0f);
            pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians((double) shake)));
            pose.scale(cfg.heartScale, cfg.heartScale, 1.0f);
            pose.translate(-((float) drawX + 4.5f), -((float) drawY + 4.5f), 0.0f);

            float r = (float) cfg.heartColorR / 255.0f;
            float g = (float) cfg.heartColorG / 255.0f;
            float b = (float) cfg.heartColorB / 255.0f;
            float a = hurt ? 0.5f : 1.0f;
            RenderSystem.setShaderColor(r, g, b, a);

            int uContainer = 16;
            int uFull = 52;
            int uHalf = 61;
            int v = hardcore ? 45 : 0;

            graphics.blit(GUI_ICONS, drawX, drawY, uContainer, v, 9, 9);
            if (i < health / 2) {
                graphics.blit(GUI_ICONS, drawX, drawY, uFull, v, 9, 9);
            } else if (half) {
                graphics.blit(GUI_ICONS, drawX, drawY, uHalf, v, 9, 9);
            }
            if (i < absorption / 2) {
                graphics.blit(GUI_ICONS, drawX, drawY, 160, v, 9, 9);
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            pose.popPose();
        }
        RenderSystem.disableBlend();
    }

    private static void renderExpBar(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!shouldRender(mc, player)) {
            return;
        }
        HudConfig cfg = HudConfig.getInstance();
        if (!cfg.showExpBar) {
            return;
        }

        int x = screenWidth / 2 + cfg.expBarX;
        int y = screenHeight + cfg.expBarY;
        float progress = player.experienceProgress;
        int filled = (int) (182.0f * progress);

        RenderSystem.enableBlend();
        graphics.fill(x - 91, y, x + 91, y + 5, 0xFF000000);
        for (int i = 0; i < filled; i++) {
            float t = (float) i / 182.0f;
            int r = (int) ((float) cfg.expBarColorR * (0.3f + t * 0.7f));
            int g = (int) ((float) cfg.expBarColorG * (0.3f + t * 0.7f));
            int b = (int) ((float) cfg.expBarColorB * (0.3f + t * 0.7f));
            int color = 0xFF000000 | (r << 16) | (g << 8) | b;
            graphics.fill((x - 91) + i, y, (x - 91) + i + 1, y + 5, color);
        }
        RenderSystem.disableBlend();
    }

    private static void renderExpNumber(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!shouldRender(mc, player)) {
            return;
        }
        HudConfig cfg = HudConfig.getInstance();
        if (!cfg.showExpNumber) {
            return;
        }

        int x = screenWidth / 2 + cfg.expNumberX;
        int y = screenHeight + cfg.expNumberY;
        int level = player.experienceLevel;
        int color = 0xFF000000 | (cfg.expNumberColorR << 16) | (cfg.expNumberColorG << 8) | cfg.expNumberColorB;
        String text = String.valueOf(level);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate((float) x, (float) (y + 4), 0.0f);
        pose.scale(cfg.expNumberScale, cfg.expNumberScale, 1.0f);
        pose.translate((float) (-x), (float) (-(y + 4)), 0.0f);

        Font font = mc.font;
        int textWidth = font.width(text);
        int textX = x - textWidth / 2;
        int outline = 0xFF000000;

        // contorno negro (8 direcciones) + texto de color
        drawText(graphics, font, text, textX - 1, y, outline);
        drawText(graphics, font, text, textX + 1, y, outline);
        drawText(graphics, font, text, textX, y - 1, outline);
        drawText(graphics, font, text, textX, y + 1, outline);
        drawText(graphics, font, text, textX - 1, y - 1, outline);
        drawText(graphics, font, text, textX + 1, y - 1, outline);
        drawText(graphics, font, text, textX - 1, y + 1, outline);
        drawText(graphics, font, text, textX + 1, y + 1, outline);
        drawText(graphics, font, text, textX, y, color);

        pose.popPose();
    }

    private static void drawText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        font.drawInBatch(text, (float) x, (float) y, color, false,
                graphics.pose().last().pose(), graphics.bufferSource(),
                Font.DisplayMode.NORMAL, 0, 15728880);
    }
}
