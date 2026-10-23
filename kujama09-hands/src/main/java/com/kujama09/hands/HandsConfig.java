package com.kujama09.hands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Configuración guardada en {@code config/kujama09_hands.json}. */
public final class HandsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("kujama09_hands.json");
    private static HandsConfig instance = new HandsConfig();

    /** Activa el efecto de manos alejadas. */
    public boolean farHands = true;
    /** Tamano de manos y objetos, en porcentaje (30 a 150). */
    public int scale = 80;
    /** Desplazamiento horizontal hacia afuera, en centesimas de bloque (-100 a 100). */
    public int offsetX = 25;
    /** Desplazamiento vertical (-100 a 100); negativo = mas abajo. */
    public int offsetY = -10;
    /** Profundidad (-100 a 100); negativo = mas lejos. */
    public int offsetZ = -20;
    /** Resplandor dorado en las esquinas donde estan las manos. */
    public boolean glow = true;
    /** Color del resplandor, formato #RRGGBB. */
    public String glowColor = "#FFC83C";
    /** Intensidad del resplandor (0 a 100). */
    public int glowIntensity = 40;

    public static HandsConfig get() {
        return instance;
    }

    public static void load() {
        HandsConfig loaded = null;
        if (Files.exists(FILE)) {
            try {
                loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), HandsConfig.class);
            } catch (Exception e) {
                HandsClient.LOGGER.warn("No se pudo leer {}, se usan valores por defecto", FILE, e);
            }
        }
        instance = loaded != null ? loaded : new HandsConfig();
        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            HandsClient.LOGGER.warn("No se pudo guardar {}", FILE, e);
        }
    }

    public void resetToDefaults() {
        HandsConfig d = new HandsConfig();
        farHands = d.farHands;
        scale = d.scale;
        offsetX = d.offsetX;
        offsetY = d.offsetY;
        offsetZ = d.offsetZ;
        glow = d.glow;
        glowColor = d.glowColor;
        glowIntensity = d.glowIntensity;
    }

    private void sanitize() {
        scale = clamp(scale, 30, 150);
        offsetX = clamp(offsetX, -100, 100);
        offsetY = clamp(offsetY, -100, 100);
        offsetZ = clamp(offsetZ, -100, 100);
        glowIntensity = clamp(glowIntensity, 0, 100);
        if (glowColor == null || parseColor(glowColor) < 0) {
            glowColor = "#FFC83C";
        }
    }

    public static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** Devuelve el color como 0xRRGGBB, o -1 si el texto no es válido. */
    public static int parseColor(String text) {
        String s = text.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6) {
            return -1;
        }
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int glowRgb() {
        int c = parseColor(glowColor);
        return c < 0 ? 0xFFC83C : c;
    }
}
