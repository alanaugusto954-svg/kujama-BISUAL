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
    public SkyTheme sky = SkyTheme.OFF;
    /** Intensidad del cielo de color, 0..100. */
    public int skyStrength = 70;

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
    }

    private void sanitize() {
        if (Float.isNaN(hudScale)) hudScale = 1.0f;
        hudScale = Math.max(1.0f, Math.min(3.0f, hudScale));
        skyStrength = Math.max(0, Math.min(100, skyStrength));
        if (sky == null) sky = SkyTheme.OFF;
    }
}
