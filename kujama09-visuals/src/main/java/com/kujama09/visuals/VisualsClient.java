package com.kujama09.visuals;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
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

            root.then(ClientCommandManager.literal("reset").executes(ctx -> {
                VisualsConfig.get().resetToDefaults();
                VisualsConfig.save();
                ctx.getSource().sendFeedback(Text.literal("[kv] Valores restaurados"));
                return 1;
            }));

            root.executes(ctx -> {
                ctx.getSource().sendFeedback(Text.literal(
                        "[kv] /kv hud|bar|keys|xyz|cps  |  /kv scale 1-3  |  "
                                + "/kv sky off|red|aurora|purple|fire  |  /kv sky strength 0-100  |  /kv reset"));
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
            default -> false;
        };
    }
}
