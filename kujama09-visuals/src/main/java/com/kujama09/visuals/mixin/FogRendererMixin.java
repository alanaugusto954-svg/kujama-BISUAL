package com.kujama09.visuals.mixin;

import com.kujama09.visuals.SkyTheme;
import com.kujama09.visuals.VisualsConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mezcla el color de la niebla/horizonte con el tema de cielo elegido. Este mixin es opcional
 * (required=false): si Minecraft cambia ese método, el mod sigue funcionando y solo se pierde este efecto.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "getFogColor", at = @At("RETURN"))
    private void kujama09_visuals$tintFog(Camera camera, float tickProgress, ClientWorld world,
                                          int viewDistance, float skyDarkness,
                                          CallbackInfoReturnable<Vector4f> cir) {
        VisualsConfig cfg = VisualsConfig.get();
        if (cfg.sky == SkyTheme.OFF || cfg.skyStrength <= 0) {
            return;
        }
        Vector4f color = cir.getReturnValue();
        if (color == null) {
            return;
        }
        float s = cfg.skyStrength / 100f;
        if (cfg.sky.realistic) {
            // Conserva la luz del día: de noche el cielo sigue oscuro.
            float light = Math.max(0f, Math.min(1f, Math.max(color.x, Math.max(color.y, color.z)) / 0.85f));
            color.x = color.x + (cfg.sky.r / 255f * light - color.x) * s;
            color.y = color.y + (cfg.sky.g / 255f * light - color.y) * s;
            color.z = color.z + (cfg.sky.b / 255f * light - color.z) * s;
            return;
        }
        color.x = color.x + (cfg.sky.r / 255f - color.x) * s;
        color.y = color.y + (cfg.sky.g / 255f - color.y) * s;
        color.z = color.z + (cfg.sky.b / 255f - color.z) * s;
    }
}
