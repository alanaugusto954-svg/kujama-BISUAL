package com.kujama09.hands;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Resplandor de esquina: halos dorados suaves donde quedan las manos. Es un efecto de pantalla aproximado: no
 * sigue la silueta exacta de la mano ni del objeto.
 */
public final class Glow {
    private Glow() {
    }

    public static void render(DrawContext c) {
        MinecraftClient mc = MinecraftClient.getInstance();
        HandsConfig cfg = HandsConfig.get();
        if (!cfg.glow || cfg.glowIntensity <= 0 || mc.player == null || mc.options.hudHidden
                || !mc.options.getPerspective().isFirstPerson()) {
            return;
        }
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        float spread = cfg.farHands ? 0.30f + cfg.offsetX / 100f * 0.35f : 0.22f;
        float s = cfg.farHands ? cfg.scale / 100f : 1f;
        int rx = Math.round(w * 0.14f * (0.6f + s * 0.5f));
        int ry = Math.round(h * 0.30f * (0.6f + s * 0.5f));
        int cy = h - Math.round(h * 0.04f);
        int rgb = cfg.glowRgb();
        float strength = cfg.glowIntensity / 100f;
        ellipse(c, Math.round(w * (0.5f + spread)), cy, rx, ry, rgb, strength);
        ellipse(c, Math.round(w * (0.5f - spread)), cy, rx, ry, rgb, strength);
    }

    /** Elipse con degradado radial hecho con anillos concéntricos muy translúcidos. */
    private static void ellipse(DrawContext c, int cx, int cy, int rx, int ry, int rgb, float strength) {
        int rings = 7;
        int alpha = Math.max(1, Math.round(22 * strength));
        for (int i = 0; i < rings; i++) {
            float k = 1f - i / (float) rings;
            int erx = Math.round(rx * k);
            int ery = Math.round(ry * k);
            int color = (alpha << 24) | rgb;
            for (int dy = -ery; dy <= ery; dy += 2) {
                double t = dy / (double) ery;
                int half = (int) Math.round(erx * Math.sqrt(1.0 - t * t));
                c.fill(cx - half, cy + dy, cx + half, cy + dy + 2, color);
            }
        }
    }
}
