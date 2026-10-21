package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.joml.Matrix3x2fStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** HUD minimalista: FPS, hora, canción de Spotify, coordenadas, teclas y CPS. Sin paneles pesados. */
public final class HudRenderer {
    private static final int TEXT = 0xE6FFFFFF;
    private static final int DIM = 0x99C8CCD6;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private HudRenderer() {
    }

    public static void render(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        VisualsConfig cfg = VisualsConfig.get();
        if (mc.player == null || mc.world == null) {
            return;
        }

        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        // Cielo: líneas animadas (tema void) o degradado de color (otros temas).
        if (cfg.sky == SkyTheme.VOID) {
            if (cfg.skyLines) {
                SkyOverlay.render(context, mc, width, height, cfg.skyStrength);
            }
        } else if (cfg.sky != SkyTheme.OFF && cfg.skyStrength > 0) {
            int alpha = Math.round(0x70 * (cfg.skyStrength / 100f));
            context.fillGradient(0, 0, width, Math.round(height * 0.55f), cfg.sky.argb(alpha), cfg.sky.argb(0));
        }

        CpsCounter.poll(mc.getWindow().getHandle(), mc.currentScreen == null);

        if (!cfg.hud) {
            return;
        }
        TextRenderer font = mc.textRenderer;
        float scale = cfg.hudScale * 0.85f;
        int vw = Math.round(width / scale);
        int vh = Math.round(height / scale);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scale, scale);

        if (cfg.topBar) {
            String fps = "FPS: " + mc.getCurrentFps();
            context.drawText(font, fps, (vw - font.getWidth(fps)) / 2, 4, TEXT, true);
            String time = LocalTime.now().format(CLOCK);
            context.drawText(font, time, vw - font.getWidth(time) - 6, 6, TEXT, true);
        }

        if (cfg.music && SpotifyReader.isPlaying()) {
            drawMusic(context, font, vw / 2 - 80, cfg.topBar ? 17 : 6);
        }

        if (cfg.coords) {
            String xyz = String.format(Locale.ROOT, "%.1f, %.1f, %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            context.drawText(font, xyz, 6, 6, TEXT, true);
            context.drawText(font, "Visuals by kujama09", 6, 17, DIM, true);
        }

        if (cfg.keystrokes || cfg.cps) {
            drawKeysAndCps(context, font, mc, cfg, 6, vh);
        }

        matrices.popMatrix();
    }

    /** Tarjeta con la canción: icono, título, artista y unas barras animadas. */
    private static void drawMusic(DrawContext context, TextRenderer font, int x, int y) {
        int w = 160;
        int h = 28;
        context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x24FFFFFF);
        context.fill(x, y, x + w, y + h, 0xA0101018);

        // Icono
        context.fillGradient(x + 3, y + 3, x + 25, y + 25, 0xFF3B2A6B, 0xFF14101F);
        context.drawText(font, "♪", x + 11, y + 10, TEXT, false);

        String title = font.trimToWidth(SpotifyReader.title(), w - 62);
        String artist = font.trimToWidth(SpotifyReader.artist(), w - 62);
        context.drawText(font, title, x + 30, y + 5, TEXT, false);
        context.drawText(font, artist, x + 30, y + 16, DIM, false);

        // Barras animadas (indican que está sonando)
        float t = System.nanoTime() / 1_000_000_000f;
        for (int i = 0; i < 4; i++) {
            int bh = 3 + Math.round(8 * (0.5f + 0.5f * (float) Math.sin(t * (4f + i * 1.7f) + i)));
            int bx = x + w - 22 + i * 5;
            context.fill(bx, y + 20 - bh, bx + 3, y + 20, 0xCC9B7BFF);
        }
    }

    private static void drawKeysAndCps(DrawContext context, TextRenderer font, MinecraftClient mc,
                                       VisualsConfig cfg, int x, int vh) {
        int k = 16;
        int gap = 2;
        int blockH = k + gap + k + gap + 14;
        int y0 = vh - 6 - blockH;

        if (cfg.cps) {
            context.drawText(font, "CPS: " + CpsCounter.left() + " | " + CpsCounter.right(),
                    x, y0 - 12, TEXT, true);
        }
        if (!cfg.keystrokes) {
            return;
        }
        drawKey(context, font, "W", mc.options.forwardKey, x + k + gap, y0, k, k);
        drawKey(context, font, "A", mc.options.leftKey, x, y0 + k + gap, k, k);
        drawKey(context, font, "S", mc.options.backKey, x + k + gap, y0 + k + gap, k, k);
        drawKey(context, font, "D", mc.options.rightKey, x + 2 * (k + gap), y0 + k + gap, k, k);
        int by = y0 + 2 * (k + gap);
        int bw = (3 * k + 2 * gap - gap) / 2;
        drawKey(context, font, "LMB", mc.options.attackKey, x, by, bw, 14);
        drawKey(context, font, "RMB", mc.options.useKey, x + bw + gap, by, bw, 14);
    }

    private static void drawKey(DrawContext context, TextRenderer font, String label, KeyBinding key,
                                int x, int y, int w, int h) {
        boolean down = key.isPressed();
        context.fill(x, y, x + w, y + h, down ? 0x66FFFFFF : 0x30FFFFFF);
        context.fill(x + 1, y + 1, x + w - 1, y + h - 1, down ? 0xDDFFFFFF : 0x99101018);
        int color = down ? 0xFF101018 : TEXT;
        context.drawText(font, label, x + (w - font.getWidth(label)) / 2, y + (h - 8) / 2, color, false);
    }
}
