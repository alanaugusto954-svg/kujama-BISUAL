package com.kujama09.visuals;

import com.kujama09.visuals.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Fullbright: sube el brillo del juego por encima del máximo normal. Guarda el valor original y lo restaura al
 * apagarlo y al cerrar el juego, para no dejar un valor inválido en options.txt.
 */
public final class Fullbright {
    private static double saved = 0.5;
    private static boolean applied;

    private Fullbright() {
    }

    public static void tick(MinecraftClient client) {
        if (client.options == null) {
            return;
        }
        VisualsConfig cfg = VisualsConfig.get();
        SimpleOption<Double> gamma = client.options.getGamma();
        if (cfg.fullbright) {
            if (!applied) {
                saved = Math.max(0.0, Math.min(1.0, gamma.getValue()));
                applied = true;
            }
            if (Math.abs(gamma.getValue() - cfg.fullbrightLevel) > 1.0e-4) {
                force(gamma, cfg.fullbrightLevel);
            }
        } else if (applied) {
            restore(client);
        }
    }

    /** Devuelve el brillo original si el fullbright estaba aplicado. */
    public static void restore(MinecraftClient client) {
        if (!applied || client.options == null) {
            return;
        }
        applied = false;
        force(client.options.getGamma(), saved);
    }

    @SuppressWarnings("unchecked")
    private static void force(SimpleOption<Double> option, double value) {
        try {
            ((SimpleOptionAccessor<Double>) (Object) option).kujama09_visuals$setValue(value);
        } catch (Throwable t) {
            // Si el mixin no se aplicó, se usa el máximo permitido por el juego (100%).
            option.setValue(Math.max(0.0, Math.min(1.0, value)));
        }
    }
}
