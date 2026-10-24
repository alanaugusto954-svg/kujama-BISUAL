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
        int tr = cfg.sky.r;
        int tg = cfg.sky.g;
        int tb = cfg.sky.b;
        if (cfg.sky.realistic) {
            int max = Math.max((color >> 16) & 0xFF, Math.max((color >> 8) & 0xFF, color & 0xFF));
            float light = Math.max(0f, Math.min(1f, max / 255f));
            tr = Math.round(tr * light);
            tg = Math.round(tg * light);
            tb = Math.round(tb * light);
        }
        int r = blend((color >> 16) & 0xFF, tr, s);
        int g = blend((color >> 8) & 0xFF, tg, s);
        int b = blend(color & 0xFF, tb, s);
        return a | (r << 16) | (g << 8) | b;
    }

    private static int blend(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
