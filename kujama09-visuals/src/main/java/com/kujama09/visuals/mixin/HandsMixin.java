package com.kujama09.visuals.mixin;

import com.kujama09.visuals.Theme;
import com.kujama09.visuals.VisualsClient;
import com.kujama09.visuals.VisualsConfig;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Cambia la textura de los brazos en primera persona: bloques negros con borde del color del tema. */
@Mixin(PlayerEntityRenderer.class)
public abstract class HandsMixin {

    @ModifyVariable(method = "renderRightArm", at = @At("HEAD"), argsOnly = true)
    private Identifier kujama09_visuals$rightHand(Identifier original) {
        return kujama09_visuals$swap(original);
    }

    @ModifyVariable(method = "renderLeftArm", at = @At("HEAD"), argsOnly = true)
    private Identifier kujama09_visuals$leftHand(Identifier original) {
        return kujama09_visuals$swap(original);
    }

    private static Identifier kujama09_visuals$swap(Identifier original) {
        if (!VisualsConfig.get().hands) {
            return original;
        }
        return Identifier.of(VisualsClient.MOD_ID, "textures/entity/hand_" + Theme.current().id + ".png");
    }
}
