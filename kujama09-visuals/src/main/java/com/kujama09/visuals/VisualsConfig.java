package com.kujama09.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Configuración persistente, guardada en {@code config/kujama09_visuals.json}. */
public final class VisualsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("kujama09_visuals.json");
    private static VisualsConfig instance = new VisualsConfig();

    public boolean hud = true;
    public boolean topBar = true;
    public boolean keystrokes = true;
    public boolean coords = true;
    public boolean cps = true;
    /** Escala del HUD (1.0 a 3.0). */
    public float hudScale = 1.0f;
    public SkyTheme sky = SkyTheme.VOID;
    /** Intensidad del cielo de color, 0..100. */
    public int skyStrength = 100;
    /** Si es true, la lluvia y las tormentas dejan de verse y oirse (solo en el cliente). */
    public boolean noRain = false;
    /** Líneas blancas animadas sobre el cielo (solo con el tema void). */
    public boolean skyLines = true;
    /** Partículas claras flotando alrededor del jugador. */
    public boolean particles = true;
    /** Muestra la canción de Spotify que suena (solo Windows). */
    public boolean music = true;
    /** Tema de color de la interfaz (ver Theme). */
    public String theme = "amethyst";
    /** Fullbright: brillo por encima del máximo normal. */
    public boolean fullbright = false;
    /** Nivel de brillo del fullbright (2 a 15). */
    public float fullbrightLevel = 12.0f;
    /** Panel con las funciones activas, arriba a la derecha. */
    public boolean modulesPanel = true;
    /** Manos negras con borde de color en primera persona. */
    public boolean hands = true;
    /** Truenos dentro de las nubes (destellos, sin rayos al suelo). */
    public boolean thunder = false;
    /** Frecuencia de los truenos (0 a 100). */
    public int thunderRate = 50;
    /** Sonido de trueno lejano. */
    public boolean thunderSound = true;
    /** Logo flotando en el cielo. */
    public boolean skyLogo = false;
    /** Tamano del logo (0 a 100). */
    public int logoSize = 40;
    /** Panel con la tecla del menú. */
    public boolean keybindsPanel = true;

    public static VisualsConfig get() {
        return instance;
    }

    public static void load() {
        VisualsConfig loaded = null;
        if (Files.isRegularFile(FILE)) {
            try {
                loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), VisualsConfig.class);
            } catch (Exception e) {
                VisualsClient.LOGGER.warn("No se pudo leer {}, se usan valores por defecto", FILE, e);
            }
        }
        instance = loaded != null ? loaded : new VisualsConfig();
        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            VisualsClient.LOGGER.warn("No se pudo guardar {}", FILE, e);
        }
    }

    public void resetToDefaults() {
        VisualsConfig d = new VisualsConfig();
        hud = d.hud;
        topBar = d.topBar;
        keystrokes = d.keystrokes;
        coords = d.coords;
        cps = d.cps;
        hudScale = d.hudScale;
        sky = d.sky;
        skyStrength = d.skyStrength;
        noRain = d.noRain;
        skyLines = d.skyLines;
        particles = d.particles;
        music = d.music;
        theme = d.theme;
        fullbright = d.fullbright;
        fullbrightLevel = d.fullbrightLevel;
        modulesPanel = d.modulesPanel;
        hands = d.hands;
        thunder = d.thunder;
        thunderRate = d.thunderRate;
        thunderSound = d.thunderSound;
        skyLogo = d.skyLogo;
        logoSize = d.logoSize;
        keybindsPanel = d.keybindsPanel;
    }

    private void sanitize() {
        if (Float.isNaN(hudScale)) hudScale = 1.0f;
        hudScale = Math.max(1.0f, Math.min(3.0f, hudScale));
        skyStrength = Math.max(0, Math.min(100, skyStrength));
        if (sky == null) sky = SkyTheme.OFF;
        if (theme == null) theme = "amethyst";
        logoSize = Math.max(0, Math.min(100, logoSize));
        thunderRate = Math.max(0, Math.min(100, thunderRate));
        if (Float.isNaN(fullbrightLevel)) fullbrightLevel = 12.0f;
        fullbrightLevel = Math.max(2.0f, Math.min(15.0f, fullbrightLevel));
    }
}
