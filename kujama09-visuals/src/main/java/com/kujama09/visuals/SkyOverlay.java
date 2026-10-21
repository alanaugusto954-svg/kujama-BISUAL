package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Líneas blancas animadas sobre el cielo. Minecraft no permite reemplazar el domo del cielo del Overworld con
 * una textura, así que las líneas se dibujan como una capa 2D que sigue la cámara (parallax según hacia dónde
 * mirás) y se recorta en el horizonte. Funciona mejor en terreno abierto: sobre colinas o árboles altos las
 * líneas se superponen un poco.
 */
public final class SkyOverlay {
    private static final Identifier TEXTURE = Identifier.of("kujama09_visuals", "textures/gui/sky_lines.png");
    private static final int TEX_W = 1024;
    private static final int TEX_H = 512;

    private SkyOverlay() {
    }

    public static void render(DrawContext context, MinecraftClient mc, int width, int height, int strengthPercent) {
        ClientWorld world = mc.world;
        ClientPlayerEntity player = mc.player;
        if (world == null || player == null || strengthPercent <= 0) {
            return;
        }
        if (!world.getRegistryKey().equals(World.OVERWORLD) || !world.isSkyVisible(player.getBlockPos())) {
            return;
        }

        double fov = Math.max(30.0, mc.options.getFov().getValue());
        double halfFov = Math.toRadians(fov / 2.0);
        double pitchRad = Math.toRadians(player.getPitch());
        // Posición vertical del horizonte en pantalla (mirar hacia abajo sube el horizonte).
        double horizon = height / 2.0 - Math.tan(pitchRad) * (height / 2.0) / Math.tan(halfFov);
        int horizonY = (int) Math.max(0, Math.min(height, horizon));
        if (horizonY <= 0) {
            return;
        }

        float pixelsPerDegree = (float) (height / fov);
        int tileH = Math.round(height * 1.5f);
        int tileW = tileH * (TEX_W / TEX_H);
        float drift = (System.nanoTime() / 1_000_000_000f) * 5f;
        float offset = (-player.getYaw() * pixelsPerDegree + drift) % tileW;
        if (offset > 0) {
            offset -= tileW;
        }

        int alpha = Math.round(255 * 0.9f * (strengthPercent / 100f));
        int color = (alpha << 24) | 0x00FFFFFF;
        int top = horizonY - tileH + Math.round(height * 0.15f);

        context.enableScissor(0, 0, width, horizonY);
        for (float x = offset; x < width; x += tileW) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, Math.round(x), top, 0f, 0f,
                    tileW + 1, tileH, TEX_W, TEX_H, TEX_W, TEX_H, color);
        }
        context.disableScissor();
    }
}
