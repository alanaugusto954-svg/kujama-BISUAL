package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

/**
 * Partículas claras que flotan alrededor del jugador (destellos y hojas pálidas). Son partículas normales
 * de Minecraft, creadas solo en el cliente: no afectan al servidor ni a la jugabilidad.
 */
public final class ParticleSpawner {
    private static final Random RANDOM = new Random();

    private ParticleSpawner() {
    }

    public static void tick(MinecraftClient client) {
        if (!VisualsConfig.get().particles) {
            return;
        }
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if (world == null || player == null) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            double x = player.getX() + (RANDOM.nextDouble() - 0.5) * 18.0;
            double y = player.getY() + RANDOM.nextDouble() * 7.0 - 1.0;
            double z = player.getZ() + (RANDOM.nextDouble() - 0.5) * 18.0;
            double vx = (RANDOM.nextDouble() - 0.5) * 0.02;
            double vy = (RANDOM.nextDouble() - 0.5) * 0.01;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.02;
            if (RANDOM.nextInt(3) == 0) {
                world.addParticleClient(ParticleTypes.PALE_OAK_LEAVES, x, y, z, vx, vy, vz);
            } else {
                world.addParticleClient(ParticleTypes.END_ROD, x, y, z, vx, vy, vz);
            }
        }
    }
}
