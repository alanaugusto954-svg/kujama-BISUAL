package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Logo que flota en el cielo. Por defecto usa el zorro de kujama09; si existe
 * {@code config/kujama09_visuals/logo.png} se usa esa imagen (la que tú pongas).
 */
public final class SkyLogo {
    private static final Identifier DEFAULT = Identifier.of(VisualsClient.MOD_ID, "textures/gui/logo.png");
    private static final Identifier CUSTOM = Identifier.of(VisualsClient.MOD_ID, "custom_logo");

    private static Identifier active = DEFAULT;
    private static NativeImageBackedTexture custom;
    private static boolean loaded;

    private SkyLogo() {
    }

    public static Path file() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
                .resolve("kujama09_visuals").resolve("logo.png");
    }

    /** Vuelve a leer la imagen personalizada (o el logo por defecto si no existe). */
    public static void reload() {
        MinecraftClient mc = MinecraftClient.getInstance();
        loaded = true;
        active = DEFAULT;
        try {
            if (custom != null) {
                mc.getTextureManager().destroyTexture(CUSTOM);
                custom = null;
            }
            Path p = file();
            if (Files.isRegularFile(p)) {
                try (InputStream in = Files.newInputStream(p)) {
                    NativeImage img = NativeImage.read(in);
                    custom = new NativeImageBackedTexture(() -> "kujama09_visuals_logo", img);
                    mc.getTextureManager().registerTexture(CUSTOM, custom);
                    active = CUSTOM;
                }
            }
        } catch (Throwable t) {
            VisualsClient.LOGGER.warn("No se pudo cargar el logo personalizado", t);
            active = DEFAULT;
        }
    }

    public static void render(DrawContext c, MinecraftClient mc, int width, int height, VisualsConfig cfg) {
        if (!cfg.skyLogo || mc.player == null) {
            return;
        }
        if (!loaded) {
            reload();
        }
        int size = 48 + Math.round(cfg.logoSize / 100f * 112f);
        float t = System.nanoTime() / 1_000_000_000f;
        // Se mueve con la cámara para parecer parte del cielo.
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        float wrapped = ((yaw % 360f) + 540f) % 360f - 180f;
        float px = width / 2f - wrapped * (width / 90f);
        float py = height * 0.28f - (pitch + 20f) * (height / 70f) + (float) Math.sin(t * 0.8f) * 4f;
        int x = Math.round(px - size / 2f);
        int y = Math.round(py - size / 2f);
        if (x + size < 0 || x > width || y + size < 0 || y > height) {
            return;
        }
        c.drawTexture(RenderPipelines.GUI_TEXTURED, active, x, y, 0f, 0f, size, size, size, size, size, size,
                0xD0FFFFFF);
    }
}
