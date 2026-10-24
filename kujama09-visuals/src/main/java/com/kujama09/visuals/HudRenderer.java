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
        } else if (cfg.sky != SkyTheme.OFF && !cfg.sky.realistic && cfg.skyStrength > 0) {
            int alpha = Math.round(0x70 * (cfg.skyStrength / 100f));
            context.fillGradient(0, 0, width, Math.round(height * 0.55f), cfg.sky.argb(alpha), cfg.sky.argb(0));
        }

        Thunder.render(context, mc, width, height, cfg);
        SkyLogo.render(context, mc, width, height, cfg);

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

        Theme theme = Theme.current();
        if (cfg.topBar) {
            drawTopBar(context, font, mc, theme, vw);
        }

        if (cfg.coords) {
            String xyz = String.format(Locale.ROOT, "%.1f, %.1f, %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            String label = "XYZ";
            int w = font.getWidth(label) + font.getWidth(xyz) + 22;
            Ui.panel(context, 6, 6, 6 + w, 22, 5, 0xB0101016, Ui.alpha(theme.accent, 0.55f));
            context.drawText(font, label, 12, 11, theme.accent, true);
            context.drawText(font, xyz, 12 + font.getWidth(label) + 8, 11, TEXT, true);
        }

        drawSidePanels(context, font, cfg, theme, vw);

        if (cfg.music && SpotifyReader.isPlaying()) {
            drawMusic(context, font, theme, vw / 2 - 80, cfg.topBar ? 30 : 6);
        }

        if (cfg.keystrokes || cfg.cps) {
            drawKeysAndCps(context, font, mc, cfg, 6, vh);
        }

        matrices.popMatrix();
    }

    private static void drawTopBar(DrawContext context, TextRenderer font, MinecraftClient mc, Theme theme, int vw) {
        String where;
        if (mc.isInSingleplayer()) {
            where = "Singleplayer";
        } else if (mc.getCurrentServerEntry() != null) {
            where = mc.getCurrentServerEntry().address;
        } else {
            where = "Multiplayer";
        }
        where = font.trimToWidth(where, 120);
        String name = "kujama09";
        String fps = mc.getCurrentFps() + " FPS";
        String time = LocalTime.now().format(CLOCK);
        int gap = 10;
        int w = font.getWidth(name) + font.getWidth(where) + font.getWidth(fps) + font.getWidth(time) + gap * 5;
        int x = (vw - w) / 2;
        Ui.glow(context, x, 4, x + w, 20, 6, Ui.alpha(theme.accent, 0.6f));
        Ui.panel(context, x, 4, x + w, 20, 6, 0xC0101016, Ui.alpha(theme.accent, 0.6f));
        int tx = x + gap;
        context.drawText(font, name, tx, 9, theme.accent, true);
        tx += font.getWidth(name) + gap;
        context.drawText(font, where, tx, 9, TEXT, true);
        tx += font.getWidth(where) + gap;
        context.drawText(font, fps, tx, 9, TEXT, true);
        tx += font.getWidth(fps) + gap;
        context.drawText(font, time, tx, 9, DIM, true);
    }

    private static void drawSidePanels(DrawContext context, TextRenderer font, VisualsConfig cfg, Theme theme, int vw) {
        int w = 96;
        int x = vw - w - 6;
        int y = 6;
        if (cfg.modulesPanel) {
            java.util.List<String> on = new java.util.ArrayList<>();
            if (cfg.fullbright) on.add("Fullbright");
            if (cfg.hands) on.add("Hands");
            if (cfg.thunder) on.add("Thunder");
            if (cfg.skyLogo) on.add("Sky Logo");
            if (cfg.noRain) on.add("No Rain");
            if (cfg.sky != SkyTheme.OFF) on.add("Sky: " + Ui.capitalize(cfg.sky.id));
            if (cfg.particles) on.add("Particles");
            if (cfg.music) on.add("Spotify");
            if (cfg.keystrokes) on.add("Keystrokes");
            if (cfg.cps) on.add("CPS");
            int h = 18 + Math.max(1, on.size()) * 11 + 3;
            Ui.panel(context, x, y, x + w, y + h, 5, 0xB0101016, Ui.alpha(theme.accent, 0.45f));
            context.drawText(font, "Modules", x + 8, y + 6, theme.accent, true);
            context.fill(x + 6, y + 16, x + w - 6, y + 17, Ui.alpha(theme.accent, 0.35f));
            int ly = y + 21;
            if (on.isEmpty()) {
                context.drawText(font, "none", x + 8, ly, DIM, false);
            }
            for (String m : on) {
                context.fill(x + 8, ly + 3, x + 11, ly + 6, theme.accent);
                context.drawText(font, m, x + 15, ly, TEXT, false);
                ly += 11;
            }
            y += h + 5;
        }
        if (cfg.keybindsPanel) {
            int h = 30;
            Ui.panel(context, x, y, x + w, y + h, 5, 0xB0101016, Ui.alpha(theme.accent, 0.45f));
            context.drawText(font, "KeyBinds", x + 8, y + 6, theme.accent, true);
            context.fill(x + 6, y + 16, x + w - 6, y + 17, Ui.alpha(theme.accent, 0.35f));
            context.drawText(font, "Menu", x + 8, y + 20, TEXT, false);
            String key = "R-Shift";
            context.drawText(font, key, x + w - font.getWidth(key) - 8, y + 20, DIM, false);
        }
    }

    /** Tarjeta con la canción: icono, título, artista y unas barras animadas. */
    private static void drawMusic(DrawContext context, TextRenderer font, Theme theme, int x, int y) {
        int w = 160;
        int h = 28;
        context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x24FFFFFF);
        context.fill(x, y, x + w, y + h, 0xA0101018);

        // Icono
        net.minecraft.util.Identifier cover = SpotifyCover.get();
        if (cover != null) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, cover, x + 3, y + 3,
                    0f, 0f, 22, 22, 64, 64, 64, 64, 0xFFFFFFFF);
        } else {
            context.fillGradient(x + 3, y + 3, x + 25, y + 25, theme.accent2, 0xFF14101F);
            context.drawText(font, "♪", x + 11, y + 10, TEXT, false);
        }

        String title = font.trimToWidth(SpotifyReader.title(), w - 62);
        String artist = font.trimToWidth(SpotifyReader.artist(), w - 62);
        context.drawText(font, title, x + 30, y + 5, TEXT, false);
        context.drawText(font, artist, x + 30, y + 16, DIM, false);

        // Barras animadas (indican que está sonando)
        float t = System.nanoTime() / 1_000_000_000f;
        for (int i = 0; i < 4; i++) {
            int bh = 3 + Math.round(8 * (0.5f + 0.5f * (float) Math.sin(t * (4f + i * 1.7f) + i)));
            int bx = x + w - 22 + i * 5;
            context.fill(bx, y + 20 - bh, bx + 3, y + 20, theme.accent);
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
