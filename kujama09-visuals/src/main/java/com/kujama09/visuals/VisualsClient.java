package com.kujama09.visuals;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Punto de entrada. Registra el HUD y el comando /kv, con el que se activa y configura todo.
 */
public final class VisualsClient implements ClientModInitializer {
    public static final String MOD_ID = "kujama09_visuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        VisualsConfig.load();
        SpotifyReader.start();
        ClientTickEvents.END_CLIENT_TICK.register(ParticleSpawner::tick);
        ClientTickEvents.END_CLIENT_TICK.register(Fullbright::tick);
        ClientTickEvents.END_CLIENT_TICK.register(VisualsClient::pollMenuKey);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING
                .register(Fullbright::restore);

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "hud"),
                (context, tickCounter) -> HudRenderer.render(context));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("kv");

            root.then(toggle("hud", (c, v) -> c.hud = v));
            root.then(toggle("bar", (c, v) -> c.topBar = v));
            root.then(toggle("keys", (c, v) -> c.keystrokes = v));
            root.then(toggle("xyz", (c, v) -> c.coords = v));
            root.then(toggle("cps", (c, v) -> c.cps = v));
            root.then(toggle("music", (c, v) -> c.music = v));
            root.then(toggle("particles", (c, v) -> c.particles = v));
            root.then(toggle("lines", (c, v) -> c.skyLines = v));
            root.then(toggle("fullbright", (c, v) -> c.fullbright = v));
            root.then(toggle("hands", (c, v) -> c.hands = v));
            root.then(toggle("thunder", (c, v) -> c.thunder = v));
            root.then(toggle("thundersound", (c, v) -> c.thunderSound = v));
            root.then(ClientCommandManager.literal("thunderrate")
                    .then(ClientCommandManager.argument("valor", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> {
                                VisualsConfig.get().thunderRate = IntegerArgumentType.getInteger(ctx, "valor");
                                VisualsConfig.save();
                                ctx.getSource().sendFeedback(Text.literal("[kv] Frecuencia de truenos: "
                                        + VisualsConfig.get().thunderRate));
                                return 1;
                            })));
            root.then(ClientCommandManager.literal("logo")
                    .executes(ctx -> {
                        VisualsConfig cfg = VisualsConfig.get();
                        cfg.skyLogo = !cfg.skyLogo;
                        VisualsConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("[kv] logo: " + (cfg.skyLogo ? "ON" : "OFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.literal("reload").executes(ctx -> {
                        SkyLogo.reload();
                        ctx.getSource().sendFeedback(Text.literal("[kv] Logo recargado. Tu imagen: " + SkyLogo.file()));
                        return 1;
                    })));
            root.then(toggle("modules", (c, v) -> c.modulesPanel = v));
            root.then(toggle("keybinds", (c, v) -> c.keybindsPanel = v));

            root.then(ClientCommandManager.literal("menu").executes(ctx -> {
                net.minecraft.client.MinecraftClient.getInstance()
                        .send(() -> net.minecraft.client.MinecraftClient.getInstance().setScreen(new MenuScreen()));
                return 1;
            }));

            LiteralArgumentBuilder<FabricClientCommandSource> themeCmd = ClientCommandManager.literal("theme");
            for (Theme t : Theme.values()) {
                themeCmd.then(ClientCommandManager.literal(t.id).executes(ctx -> {
                    VisualsConfig.get().theme = t.id;
                    VisualsConfig.save();
                    ctx.getSource().sendFeedback(Text.literal("[kv] Tema: " + t.label));
                    return 1;
                }));
            }
            root.then(themeCmd);

            root.then(ClientCommandManager.literal("scale")
                    .then(ClientCommandManager.argument("valor", IntegerArgumentType.integer(1, 3))
                            .executes(ctx -> {
                                int v = IntegerArgumentType.getInteger(ctx, "valor");
                                VisualsConfig.get().hudScale = v;
                                VisualsConfig.save();
                                ctx.getSource().sendFeedback(Text.literal("[kv] Escala del HUD: " + v));
                                return 1;
                            })));

            LiteralArgumentBuilder<FabricClientCommandSource> sky = ClientCommandManager.literal("sky");
            for (SkyTheme theme : SkyTheme.values()) {
                sky.then(ClientCommandManager.literal(theme.id).executes(ctx -> {
                    VisualsConfig.get().sky = theme;
                    VisualsConfig.save();
                    ctx.getSource().sendFeedback(Text.literal("[kv] Cielo: " + theme.id));
                    return 1;
                }));
            }
            sky.then(ClientCommandManager.literal("strength")
                    .then(ClientCommandManager.argument("porcentaje", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> {
                                int v = IntegerArgumentType.getInteger(ctx, "porcentaje");
                                VisualsConfig.get().skyStrength = v;
                                VisualsConfig.save();
                                ctx.getSource().sendFeedback(Text.literal("[kv] Intensidad del cielo: " + v + "%"));
                                return 1;
                            })));
            root.then(sky);

            root.then(ClientCommandManager.literal("rain")
                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        VisualsConfig.get().noRain = true;
                        VisualsConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("[kv] Lluvia quitada (solo visual)"));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("on").executes(ctx -> {
                        VisualsConfig.get().noRain = false;
                        VisualsConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("[kv] Lluvia normal"));
                        return 1;
                    })));

            root.then(ClientCommandManager.literal("reset").executes(ctx -> {
                VisualsConfig.get().resetToDefaults();
                VisualsConfig.save();
                ctx.getSource().sendFeedback(Text.literal("[kv] Valores restaurados"));
                return 1;
            }));

            root.executes(ctx -> {
                ctx.getSource().sendFeedback(Text.literal(
                        "[kv] /kv hud|bar|keys|xyz|cps|music|particles|lines  |  /kv scale 1-3  |  /kv rain off|on  |  "
                                + "/kv sky void|off|red|aurora|purple|fire  |  /kv sky strength 0-100  |  /kv reset"));
                return 1;
            });

            dispatcher.register(root);
        });

        LOGGER.info("kujama09 visuals inicializado");
    }

    /** Subcomando que alterna una opción booleana: /kv nombre. */
    private static LiteralArgumentBuilder<FabricClientCommandSource> toggle(
            String name, BiConsumer<VisualsConfig, Boolean> setter) {
        return ClientCommandManager.literal(name).executes(ctx -> {
            VisualsConfig cfg = VisualsConfig.get();
            boolean now = current(cfg, name);
            setter.accept(cfg, !now);
            VisualsConfig.save();
            ctx.getSource().sendFeedback(Text.literal("[kv] " + name + ": " + (!now ? "ON" : "OFF")));
            return 1;
        });
    }

    private static boolean current(VisualsConfig c, String name) {
        return switch (name) {
            case "hud" -> c.hud;
            case "bar" -> c.topBar;
            case "keys" -> c.keystrokes;
            case "xyz" -> c.coords;
            case "cps" -> c.cps;
            case "music" -> c.music;
            case "particles" -> c.particles;
            case "lines" -> c.skyLines;
            case "fullbright" -> c.fullbright;
            case "hands" -> c.hands;
            case "thunder" -> c.thunder;
            case "thundersound" -> c.thunderSound;
            case "modules" -> c.modulesPanel;
            case "keybinds" -> c.keybindsPanel;
            default -> false;
        };
    }

    private static boolean shiftWasDown;

    /** Abre el menú con Right Shift (flanco de subida, solo si no hay otra pantalla abierta). */
    private static void pollMenuKey(net.minecraft.client.MinecraftClient client) {
        if (client.getWindow() == null) {
            return;
        }
        boolean down = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(),
                org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (down && !shiftWasDown && client.currentScreen == null && client.player != null) {
            client.setScreen(new MenuScreen());
        }
        shiftWasDown = down;
    }
}
