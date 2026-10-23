package com.kujama09.hands.mixin;

import com.kujama09.hands.HandsConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mueve y escala solo el dibujo de la mano y del objeto en primera persona. No toca la cámara, la posición real
 * del objeto ni nada del juego: únicamente la matriz con la que se dibuja.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    private boolean kujama09_hands$pushed;

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void kujama09_hands$begin(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                                      float swingProgress, ItemStack item, float equipProgress,
                                      MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                      CallbackInfo ci) {
        kujama09_hands$pushed = false;
        HandsConfig cfg = HandsConfig.get();
        if (!cfg.farHands) {
            return;
        }
        boolean right = (hand == Hand.MAIN_HAND) == (player.getMainArm() == Arm.RIGHT);
        float side = right ? 1f : -1f;
        matrices.push();
        kujama09_hands$pushed = true;
        matrices.translate(side * cfg.offsetX / 100f, cfg.offsetY / 100f, cfg.offsetZ / 100f);
        float s = cfg.scale / 100f;
        matrices.scale(s, s, s);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void kujama09_hands$end(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                                    float swingProgress, ItemStack item, float equipProgress,
                                    MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                    CallbackInfo ci) {
        if (kujama09_hands$pushed) {
            kujama09_hands$pushed = false;
            matrices.pop();
        }
    }
}
