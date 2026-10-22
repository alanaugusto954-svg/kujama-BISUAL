package com.kujama09.visuals;

/** Temas de color de la interfaz. {@code accent} es el color principal y {@code accent2} el secundario (ARGB). */
public enum Theme {
    AMETHYST("amethyst", "Amethyst", 0xFFC15CFF, 0xFF8E3BE0),
    VOID("void", "Void", 0xFF7B6CFF, 0xFF4B3AD8),
    BLOOD("blood", "Blood", 0xFFFF3B4E, 0xFFB5172B),
    SUNSET("sunset", "Sunset", 0xFFFF9A3C, 0xFFE0621E),
    PLASMA("plasma", "Plasma", 0xFFFF3CF0, 0xFFB615C9),
    ROSE("rose", "Rose", 0xFFFF7FA8, 0xFFE0457B),
    OCEAN("ocean", "Ocean", 0xFF4DB6FF, 0xFF1E78D6),
    ARCTIC("arctic", "Arctic", 0xFF7DE3FF, 0xFF2EA6C9),
    AQUA("aqua", "Aqua", 0xFF35F0D0, 0xFF16A58E),
    MATRIX("matrix", "Matrix", 0xFF22FF66, 0xFF11B845),
    LIME("lime", "Lime", 0xFFB8F23C, 0xFF7FC21E),
    GOLD("gold", "Gold", 0xFFFFD21F, 0xFFD39A0E),
    COBALT("cobalt", "Cobalt", 0xFF5B84E8, 0xFF2F55B8),
    MONO("mono", "Mono", 0xFFF2F2F2, 0xFF9A9A9A),
    SLATE("slate", "Slate", 0xFF9AA6BC, 0xFF627089),
    LAVA("lava", "Lava", 0xFFFF5A1F, 0xFFC22A00);

    public final String id;
    public final String label;
    public final int accent;
    public final int accent2;

    Theme(String id, String label, int accent, int accent2) {
        this.id = id;
        this.label = label;
        this.accent = accent;
        this.accent2 = accent2;
    }

    public static Theme byId(String id) {
        for (Theme t : values()) {
            if (t.id.equalsIgnoreCase(id)) {
                return t;
            }
        }
        return AMETHYST;
    }

    /** Tema activo según la configuración. */
    public static Theme current() {
        return byId(VisualsConfig.get().theme);
    }
}
