package com.kujama09.visuals.mixin;

import com.kujama09.visuals.VisualsConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quita la lluvia y las tormentas solo en el cliente: el renderizado y el sonido del clima dependen de estos
 * "gradientes". Se ignora en el mundo del servidor integrado, así el clima real y la jugabilidad no cambian.
 * Mixin opcional (defaultRequire = 0): si Minecraft cambia estos métodos, solo se pierde este efecto.
 */
@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
    private void kujama09_visuals$noRain(float delta, CallbackInfoReturnable<Float> cir) {
        if (VisualsConfig.get().noRain && (Object) this instanceof ClientWorld) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true)
    private void kujama09_visuals$noThunder(float delta, CallbackInfoReturnable<Float> cir) {
        if (VisualsConfig.get().noRain && (Object) this instanceof ClientWorld) {
            cir.setReturnValue(0.0f);
        }
    }
}
