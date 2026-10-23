package com.kujama09.hands;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Menú de ajuste con controles deslizantes. Se abre con /kh menu. El fondo queda transparente para ver las manos. */
public final class HandsScreen extends Screen {
    private record Slider(int x1, int y1, int x2, int y2, int min, int max, Consumer<Integer> setter) {
    }

    private record Button(int x1, int y1, int x2, int y2, Runnable action) {
    }

    private final List<Slider> sliders = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private Slider dragging;

    public HandsScreen() {
        super(Text.literal("kujama09 hands"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        HandsConfig.save();
        super.close();
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            for (Slider s : sliders) {
                if (inside(click.x(), click.y(), s.x1, s.y1, s.x2, s.y2)) {
                    dragging = s;
                    apply(s, click.x());
                    return true;
                }
            }
            for (Button b : buttons) {
                if (inside(click.x(), click.y(), b.x1, b.y1, b.x2, b.y2)) {
                    b.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private static boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private static void apply(Slider s, double mx) {
        double f = Math.max(0, Math.min(1, (mx - s.x1) / Math.max(1, s.x2 - s.x1)));
        s.setter.accept((int) Math.round(s.min + f * (s.max - s.min)));
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        sliders.clear();
        buttons.clear();
        if (dragging != null) {
            if (GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                apply(dragging, mouseX);
            } else {
                dragging = null;
            }
        }
        HandsConfig cfg = HandsConfig.get();
        int accent = 0xFF000000 | cfg.glowRgb();

        int w = 190;
        int h = 196;
        int x = 10;
        int y = (height - h) / 2;
        c.fill(x, y, x + w, y + h, 0xD0101016);
        c.fill(x, y, x + w, y + 1, accent);
        c.drawText(textRenderer, "kujama09 hands", x + 8, y + 7, accent, true);
        int cy = y + 22;

        cy = toggle(c, x, cy, w, "Manos alejadas", cfg.farHands, mouseX, mouseY, accent, () -> cfg.farHands = !cfg.farHands);
        cy = slider(c, x, cy, w, "Tamano", cfg.scale, 30, 150, accent, v -> cfg.scale = v);
        cy = slider(c, x, cy, w, "Horizontal", cfg.offsetX, -100, 100, accent, v -> cfg.offsetX = v);
        cy = slider(c, x, cy, w, "Vertical", cfg.offsetY, -100, 100, accent, v -> cfg.offsetY = v);
        cy = slider(c, x, cy, w, "Profundidad", cfg.offsetZ, -100, 100, accent, v -> cfg.offsetZ = v);
        cy = toggle(c, x, cy, w, "Resplandor", cfg.glow, mouseX, mouseY, accent, () -> cfg.glow = !cfg.glow);
        cy = slider(c, x, cy, w, "Intensidad", cfg.glowIntensity, 0, 100, accent, v -> cfg.glowIntensity = v);

        c.drawText(textRenderer, "Color: " + cfg.glowColor + "  (/kh color #RRGGBB)", x + 8, cy + 2, 0xFFB8BCC8, false);
        c.fill(x + w - 22, cy, x + w - 8, cy + 10, accent);
        cy += 16;
        int bx2 = x + 8 + 70;
        boolean hov = inside(mouseX, mouseY, x + 8, cy, bx2, cy + 14);
        c.fill(x + 8, cy, bx2, cy + 14, hov ? 0xFFB5172B : 0xFF7A1020);
        c.drawText(textRenderer, "Restaurar", x + 16, cy + 3, 0xFFFFFFFF, false);
        buttons.add(new Button(x + 8, cy, bx2, cy + 14, cfg::resetToDefaults));
    }

    private int toggle(DrawContext c, int x, int y, int w, String label, boolean on, int mx, int my, int accent,
                       Runnable action) {
        boolean hov = inside(mx, my, x + 4, y, x + w - 4, y + 16);
        if (hov) {
            c.fill(x + 4, y, x + w - 4, y + 16, 0x20FFFFFF);
        }
        c.drawText(textRenderer, label, x + 8, y + 4, 0xFFE6E8EE, false);
        int sx = x + w - 34;
        c.fill(sx, y + 3, sx + 24, y + 13, on ? accent : 0xFF3A3C46);
        int knob = on ? sx + 15 : sx + 1;
        c.fill(knob, y + 4, knob + 8, y + 12, 0xFFFFFFFF);
        buttons.add(new Button(x + 4, y, x + w - 4, y + 16, () -> {
            action.run();
            HandsConfig.save();
        }));
        return y + 17;
    }

    private int slider(DrawContext c, int x, int y, int w, String label, int value, int min, int max, int accent,
                       Consumer<Integer> setter) {
        c.drawText(textRenderer, label, x + 8, y + 1, 0xFFE6E8EE, false);
        String v = String.valueOf(value);
        c.drawText(textRenderer, v, x + w - 8 - textRenderer.getWidth(v), y + 1, accent, false);
        int sx1 = x + 8;
        int sx2 = x + w - 8;
        int sy = y + 13;
        c.fill(sx1, sy, sx2, sy + 3, 0xFF2A2C34);
        float f = (value - min) / (float) (max - min);
        int fx = sx1 + Math.round((sx2 - sx1) * f);
        c.fill(sx1, sy, Math.max(sx1 + 1, fx), sy + 3, accent);
        c.fill(fx - 2, sy - 2, fx + 2, sy + 5, 0xFFFFFFFF);
        sliders.add(new Slider(sx1, sy - 4, sx2, sy + 8, min, max, setter));
        return y + 22;
    }
}
