package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;

/** Convierte la portada descargada por SpotifyReader en una textura del juego (se llama desde el hilo de render). */
public final class SpotifyCover {
    private static final Identifier ID = Identifier.of(VisualsClient.MOD_ID, "spotify_cover");
    private static NativeImageBackedTexture texture;
    private static int loadedVersion = -1;
    private static boolean valid;

    private SpotifyCover() {
    }

    /** Devuelve la textura de la portada, o null si no hay una disponible. */
    public static Identifier get() {
        if (!VisualsConfig.get().musicCover || !SpotifyReader.hasCover()) {
            return null;
        }
        int version = SpotifyReader.coverVersion();
        if (version != loadedVersion) {
            loadedVersion = version;
            load();
        }
        return valid ? ID : null;
    }

    private static void load() {
        MinecraftClient mc = MinecraftClient.getInstance();
        valid = false;
        try {
            if (texture != null) {
                mc.getTextureManager().destroyTexture(ID);
                texture = null;
            }
            try (InputStream in = Files.newInputStream(SpotifyReader.COVER_FILE)) {
                NativeImage image = NativeImage.read(in);
                texture = new NativeImageBackedTexture(() -> "kujama09_visuals_spotify_cover", image);
                mc.getTextureManager().registerTexture(ID, texture);
                valid = true;
            }
        } catch (Throwable t) {
            VisualsClient.LOGGER.warn("No se pudo cargar la portada de Spotify", t);
            valid = false;
        }
    }
}
