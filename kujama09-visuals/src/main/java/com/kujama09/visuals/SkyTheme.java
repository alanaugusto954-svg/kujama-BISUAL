package com.kujama09.visuals;

/** Colores de cielo disponibles (RGB 0..255). OFF = cielo normal de Minecraft. */
public enum SkyTheme {
    OFF("off", 0, 0, 0),
    /** Cielo negro con líneas blancas animadas (estilo "clean"). */
    VOID("void", 0, 0, 0),
    RED("red", 200, 30, 30),
    AURORA("aurora", 30, 150, 190),
    PURPLE("purple", 120, 40, 200),
    FIRE("fire", 255, 120, 20),
    /** Cielos realistas: conservan el ciclo día/noche (se oscurecen de noche). */
    STORM("storm", 62, 70, 86, true),
    OVERCAST("overcast", 128, 136, 148, true),
    DUSK("dusk", 214, 120, 96, true);

    public final String id;
    public final int r;
    public final int g;
    public final int b;
    /** Si es true, el color se atenua según la luz del día en vez de ser plano. */
    public final boolean realistic;

    SkyTheme(String id, int r, int g, int b) {
        this(id, r, g, b, false);
    }

    SkyTheme(String id, int r, int g, int b, boolean realistic) {
        this.realistic = realistic;
        this.id = id;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int argb(int alpha) {
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }
}
