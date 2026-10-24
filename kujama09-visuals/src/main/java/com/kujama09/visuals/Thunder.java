package com.kujama09.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Truenos dentro de las nubes (sin rayos que caigan al suelo). Cada evento es un destello que ilumina una zona del
 * cielo con 2 o 3 pulsos rápidos, ramas finas dentro de la nube y, si se activa, un trueno lejano un instante
 * después. Es solo visual y de sonido: no cambia el clima real ni crea rayos.
 */
public final class Thunder {
    private static final Random RNG = new Random();

    private static long nextFlashAt;
    private static long flashStart = -1;
    private static float azimuth;      // grados, dirección del destello en el mundo
    private static float elevation;    // grados sobre el horizonte
    private static float spreadDeg;    // ancho del destello
    private static long[] pulseStart = new long[0];
    private static long[] pulseLen = new long[0];
    private static float[] pulsePower = new float[0];
    private static long leadIn;       // ms de iluminación previa, antes del primer pulso
    private static long thunderAt = -1;
    private static float thunderVolume;
    private static int[] forkX = new int[0];
    private static int[] forkY = new int[0];

    private Thunder() {
    }

    public static void render(DrawContext c, MinecraftClient mc, int width, int height, VisualsConfig cfg) {
        ClientWorld world = mc.world;
        ClientPlayerEntity player = mc.player;
        if (!cfg.thunder || world == null || player == null
                || !world.getRegistryKey().equals(World.OVERWORLD)) {
            flashStart = -1;
            return;
        }
        long now = System.currentTimeMillis();

        // Trueno pendiente (sonido lejano).
        if (thunderAt > 0 && now >= thunderAt) {
            thunderAt = -1;
            if (cfg.thunderSound) {
                mc.getSoundManager().play(PositionedSoundInstance.ambient(
                        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.55f + RNG.nextFloat() * 0.25f, thunderVolume));
            }
        }

        boolean sky = world.isSkyVisible(player.getBlockPos());
        if (flashStart < 0) {
            if (nextFlashAt == 0) {
                scheduleNext(now, cfg);
            }
            if (sky && now >= nextFlashAt) {
                startFlash(now, player.getYaw(), cfg);
            }
            return;
        }
        if (!sky) {
            return;
        }

        float power = currentPower(now);
        float lead = leadPower(now);
        if (power <= 0f && lead <= 0f) {
            if (now > flashStart + totalLength()) {
                flashStart = -1;
                scheduleNext(now, cfg);
            }
            return;
        }
        draw(c, mc, player, width, height, power, lead);
    }

    private static void scheduleNext(long now, VisualsConfig cfg) {
        // rate 0..100: de ~25 s entre truenos a ~4 s.
        float rate = Math.max(0, Math.min(100, cfg.thunderRate)) / 100f;
        long min = Math.round(25000 - 21000 * rate);
        long extra = Math.round(min * 0.9);
        nextFlashAt = now + min + (long) (RNG.nextDouble() * extra);
    }

    private static void startFlash(long now, float yaw, VisualsConfig cfg) {
        flashStart = now;
        // Casi siempre dentro del campo de visión actual, para que se note.
        azimuth = yaw + (RNG.nextFloat() - 0.5f) * 110f;
        elevation = 14f + RNG.nextFloat() * 36f;
        spreadDeg = 10f + RNG.nextFloat() * 12f;

        leadIn = 700 + RNG.nextInt(1100);
        int pulses = 2 + RNG.nextInt(2);
        pulseStart = new long[pulses];
        pulseLen = new long[pulses];
        pulsePower = new float[pulses];
        long t = leadIn;
        for (int i = 0; i < pulses; i++) {
            pulseStart[i] = t;
            pulseLen[i] = 70 + RNG.nextInt(110);
            pulsePower[i] = i == 0 ? 0.75f + RNG.nextFloat() * 0.25f : 0.35f + RNG.nextFloat() * 0.5f;
            t += pulseLen[i] + 50 + RNG.nextInt(110);
        }
        int n = 7 + RNG.nextInt(5);
        forkX = new int[n];
        forkY = new int[n];
        int y = 0;
        for (int i = 0; i < n; i++) {
            forkX[i] = i == 0 ? -40 : forkX[i - 1] + 6 + RNG.nextInt(10);
            y += RNG.nextInt(7) - 3;
            forkY[i] = y;
        }
        if (cfg.thunderSound) {
            thunderAt = now + leadIn + 900 + RNG.nextInt(2600);
            thunderVolume = 0.35f + RNG.nextFloat() * 0.3f;
        }
    }

    private static long totalLength() {
        int last = pulseStart.length - 1;
        return last < 0 ? 0 : pulseStart[last] + pulseLen[last];
    }

    /**
     * Iluminación previa al destello: las nubes se encienden de a poco con parpadeos irregulares, como cuando la
     * carga eléctrica se acumula antes del trueno. Dura {@code leadIn} ms y llega a su máximo justo antes del
     * primer pulso.
     */
    private static float leadPower(long now) {
        long t = now - flashStart;
        if (t < 0 || t >= leadIn) {
            return 0f;
        }
        float k = t / (float) leadIn;
        float ramp = k * k;
        double flicker = 0.55 + 0.45 * Math.sin(t * 0.045) * Math.sin(t * 0.0113 + 1.3)
                + 0.2 * Math.sin(t * 0.13 + 4.0);
        float p = (float) (ramp * Math.max(0.0, Math.min(1.0, flicker)) * 0.55);
        return Math.max(0f, p);
    }

    private static float currentPower(long now) {
        long t = now - flashStart - 0;
        for (int i = 0; i < pulseStart.length; i++) {
            if (t >= pulseStart[i] && t < pulseStart[i] + pulseLen[i]) {
                float k = (t - pulseStart[i]) / (float) pulseLen[i];
                // Sube muy rápido y se apaga más despacio.
                float shape = k < 0.15f ? k / 0.15f : 1f - (k - 0.15f) / 0.85f;
                return pulsePower[i] * Math.max(0f, shape);
            }
        }
        return 0f;
    }

    private static void draw(DrawContext c, MinecraftClient mc, ClientPlayerEntity player,
                             int width, int height, float power, float lead) {
        double fov = Math.max(30.0, mc.options.getFov().getValue());
        float ppd = (float) (height / fov);
        float yawDiff = ((azimuth - player.getYaw()) % 360f + 540f) % 360f - 180f;
        int cx = Math.round(width / 2f + yawDiff * ppd);
        int cy = Math.round(height / 2f - (elevation + player.getPitch()) * ppd);
        int horizonY = Math.round(height / 2f - player.getPitch() * ppd);
        int clipY = Math.max(0, Math.min(height, horizonY));
        if (clipY <= 0) {
            return;
        }

        c.enableScissor(0, 0, width, clipY);
        // Iluminación general suave del cielo (las nubes se encienden).
        float wash = Math.max(power, lead * 0.35f);
        c.fillGradient(0, 0, width, clipY, ((int) (wash * 38) << 24) | 0xC8DCFF,
                ((int) (wash * 12) << 24) | 0xC8DCFF);
        float glowPower = Math.max(power, lead);

        // Resplandor principal: elipse con degradado radial hecho de capas translúcidas.
        int rx = Math.round(spreadDeg * ppd);
        int ry = Math.round(spreadDeg * 0.42f * ppd);
        for (int i = 0; i < 9; i++) {
            float k = 1f - i / 9f;
            int a = Math.round(26 * glowPower * (0.5f + 0.5f * (1f - k)));
            int color = (a << 24) | (i < 5 ? 0xBFD4FF : 0xE8F0FF);
            int erx = Math.round(rx * k);
            int ery = Math.round(ry * k);
            for (int dy = -ery; dy <= ery; dy += 2) {
                double t = dy / (double) Math.max(1, ery);
                int half = (int) Math.round(erx * Math.sqrt(1.0 - t * t));
                c.fill(cx - half, cy + dy, cx + half, cy + dy + 2, color);
            }
        }
        // Ramas finas dentro de la nube.
        int core = ((int) (Math.min(1f, power * 1.3f) * 235) << 24) | 0xF4F8FF;
        int halo = ((int) (power * 70) << 24) | 0x9DB8FF;
        for (int i = 1; power > 0f && i < forkX.length; i++) {
            segment(c, cx + forkX[i - 1], cy + forkY[i - 1], cx + forkX[i], cy + forkY[i], halo, core);
        }
        c.disableScissor();
    }

    private static void segment(DrawContext c, int x1, int y1, int x2, int y2, int halo, int core) {
        int steps = Math.max(1, Math.abs(x2 - x1));
        for (int s = 0; s <= steps; s++) {
            int x = x1 + s;
            int y = y1 + Math.round((y2 - y1) * (s / (float) steps));
            c.fill(x - 1, y - 1, x + 2, y + 2, halo);
            c.fill(x, y, x + 1, y + 1, core);
        }
    }
}
