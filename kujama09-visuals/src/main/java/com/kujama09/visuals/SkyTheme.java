package com.kujama09.visuals;

/** Colores de cielo disponibles (RGB 0..255). OFF = cielo normal de Minecraft. */
public enum SkyTheme {
    OFF("off", 0, 0, 0),
    RED("red", 200, 30, 30),
    AURORA("aurora", 30, 150, 190),
    PURPLE("purple", 120, 40, 200),
    FIRE("fire", 255, 120, 20);

    public final String id;
    public final int r;
    public final int g;
    public final int b;

    SkyTheme(String id, int r, int g, int b) {
        this.id = id;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int argb(int alpha) {
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }
}
