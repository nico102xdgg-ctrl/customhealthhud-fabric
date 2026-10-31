package net.poh.customhealthhud.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.poh.customhealthhud.config.HudConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric no tiene un evento para cancelar elementos del HUD vanilla (en Forge era
 * RenderGuiOverlayEvent.Pre + setCanceled), así que se hace con mixins.
 */
@Mixin(Gui.class)
public class GuiMixin {

    // Hotbar: se oculta si showHotbar = false
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void customhealthhud$hotbar(CallbackInfo ci) {
        if (!HudConfig.getInstance().showHotbar) {
            ci.cancel();
        }
    }

    // Barra + número de experiencia vanilla: siempre se ocultan (el mod dibuja las suyas)
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void customhealthhud$expBar(CallbackInfo ci) {
        ci.cancel();
    }

    // Vida de la montura
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true, require = 0)
    private void customhealthhud$mountHealth(CallbackInfo ci) {
        if (!HudConfig.getInstance().showMountHealth) {
            ci.cancel();
        }
    }

    // Corazones vanilla: solo se ven si showVanillaPlayerHealth = true
    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true, require = 0)
    private void customhealthhud$vanillaHearts(CallbackInfo ci) {
        if (!HudConfig.getInstance().showVanillaPlayerHealth) {
            ci.cancel();
        }
    }

    // Armadura / comida / aire: en vanilla se dibujan dentro de renderPlayerHealth,
    // así que se filtran por la fila de la textura (v) de cada icono.
    @Redirect(method = "renderPlayerHealth",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0)
    private void customhealthhud$filterIcons(GuiGraphics graphics, ResourceLocation texture,
                                             int x, int y, int u, int v, int w, int h) {
        HudConfig cfg = HudConfig.getInstance();
        if (v == 9 && (u == 16 || u == 25 || u == 34) && !cfg.showArmor) {
            return;
        }
        if (v == 18 && !cfg.showAir) {
            return;
        }
        if (v == 27 && !cfg.showFood) {
            return;
        }
        graphics.blit(texture, x, y, u, v, w, h);
    }
}
