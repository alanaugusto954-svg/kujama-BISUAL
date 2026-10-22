package com.kujama09.visuals.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Permite fijar el valor de una opción sin pasar por su validador. Se usa para el fullbright: el brillo de
 * Minecraft solo acepta 0 a 1, pero el sistema de luz admite valores mayores.
 */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {

    @Accessor("value")
    void kujama09_visuals$setValue(T value);
}
