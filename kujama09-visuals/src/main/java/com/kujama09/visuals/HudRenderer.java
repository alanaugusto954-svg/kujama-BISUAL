package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.joml.Matrix3x2fStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Dibuja el HUD: barra superior, teclas, coordenadas y CPS. */
public final class HudRenderer {
    private static final int BG = 0xAA0B0E14;
    private static final int BORDER = 0x40FFFFFF;
    private static final int GREEN = 0xFF2ED573;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFB8C0CC;
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

        // Atmósfera: degradado de color arriba, para el cielo de color.
        if (cfg.sky != SkyTheme.OFF && cfg.skyStrength > 0) {
            int alpha = Math.round(0x70 * (cfg.skyStrength / 100f));
            context.fillGradient(0, 0, width, Math.round(height * 0.55f), cfg.sky.argb(alpha), cfg.sky.argb(0));
        }

        CpsCounter.poll(mc.getWindow().getHandle(), mc.currentScreen == null);

        if (!cfg.hud) {
            return;
        }
        TextRenderer font = mc.textRenderer;
        float scale = cfg.hudScale;
        float vw = width / scale;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scale, scale);

        if (cfg.topBar) {
            drawTopBar(context, font, mc, (int) vw);
        }
        int y = 6;
        if (cfg.keystrokes) {
            y = drawKeys(context, font, mc, 6, y) + 6;
        }
        if (cfg.coords) {
            String xyz = String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            drawPanelText(context, font, xyz, 6, y, WHITE);
            y += 16;
        }
        if (cfg.cps) {
            String text = "CPS: " + CpsCounter.left() + " | " + CpsCounter.right();
            drawPanelText(context, font, text, 6, y, GREY);
        }

        matrices.popMatrix();
    }

    private static void drawTopBar(DrawContext context, TextRenderer font, MinecraftClient mc, int virtualWidth) {
        String name = mc.player.getName().getString();
        String fps = mc.getCurrentFps() + " fps";
        String time = LocalTime.now().format(CLOCK);

        int pad = 6;
        int badge = 11;
        int sep = 9;
        int w = pad + badge + 5 + font.getWidth(name) + sep + font.getWidth(fps) + sep + font.getWidth(time) + pad;
        int h = 17;
        int x = (virtualWidth - w) / 2;
        int y = 3;

        context.fill(x - 1, y - 1, x + w + 1, y + h + 1, BORDER);
        context.fill(x, y, x + w, y + h, BG);

        int cx = x + pad;
        context.fill(cx, y + 3, cx + badge, y + 3 + badge, GREEN);
        context.drawText(font, "K", cx + 3, y + 5, 0xFF04210F, false);
        cx += badge + 5;
        context.drawText(font, name, cx, y + 5, WHITE, true);
        cx += font.getWidth(name) + sep;
        context.drawText(font, fps, cx, y + 5, GREEN, true);
        cx += font.getWidth(fps) + sep;
        context.drawText(font, time, cx, y + 5, GREY, true);
    }

    /** Devuelve la coordenada Y donde termina el bloque de teclas. */
    private static int drawKeys(DrawContext context, TextRenderer font, MinecraftClient mc, int x, int y) {
        int k = 18;
        int gap = 2;
        drawKey(context, font, "W", mc.options.forwardKey, x + k + gap, y, k, k);
        drawKey(context, font, "A", mc.options.leftKey, x, y + k + gap, k, k);
        drawKey(context, font, "S", mc.options.backKey, x + k + gap, y + k + gap, k, k);
        drawKey(context, font, "D", mc.options.rightKey, x + 2 * (k + gap), y + k + gap, k, k);
        drawKey(context, font, "SPACE", mc.options.jumpKey, x, y + 2 * (k + gap), 3 * k + 2 * gap, 14);
        return y + 2 * (k + gap) + 14;
    }

    private static void drawKey(DrawContext context, TextRenderer font, String label, KeyBinding key,
                                int x, int y, int w, int h) {
        boolean down = key.isPressed();
        context.fill(x, y, x + w, y + h, down ? 0xCCFFFFFF : 0x99000000);
        int color = down ? 0xFF0B0E14 : WHITE;
        context.drawText(font, label, x + (w - font.getWidth(label)) / 2, y + (h - 8) / 2, color, false);
    }

    private static void drawPanelText(DrawContext context, TextRenderer font, String text, int x, int y, int color) {
        context.fill(x - 3, y - 2, x + font.getWidth(text) + 3, y + 11, 0x80000000);
        context.drawText(font, text, x, y, color, true);
    }
}
