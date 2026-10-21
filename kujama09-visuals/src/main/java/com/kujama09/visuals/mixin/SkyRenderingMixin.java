package com.kujama09.visuals.mixin;

import com.kujama09.visuals.SkyTheme;
import com.kujama09.visuals.VisualsConfig;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mezcla el color del domo del cielo con el tema elegido (con "void" queda negro). Mixin opcional: si Minecraft
 * cambia este método, el mod sigue funcionando y solo se pierde este efecto.
 */
@Mixin(SkyRendering.class)
public abstract class SkyRenderingMixin {

    @ModifyVariable(method = "renderTopSky", at = @At("HEAD"), argsOnly = true)
    private int kujama09_visuals$tintTopSky(int color) {
        VisualsConfig cfg = VisualsConfig.get();
        if (cfg.sky == SkyTheme.OFF || cfg.skyStrength <= 0) {
            return color;
        }
        float s = cfg.skyStrength / 100f;
        int a = color & 0xFF000000;
        int r = blend((color >> 16) & 0xFF, cfg.sky.r, s);
        int g = blend((color >> 8) & 0xFF, cfg.sky.g, s);
        int b = blend(color & 0xFF, cfg.sky.b, s);
        return a | (r << 16) | (g << 8) | b;
    }

    private static int blend(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
