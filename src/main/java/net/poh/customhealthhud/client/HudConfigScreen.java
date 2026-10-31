package net.poh.customhealthhud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.poh.customhealthhud.config.HudConfig;
import net.poh.customhealthhud.network.HudConfigPacket;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class HudConfigScreen extends Screen {
    private final HudConfig tempConfig;
    private int currentTab;
    private int scrollOffset;
    private int maxScrollOffset;
    private boolean draggingSlider;
    private int draggingSliderIndex;
    private boolean isDraggingElement;
    private String dragElement;
    private int dragOffsetX;
    private int dragOffsetY;

    private static final ResourceLocation GUI_ICONS = new ResourceLocation("minecraft:textures/gui/icons.png");

    public HudConfigScreen() {
        super(Component.literal("HUD Config"));
        this.tempConfig = new HudConfig();
        this.currentTab = 1;
        this.scrollOffset = 0;
        this.maxScrollOffset = 0;
        this.draggingSlider = false;
        this.draggingSliderIndex = -1;
        this.isDraggingElement = false;
        this.dragElement = "";
        this.tempConfig.copyFrom(HudConfig.getInstance());
    }

    @Override
    protected void init() {
        this.maxScrollOffset = 700;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int w = Math.min(this.width - 40, 900);
        int h = Math.min(this.height - 40, 550);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        int leftW = (int) ((float) w * 0.35f);
        int divX = x + leftW;
        int rightW = w - leftW;
        PoseStack ps = graphics.pose();

        fillRect(ps, x, y, w, h, -12566464);
        fillRect(ps, x + 1, y + 1, w - 2, h - 2, -16777216);
        fillRect(ps, divX - 1, y + 1, 1, h - 2, -12566464);

        renderTabs(ps, graphics, x, y, w, mouseX, mouseY);

        graphics.enableScissor(x + 1, y + 31, (x + leftW) - 1, (y + h) - 51);
        if (this.currentTab == 1) {
            renderColorsPanel(ps, graphics, x, y, leftW, h, mouseX, mouseY);
        } else if (this.currentTab == 0) {
            renderToolsPanel(ps, graphics, x, y, leftW, h, mouseX, mouseY);
        }
        graphics.disableScissor();

        renderPreview(ps, graphics, divX, y, rightW, h, mouseX, mouseY);
        renderButtons(ps, graphics, x, y, w, h, mouseX, mouseY);
    }

    private void renderTabs(PoseStack ps, GuiGraphics g, int x, int y, int w, int mouseX, int mouseY) {
        int tabH = 30;
        int tabW = 100;
        int tx = x + 1;
        fillRect(ps, tx, y + 1, tabW, tabH, this.currentTab != 0 ? -14013910 : -39424);
        drawCenteredString(ps, g, "Tools", tx + tabW / 2, y + 10, -1);
        int tx2 = tx + tabW;
        fillRect(ps, tx2, y + 1, tabW, tabH, this.currentTab != 1 ? -14013910 : -39424);
        drawCenteredString(ps, g, "Colors", tx2 + tabW / 2, y + 10, -1);
    }

    private void renderToolsPanel(PoseStack ps, GuiGraphics g, int x, int y, int leftW, int h, int mouseX, int mouseY) {
        int bx = x + 15;
        int by = (y + 40) - this.scrollOffset;
        by = drawCategory(ps, g, "=== Health ===", bx, by);
        by = drawCheckbox(ps, g, "Custom Health", this.tempConfig.showPlayerHealth, bx, by);
        by = drawCheckbox(ps, g, "Vanilla Health", this.tempConfig.showVanillaPlayerHealth, bx, by);
        by += 10;
        by = drawCategory(ps, g, "=== HUD Elements ===", bx, by);
        by = drawCheckbox(ps, g, "Hotbar", this.tempConfig.showHotbar, bx, by);
        by = drawCheckbox(ps, g, "Food", this.tempConfig.showFood, bx, by);
        by = drawCheckbox(ps, g, "Armor", this.tempConfig.showArmor, bx, by);
        by = drawCheckbox(ps, g, "Air", this.tempConfig.showAir, bx, by);
        by = drawCheckbox(ps, g, "Mount Health", this.tempConfig.showMountHealth, bx, by);
        by += 10;
        by = drawCategory(ps, g, "=== Experience ===", bx, by);
        by = drawCheckbox(ps, g, "Experience Bar", this.tempConfig.showExpBar, bx, by);
        by = drawCheckbox(ps, g, "Experience Number", this.tempConfig.showExpNumber, bx, by);
        by += 10;
        by = drawCategory(ps, g, "=== Scales & Spacing ===", bx, by);
        by = drawSliderOption(ps, g, "Heart Scale", this.tempConfig.heartScale, 0.5f, 3.0f, bx, by, leftW - 30);
        by = drawSliderOption(ps, g, "Heart Spacing", (float) this.tempConfig.heartSpacing, 5.0f, 30.0f, bx, by, leftW - 30);
        by = drawSliderOption(ps, g, "Exp Number Scale", this.tempConfig.expNumberScale, 0.5f, 3.0f, bx, by, leftW - 30);
    }

    private void renderColorsPanel(PoseStack ps, GuiGraphics g, int x, int y, int leftW, int h, int mouseX, int mouseY) {
        int bx = x + 15;
        int by = (y + 40) - this.scrollOffset;
        int sw = leftW - 50;

        by = drawCategory(ps, g, "Health", bx, by);
        drawString(ps, g, "Y Position", bx, by, -1);
        by += 12;
        renderOrangeSlider(ps, bx, by, sw, this.tempConfig.heartY, -200, 200);
        by += 20;
        drawString(ps, g, "X Position", bx, by, -1);
        by += 12;
        renderOrangeSlider(ps, bx, by, sw, this.tempConfig.heartX, -200, 200);
        by += 25;
        by = drawCategory(ps, g, "Heart Color (RGB)", bx, by);
        by = renderColorSlider(ps, g, "R", this.tempConfig.heartColorR, bx, by, sw, 16711680);
        by = renderColorSlider(ps, g, "G", this.tempConfig.heartColorG, bx, by, sw, 65280);
        by = renderColorSlider(ps, g, "B", this.tempConfig.heartColorB, bx, by, sw, 255);
        by += 15;
        by = drawCategory(ps, g, "Hotbar Color (RGB)", bx, by);
        by = renderColorSlider(ps, g, "R", this.tempConfig.hotbarColorR, bx, by, sw, 16711680);
        by = renderColorSlider(ps, g, "G", this.tempConfig.hotbarColorG, bx, by, sw, 65280);
        by = renderColorSlider(ps, g, "B", this.tempConfig.hotbarColorB, bx, by, sw, 255);
        by += 15;
        by = drawCategory(ps, g, "Experience Bar", bx, by);
        drawString(ps, g, "Y Position", bx, by, -1);
        by += 12;
        renderOrangeSlider(ps, bx, by, sw, this.tempConfig.expBarY, -200, 200);
        by += 20;
        drawString(ps, g, "X Position", bx, by, -1);
        by += 12;
        renderOrangeSlider(ps, bx, by, sw, this.tempConfig.expBarX, -200, 200);
        by += 25;
        by = drawCategory(ps, g, "Exp Bar Color (RGB)", bx, by);
        by = renderColorSlider(ps, g, "R", this.tempConfig.expBarColorR, bx, by, sw, 16711680);
        by = renderColorSlider(ps, g, "G", this.tempConfig.expBarColorG, bx, by, sw, 65280);
        by = renderColorSlider(ps, g, "B", this.tempConfig.expBarColorB, bx, by, sw, 255);
        by += 15;
        by = drawCategory(ps, g, "Experience Number", bx, by);
        drawString(ps, g, "Y Position", bx, by, -1);
        by += 12;
        renderOrangeSlider(ps, bx, by, sw, this.tempConfig.expNumberY, -200, 200);
        by += 20;
        drawString(ps, g, "X Position", bx, by, -1);
        by += 12;
        renderOrangeSlider(ps, bx, by, sw, this.tempConfig.expNumberX, -200, 200);
        by += 25;
        by = drawCategory(ps, g, "Exp Number Color (RGB)", bx, by);
        by = renderColorSlider(ps, g, "R", this.tempConfig.expNumberColorR, bx, by, sw, 16711680);
        by = renderColorSlider(ps, g, "G", this.tempConfig.expNumberColorG, bx, by, sw, 65280);
        by = renderColorSlider(ps, g, "B", this.tempConfig.expNumberColorB, bx, by, sw, 255);
    }

    private int drawCategory(PoseStack ps, GuiGraphics g, String text, int x, int y) {
        drawString(ps, g, text, x, y, -22016);
        return y + 15;
    }

    private int drawCheckbox(PoseStack ps, GuiGraphics g, String label, boolean checked, int x, int y) {
        fillRect(ps, x, y, 12, 12, -1);
        fillRect(ps, x + 1, y + 1, 10, 10, !checked ? -15066598 : -16724992);
        drawString(ps, g, label, x + 18, y + 2, -1);
        return y + 18;
    }

    private int drawSliderOption(PoseStack ps, GuiGraphics g, String label, float value, float min, float max, int x, int y, int width) {
        String valueText = String.format("%.1f", value);
        drawString(ps, g, label + ": " + valueText, x, y, -1);
        y += 12;
        fillRect(ps, x, y, width - 50, 8, -13421773);
        float t = (value - min) / (max - min);
        int pos = (int) (t * (float) (width - 50));
        fillRect(ps, (x + pos) - 2, y - 2, 4, 12, -1);
        return y + 18;
    }

    private void renderOrangeSlider(PoseStack ps, int x, int y, int width, int value, int min, int max) {
        Matrix4f m = ps.last().pose();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < width; i++) {
            float t = (float) i / (float) width;
            int r = (int) (139.0f + t * 116.0f);
            int g = (int) (60.0f + t * 50.0f);
            int b = (int) (0.0f + t * 30.0f);
            float x0 = (float) (x + i);
            float x1 = (float) ((x + i) + 1);
            float y0 = (float) y;
            float y1 = (float) (y + 15);
            bb.vertex(m, x0, y1, 0.0f).color(r, g, b, 255).endVertex();
            bb.vertex(m, x1, y1, 0.0f).color(r, g, b, 255).endVertex();
            bb.vertex(m, x1, y0, 0.0f).color(r, g, b, 255).endVertex();
            bb.vertex(m, x0, y0, 0.0f).color(r, g, b, 255).endVertex();
        }
        BufferUploader.drawWithShader(bb.end());
        RenderSystem.disableBlend();

        float t = (float) (value - min) / (float) (max - min);
        int px = x + (int) (t * (float) width);
        fillRect(ps, px - 2, y - 2, 4, 19, -1);
        drawString(ps, null, String.valueOf(value), (x + width) + 5, y + 3, -1);
    }

    private int renderColorSlider(PoseStack ps, GuiGraphics g, String label, int value, int x, int y, int width, int colorMask) {
        drawString(ps, g, label + ": " + value, x, y, -1);
        y += 12;
        Matrix4f m = ps.last().pose();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int maskR = (colorMask >> 16) & 255;
        int maskG = (colorMask >> 8) & 255;
        int maskB = colorMask & 255;
        for (int i = 0; i < width; i++) {
            float t = (float) i / (float) width;
            int r = (int) ((float) maskR * t);
            int gg = (int) ((float) maskG * t);
            int b = (int) ((float) maskB * t);
            float x0 = (float) (x + i);
            float x1 = (float) ((x + i) + 1);
            float y0 = (float) y;
            float y1 = (float) (y + 10);
            bb.vertex(m, x0, y1, 0.0f).color(r, gg, b, 255).endVertex();
            bb.vertex(m, x1, y1, 0.0f).color(r, gg, b, 255).endVertex();
            bb.vertex(m, x1, y0, 0.0f).color(r, gg, b, 255).endVertex();
            bb.vertex(m, x0, y0, 0.0f).color(r, gg, b, 255).endVertex();
        }
        BufferUploader.drawWithShader(bb.end());
        RenderSystem.disableBlend();

        float t = (float) value / 255.0f;
        int px = x + (int) (t * (float) width);
        fillRect(ps, px - 2, y - 2, 4, 14, -1);
        return y + 15;
    }

    private void renderPreview(PoseStack ps, GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        fillRect(ps, x, y + 1, w, h - 2, -10855846);
        drawString(ps, g, "Preview", x + 15, y + 15, -1);

        int cx = x + w / 2;
        int cy = (y + h / 2) + 20;
        float scale = 0.5f;
        int arm = 4;
        fillRect(ps, cx - arm, cy, arm * 2 + 1, 1, -1);
        fillRect(ps, cx, cy - arm, 1, arm * 2 + 1, -1);

        RenderSystem.enableBlend();
        if (this.tempConfig.showPlayerHealth) {
            renderHeartsPreview(ps, g, cx, cy, mouseX, mouseY, scale);
        }
        if (this.tempConfig.showHotbar) {
            renderHotbarPreview(ps, cx, cy, scale);
        }
        if (this.tempConfig.showExpBar) {
            renderExpBarPreview(ps, cx, cy, mouseX, mouseY, scale);
        }
        if (this.tempConfig.showExpNumber) {
            renderExpNumberPreview(ps, g, cx, cy, mouseX, mouseY, scale);
        }
        RenderSystem.disableBlend();
    }

    private void renderHeartsPreview(PoseStack ps, GuiGraphics g, int cx, int cy, int mouseX, int mouseY, float unused) {
        float s = this.tempConfig.heartScale;
        int spacing = this.tempConfig.heartSpacing;
        int total = 10 * spacing;
        int hx = (cx - total / 2) + this.tempConfig.heartX;
        int hy = cy + this.tempConfig.heartY;
        if (isMouseOver((double) mouseX, (double) mouseY, hx - 5, hy - 5, total + 10, (int) (10.0f * s) + 10)
                || this.dragElement.equals("hearts")) {
            fillRect(ps, hx - 5, hy - 5, total + 10, (int) (10.0f * s) + 10, 1627389696);
        }
        for (int i = 0; i < 10; i++) {
            int px = hx + i * spacing;
            ps.pushPose();
            ps.translate((float) px, (float) hy, 0.0f);
            ps.scale(s, s, 1.0f);
            float r = (float) this.tempConfig.heartColorR / 255.0f;
            float gg = (float) this.tempConfig.heartColorG / 255.0f;
            float b = (float) this.tempConfig.heartColorB / 255.0f;
            RenderSystem.setShaderColor(0.2f, 0.2f, 0.2f, 1.0f);
            g.blit(GUI_ICONS, 0, 0, 16, 0, 9, 9);
            if (i < 7) {
                RenderSystem.setShaderColor(r, gg, b, 1.0f);
                g.blit(GUI_ICONS, 0, 0, 52, 0, 9, 9);
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            ps.popPose();
        }
        drawString(ps, g, "15", (hx + total) + 5, hy + 2, -1);
    }

    private void renderHotbarPreview(PoseStack ps, int cx, int cy, float unused) {
        int size = 18;
        int total = 9 * size + 8;
        int x0 = cx - total / 2;
        int y0 = cy + 100;
        for (int i = 0; i < 9; i++) {
            int sx = x0 + i * (size + 1);
            fillRect(ps, sx, y0, size, size, -7631989);
            fillRect(ps, sx, y0, size, 1, -11184811);
            fillRect(ps, sx, (y0 + size) - 1, size, 1, -11184811);
            fillRect(ps, sx, y0, 1, size, -11184811);
            fillRect(ps, (sx + size) - 1, y0, 1, size, -11184811);
            if (i == 0) {
                fillRect(ps, sx + 1, y0 + 1, size - 2, 1, -855638017);
                fillRect(ps, sx + 1, (y0 + size) - 2, size - 2, 1, -855638017);
                fillRect(ps, sx + 1, y0 + 1, 1, size - 2, -855638017);
                fillRect(ps, (sx + size) - 2, y0 + 1, 1, size - 2, -855638017);
            }
        }
    }

    private void renderExpBarPreview(PoseStack ps, int cx, int cy, int mouseX, int mouseY, float unused) {
        int bx = cx + this.tempConfig.expBarX;
        int by = cy + this.tempConfig.expBarY;
        int bw = 182;
        if (isMouseOver((double) mouseX, (double) mouseY, (bx - bw / 2) - 5, by - 5, bw + 10, 15)
                || this.dragElement.equals("expbar")) {
            fillRect(ps, (bx - bw / 2) - 5, by - 5, bw + 10, 15, 1627389696);
        }
        fillRect(ps, bx - bw / 2, by, bw, 5, -16777216);

        Matrix4f m = ps.last().pose();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 91; i++) {
            float t = (float) i / 91.0f;
            int r = (int) ((float) this.tempConfig.expBarColorR * t);
            int gg = (int) ((float) this.tempConfig.expBarColorG * t);
            int b = (int) ((float) this.tempConfig.expBarColorB * t);
            float x0 = (float) ((bx - bw / 2) + i * 2);
            float x1 = x0 + 2.0f;
            float y0 = (float) by;
            float y1 = (float) (by + 5);
            bb.vertex(m, x0, y1, 0.0f).color(r, gg, b, 255).endVertex();
            bb.vertex(m, x1, y1, 0.0f).color(r, gg, b, 255).endVertex();
            bb.vertex(m, x1, y0, 0.0f).color(r, gg, b, 255).endVertex();
            bb.vertex(m, x0, y0, 0.0f).color(r, gg, b, 255).endVertex();
        }
        BufferUploader.drawWithShader(bb.end());
    }

    private void renderExpNumberPreview(PoseStack ps, GuiGraphics g, int cx, int cy, int mouseX, int mouseY, float unused) {
        int nx = cx + this.tempConfig.expNumberX;
        int ny = cy + this.tempConfig.expNumberY;
        int color = -16777216 | (this.tempConfig.expNumberColorR << 16) | (this.tempConfig.expNumberColorG << 8) | this.tempConfig.expNumberColorB;
        String text = "30";
        float sc = this.tempConfig.expNumberScale;
        int tw = (int) ((float) this.font.width(text) * sc);
        int th = (int) (9.0f * sc);
        if (isMouseOver((double) mouseX, (double) mouseY, (nx - tw / 2) - 5, ny - 5, tw + 10, th + 10)
                || this.dragElement.equals("expnumber")) {
            fillRect(ps, (nx - tw / 2) - 5, ny - 5, tw + 10, th + 10, 1627389696);
        }
        ps.pushPose();
        ps.translate((float) nx, (float) ny, 0.0f);
        ps.scale(sc, sc, 1.0f);
        int ox = (-this.font.width(text)) / 2;
        g.drawString(this.font, text, ox, 0, color, false);
        ps.popPose();
    }

    private void renderButtons(PoseStack ps, GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        int by = (y + h) - 35;
        int saveW = 120;
        int bh = 20;
        int gap = 10;
        int resetW = 80;
        int closeW = 80;
        int total = saveW + resetW + closeW + gap * 2;
        int sx = x + (w - total) / 2;

        boolean hover = isMouseOver((double) mouseX, (double) mouseY, sx, by, saveW, bh);
        fillRect(ps, sx, by, saveW, bh, !hover ? -13158601 : -12105913);
        drawCenteredString(ps, g, "Save", sx + saveW / 2, by + 6, -1);

        int rx = (sx + saveW) + gap;
        boolean hover2 = isMouseOver((double) mouseX, (double) mouseY, rx, by, resetW, bh);
        fillRect(ps, rx, by, resetW, bh, !hover2 ? -13158601 : -12105913);
        drawCenteredString(ps, g, "Reset", rx + resetW / 2, by + 6, -1);

        int cx = (rx + resetW) + gap;
        boolean hover3 = isMouseOver((double) mouseX, (double) mouseY, cx, by, closeW, bh);
        fillRect(ps, cx, by, closeW, bh, !hover3 ? -13158601 : -12105913);
        drawCenteredString(ps, g, "Close", cx + closeW / 2, by + 6, -1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        int w = Math.min(this.width - 40, 900);
        int h = Math.min(this.height - 40, 550);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        int leftW = (int) ((float) w * 0.35f);

        // Pestañas
        if (mouseY >= (double) (y + 1) && mouseY <= (double) (y + 31)) {
            if (mouseX >= (double) (x + 1) && mouseX < (double) (x + 101)) {
                this.currentTab = 0;
                this.scrollOffset = 0;
                return true;
            }
            if (mouseX >= (double) (x + 101) && mouseX < (double) (x + 201)) {
                this.currentTab = 1;
                this.scrollOffset = 0;
                return true;
            }
        }

        // Botones Save / Reset / Close
        int by = (y + h) - 35;
        int saveW = 120;
        int resetW = 80;
        int closeW = 80;
        int gap = 10;
        int total = saveW + resetW + closeW + gap * 2;
        int sx = x + (w - total) / 2;
        if (mouseY >= (double) by && mouseY <= (double) (by + 20)) {
            if (mouseX >= (double) sx && mouseX < (double) (sx + saveW)) {
                HudConfig.getInstance().copyFrom(this.tempConfig);
                HudConfig.getInstance().save();
                ClientNetwork.sendToServer(new HudConfigPacket(this.tempConfig));
                this.minecraft.setScreen(null);
                return true;
            }
            int rx = (sx + saveW) + gap;
            if (mouseX >= (double) rx && mouseX < (double) (rx + resetW)) {
                this.tempConfig.resetToDefaults();
                this.scrollOffset = 0;
                return true;
            }
            int cx = (rx + resetW) + gap;
            if (mouseX >= (double) cx && mouseX < (double) (cx + closeW)) {
                this.minecraft.setScreen(null);
                return true;
            }
        }

        // Panel izquierdo
        if (mouseX >= (double) (x + 1) && mouseX < (double) ((x + leftW) - 1)
                && mouseY >= (double) (y + 31) && mouseY < (double) ((y + h) - 51)) {
            if (this.currentTab == 0) {
                handleToolsClick(mouseX, mouseY, x, y, leftW);
            } else if (this.currentTab == 1) {
                handleColorsClick(mouseX, mouseY, x, y, leftW);
            }
        }

        // Panel de preview
        int px = x + leftW;
        if (mouseX >= (double) px) {
            handlePreviewClick(mouseX, mouseY, px, y, w - leftW, h);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleToolsClick(double mouseX, double mouseY, int x, int y, int leftW) {
        int bx = x + 15;
        int by = (y + 40) - this.scrollOffset;
        int sliderW = leftW - 80;

        by += 15;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showPlayerHealth = !this.tempConfig.showPlayerHealth;
            return;
        }
        by += 18;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showVanillaPlayerHealth = !this.tempConfig.showVanillaPlayerHealth;
            return;
        }
        by += 28;
        by += 15;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showHotbar = !this.tempConfig.showHotbar;
            return;
        }
        by += 18;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showFood = !this.tempConfig.showFood;
            return;
        }
        by += 18;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showArmor = !this.tempConfig.showArmor;
            return;
        }
        by += 18;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showAir = !this.tempConfig.showAir;
            return;
        }
        by += 18;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showMountHealth = !this.tempConfig.showMountHealth;
            return;
        }
        by += 28;
        by += 15;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showExpBar = !this.tempConfig.showExpBar;
            return;
        }
        by += 18;
        if (isMouseOver(mouseX, mouseY, bx, by, 200, 12)) {
            this.tempConfig.showExpNumber = !this.tempConfig.showExpNumber;
            return;
        }
        by += 28;
        by += 27;
        if (isMouseOver(mouseX, mouseY, bx, by, sliderW, 8)) {
            this.draggingSlider = true;
            this.draggingSliderIndex = 100;
            updateScaleSlider(mouseX, bx, sliderW, 100);
            return;
        }
        by += 30;
        if (isMouseOver(mouseX, mouseY, bx, by, sliderW, 8)) {
            this.draggingSlider = true;
            this.draggingSliderIndex = 101;
            updateScaleSlider(mouseX, bx, sliderW, 101);
            return;
        }
        by += 30;
        if (isMouseOver(mouseX, mouseY, bx, by, sliderW, 8)) {
            this.draggingSlider = true;
            this.draggingSliderIndex = 102;
            updateScaleSlider(mouseX, bx, sliderW, 102);
        }
    }

    private void handleColorsClick(double mouseX, double mouseY, int x, int y, int leftW) {
        int bx = x + 15;
        int sliderW = leftW - 50;
        for (int i = 0; i <= 5; i++) {
            int sy = calculateSliderY(i, y);
            if (isMouseOver(mouseX, mouseY, bx, sy, sliderW, 15)) {
                this.draggingSlider = true;
                this.draggingSliderIndex = i;
                updateSliderValue(mouseX, bx, sliderW, i);
                return;
            }
        }
        for (int i = 10; i <= 42; i++) {
            int sy = calculateColorSliderY(i, y);
            if (sy > 0 && isMouseOver(mouseX, mouseY, bx, sy, sliderW, 10)) {
                this.draggingSlider = true;
                this.draggingSliderIndex = i;
                updateColorSlider(mouseX, bx, sliderW, i);
                return;
            }
        }
    }

    private void handlePreviewClick(double mouseX, double mouseY, int px, int py, int pw, int ph) {
        int cx = px + pw / 2;
        int cy = (py + ph / 2) + 20;

        if (this.tempConfig.showPlayerHealth) {
            int spacing = this.tempConfig.heartSpacing;
            int total = 10 * spacing;
            int hx = (cx - total / 2) + this.tempConfig.heartX;
            int hy = cy + this.tempConfig.heartY;
            float s = this.tempConfig.heartScale;
            if (isMouseOver(mouseX, mouseY, hx - 5, hy - 5, total + 10, (int) (10.0f * s) + 10)) {
                this.isDraggingElement = true;
                this.dragElement = "hearts";
                this.dragOffsetX = (int) (mouseX - (double) hx);
                this.dragOffsetY = (int) (mouseY - (double) hy);
                return;
            }
        }
        if (this.tempConfig.showExpBar) {
            int bx = cx + this.tempConfig.expBarX;
            int by = cy + this.tempConfig.expBarY;
            int bw = 182;
            if (isMouseOver(mouseX, mouseY, (bx - bw / 2) - 5, by - 5, bw + 10, 15)) {
                this.isDraggingElement = true;
                this.dragElement = "expbar";
                this.dragOffsetX = (int) (mouseX - (double) bx);
                this.dragOffsetY = (int) (mouseY - (double) by);
                return;
            }
        }
        if (this.tempConfig.showExpNumber) {
            int nx = cx + this.tempConfig.expNumberX;
            int ny = cy + this.tempConfig.expNumberY;
            float sc = this.tempConfig.expNumberScale;
            int tw = (int) ((float) this.font.width("30") * sc);
            int th = (int) (9.0f * sc);
            if (isMouseOver(mouseX, mouseY, (nx - tw / 2) - 5, ny - 5, tw + 10, th + 10)) {
                this.isDraggingElement = true;
                this.dragElement = "expnumber";
                this.dragOffsetX = (int) (mouseX - (double) nx);
                this.dragOffsetY = (int) (mouseY - (double) ny);
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingSlider = false;
        this.draggingSliderIndex = -1;
        this.isDraggingElement = false;
        this.dragElement = "";
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) {
            return false;
        }
        if (this.draggingSlider) {
            int w = Math.min(this.width - 40, 900);
            int x = (this.width - w) / 2;
            int leftW = (int) ((float) w * 0.35f);
            int bx = x + 15;
            int sliderW = leftW - 50;
            if (this.draggingSliderIndex >= 100) {
                updateScaleSlider(mouseX, bx, leftW - 80, this.draggingSliderIndex);
            } else if (this.draggingSliderIndex <= 5) {
                updateSliderValue(mouseX, bx, sliderW, this.draggingSliderIndex);
            } else {
                updateColorSlider(mouseX, bx, sliderW, this.draggingSliderIndex);
            }
            return true;
        }
        if (this.isDraggingElement) {
            int w = Math.min(this.width - 40, 900);
            int h = Math.min(this.height - 40, 550);
            int x = (this.width - w) / 2;
            int y = (this.height - h) / 2;
            int leftW = (int) ((float) w * 0.35f);
            int px = x + leftW;
            int cx = px + (w - leftW) / 2;
            int cy = (y + h / 2) + 20;
            if (this.dragElement.equals("hearts")) {
                this.tempConfig.heartX = ((int) mouseX - this.dragOffsetX) - cx;
                this.tempConfig.heartY = ((int) mouseY - this.dragOffsetY) - cy;
            } else if (this.dragElement.equals("expbar")) {
                this.tempConfig.expBarX = ((int) mouseX - this.dragOffsetX) - cx;
                this.tempConfig.expBarY = ((int) mouseY - this.dragOffsetY) - cy;
            } else if (this.dragElement.equals("expnumber")) {
                this.tempConfig.expNumberX = ((int) mouseX - this.dragOffsetX) - cx;
                this.tempConfig.expNumberY = ((int) mouseY - this.dragOffsetY) - cy;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int w = Math.min(this.width - 40, 900);
        int x = (this.width - w) / 2;
        int leftW = (int) ((float) w * 0.35f);
        if (mouseX >= (double) (x + 1) && mouseX < (double) ((x + leftW) - 1)) {
            int newScroll = this.scrollOffset - (int) (delta * 20.0);
            this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, newScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateSliderValue(double mouseX, int sliderX, int sliderW, int index) {
        float t = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - (double) sliderX) / (float) sliderW));
        int value = (int) ((t * 400.0f) - 200.0f);
        switch (index) {
            case 0 -> this.tempConfig.heartY = value;
            case 1 -> this.tempConfig.heartX = value;
            case 2 -> this.tempConfig.expBarY = value;
            case 3 -> this.tempConfig.expBarX = value;
            case 4 -> this.tempConfig.expNumberY = value;
            case 5 -> this.tempConfig.expNumberX = value;
            default -> { }
        }
    }

    private void updateColorSlider(double mouseX, int sliderX, int sliderW, int index) {
        float t = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - (double) sliderX) / (float) sliderW));
        int value = (int) (t * 255.0f);
        switch (index) {
            case 10 -> this.tempConfig.heartColorR = value;
            case 11 -> this.tempConfig.heartColorG = value;
            case 12 -> this.tempConfig.heartColorB = value;
            case 20 -> this.tempConfig.hotbarColorR = value;
            case 21 -> this.tempConfig.hotbarColorG = value;
            case 22 -> this.tempConfig.hotbarColorB = value;
            case 30 -> this.tempConfig.expBarColorR = value;
            case 31 -> this.tempConfig.expBarColorG = value;
            case 32 -> this.tempConfig.expBarColorB = value;
            case 40 -> this.tempConfig.expNumberColorR = value;
            case 41 -> this.tempConfig.expNumberColorG = value;
            case 42 -> this.tempConfig.expNumberColorB = value;
            default -> { }
        }
    }

    private void updateScaleSlider(double mouseX, int sliderX, int sliderW, int index) {
        float t = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - (double) sliderX) / (float) sliderW));
        switch (index) {
            case 100 -> this.tempConfig.heartScale = 0.5f + t * 2.5f;
            case 101 -> this.tempConfig.heartSpacing = 5 + (int) (t * 25.0f);
            case 102 -> this.tempConfig.expNumberScale = 0.5f + t * 2.5f;
            default -> { }
        }
    }

    private int calculateSliderY(int index, int panelY) {
        int base = (panelY + 40) - this.scrollOffset;
        return switch (index) {
            case 0 -> base + 27;
            case 1 -> base + 59;
            case 2 -> base + 318;
            case 3 -> base + 350;
            case 4 -> base + 498;
            case 5 -> base + 530;
            default -> -1;
        };
    }

    private int calculateColorSliderY(int index, int panelY) {
        int base = (panelY + 40) - this.scrollOffset;
        return switch (index) {
            case 10 -> base + 111;
            case 11 -> base + 138;
            case 12 -> base + 165;
            case 20 -> base + 222;
            case 21 -> base + 249;
            case 22 -> base + 276;
            case 30 -> base + 402;
            case 31 -> base + 429;
            case 32 -> base + 456;
            case 40 -> base + 582;
            case 41 -> base + 609;
            case 42 -> base + 636;
            default -> -1;
        };
    }

    private boolean isMouseOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= (double) x && mx < (double) (x + w) && my >= (double) y && my < (double) (y + h);
    }

    private void fillRect(PoseStack ps, int x, int y, int w, int h, int color) {
        Matrix4f m = ps.last().pose();
        float a = (float) ((color >> 24) & 255) / 255.0f;
        float r = (float) ((color >> 16) & 255) / 255.0f;
        float g = (float) ((color >> 8) & 255) / 255.0f;
        float b = (float) (color & 255) / 255.0f;
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bb.vertex(m, (float) x, (float) (y + h), 0.0f).color(r, g, b, a).endVertex();
        bb.vertex(m, (float) (x + w), (float) (y + h), 0.0f).color(r, g, b, a).endVertex();
        bb.vertex(m, (float) (x + w), (float) y, 0.0f).color(r, g, b, a).endVertex();
        bb.vertex(m, (float) x, (float) y, 0.0f).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(bb.end());
        RenderSystem.disableBlend();
    }

    private void drawString(PoseStack ps, GuiGraphics g, String text, int x, int y, int color) {
        if (g != null) {
            g.drawString(this.font, text, x, y, color, false);
        } else {
            this.font.drawInBatch(text, (float) x, (float) y, color, false, ps.last().pose(),
                    Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }

    private void drawCenteredString(PoseStack ps, GuiGraphics g, String text, int cx, int y, int color) {
        int tw = this.font.width(text);
        drawString(ps, g, text, cx - tw / 2, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
