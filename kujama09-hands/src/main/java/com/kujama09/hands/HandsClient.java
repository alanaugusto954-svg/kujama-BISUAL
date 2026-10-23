package com.kujama09.hands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/** Manos alejadas y resplandor en primera persona. Comando: /kh. */
public final class HandsClient implements ClientModInitializer {
    public static final String MOD_ID = "kujama09_hands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        HandsConfig.load();

        // Antes del chat: el resplandor queda sobre las manos y debajo del resto de la interfaz.
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(MOD_ID, "glow"),
                (context, tickCounter) -> Glow.render(context));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("kh");

            root.then(ClientCommandManager.literal("menu").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.send(() -> mc.setScreen(new HandsScreen()));
                return 1;
            }));
            root.then(ClientCommandManager.literal("far").executes(ctx -> {
                HandsConfig c = HandsConfig.get();
                c.farHands = !c.farHands;
                done(ctx.getSource(), "manos alejadas: " + (c.farHands ? "ON" : "OFF"));
                return 1;
            }));
            root.then(ClientCommandManager.literal("glow").executes(ctx -> {
                HandsConfig c = HandsConfig.get();
                c.glow = !c.glow;
                done(ctx.getSource(), "resplandor: " + (c.glow ? "ON" : "OFF"));
                return 1;
            }));
            root.then(intCmd("scale", 30, 150, (c, v) -> c.scale = v));
            root.then(intCmd("x", -100, 100, (c, v) -> c.offsetX = v));
            root.then(intCmd("y", -100, 100, (c, v) -> c.offsetY = v));
            root.then(intCmd("z", -100, 100, (c, v) -> c.offsetZ = v));
            root.then(intCmd("intensity", 0, 100, (c, v) -> c.glowIntensity = v));
            root.then(ClientCommandManager.literal("color")
                    .then(ClientCommandManager.argument("hex", StringArgumentType.word()).executes(ctx -> {
                        String hex = StringArgumentType.getString(ctx, "hex");
                        if (HandsConfig.parseColor(hex) < 0) {
                            ctx.getSource().sendFeedback(Text.literal("[kh] Usa el formato #RRGGBB, por ejemplo #FFC83C"));
                            return 0;
                        }
                        HandsConfig.get().glowColor = hex.startsWith("#") ? hex : "#" + hex;
                        done(ctx.getSource(), "color: " + HandsConfig.get().glowColor);
                        return 1;
                    })));
            root.then(ClientCommandManager.literal("reset").executes(ctx -> {
                HandsConfig.get().resetToDefaults();
                done(ctx.getSource(), "valores restaurados");
                return 1;
            }));
            root.executes(ctx -> {
                ctx.getSource().sendFeedback(Text.literal(
                        "[kh] /kh menu | far | glow | scale 30-150 | x/y/z -100..100 | intensity 0-100 | color #RRGGBB | reset"));
                return 1;
            });
            dispatcher.register(root);
        });
        LOGGER.info("kujama09 hands inicializado");
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> intCmd(
            String name, int min, int max, BiConsumer<HandsConfig, Integer> setter) {
        return ClientCommandManager.literal(name)
                .then(ClientCommandManager.argument("valor", IntegerArgumentType.integer(min, max)).executes(ctx -> {
                    int v = IntegerArgumentType.getInteger(ctx, "valor");
                    setter.accept(HandsConfig.get(), v);
                    done(ctx.getSource(), name + ": " + v);
                    return 1;
                }));
    }

    private static void done(FabricClientCommandSource source, String message) {
        HandsConfig.save();
        source.sendFeedback(Text.literal("[kh] " + message));
    }
}
