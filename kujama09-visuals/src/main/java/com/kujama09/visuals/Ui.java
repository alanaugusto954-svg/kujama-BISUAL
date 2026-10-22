package com.kujama09.visuals;

import net.minecraft.client.gui.DrawContext;

/** Utilidades de dibujo para la interfaz: rectángulos redondeados, resplandor y colores. */
public final class Ui {
    private Ui() {
    }

    /** Multiplica el canal alfa de un color ARGB por {@code mult} (0..1). */
    public static int alpha(int argb, float mult) {
        float m = mult < 0f ? 0f : Math.min(mult, 1f);
        int a = Math.round(((argb >>> 24) & 0xFF) * m);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static int lerp(int c0, int c1, float t) {
        float k = t < 0f ? 0f : Math.min(t, 1f);
        int a = ch(c0, c1, 24, k);
        int r = ch(c0, c1, 16, k);
        int g = ch(c0, c1, 8, k);
        int b = ch(c0, c1, 0, k);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int ch(int c0, int c1, int shift, float t) {
        int a = (c0 >>> shift) & 0xFF;
        int b = (c1 >>> shift) & 0xFF;
        return Math.round(a + (b - a) * t);
    }

    public static boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    /** Se acerca a {@code target} de forma suave, independiente de los fotogramas (dt en segundos). */
    public static float approach(float current, float target, float dt, float speed) {
        return current + (target - current) * (1f - (float) Math.exp(-dt * speed));
    }

    /** Rectángulo con las esquinas redondeadas, dibujado con filas horizontales. */
    public static void roundedFill(DrawContext c, int x1, int y1, int x2, int y2, int radius, int color) {
        if (x2 <= x1 || y2 <= y1 || (color >>> 24) == 0) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
        if (r == 0) {
            c.fill(x1, y1, x2, y2, color);
            return;
        }
        c.fill(x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * (double) r - dy * dy));
            c.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            c.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
        }
    }

    /** Panel redondeado con borde de 1 píxel. */
    public static void panel(DrawContext c, int x1, int y1, int x2, int y2, int radius, int fill, int border) {
        roundedFill(c, x1, y1, x2, y2, radius, border);
        roundedFill(c, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
    }

    /** Resplandor suave alrededor de un rectángulo (capas translúcidas cada vez más grandes). */
    public static void glow(DrawContext c, int x1, int y1, int x2, int y2, int radius, int color) {
        for (int i = 4; i >= 1; i--) {
            roundedFill(c, x1 - i * 2, y1 - i * 2, x2 + i * 2, y2 + i * 2, radius + i * 2,
                    alpha(color, 0.18f));
        }
    }

    public static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
