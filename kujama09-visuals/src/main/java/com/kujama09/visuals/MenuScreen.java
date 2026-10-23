package com.kujama09.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Menú de ajustes (Right Shift). Interfaz en modo inmediato: cada fotograma se dibuja y se registran las zonas
 * clicables. Pestañas: Visual, World, Themes y Settings.
 */
public final class MenuScreen extends Screen {
    private static final String[] TABS = {"Visual", "World", "Themes", "Settings"};
    private static int lastTab = 0;

    private record Hit(int x1, int y1, int x2, int y2, Runnable action) {
    }

    private record SliderHit(int x1, int y1, int x2, int y2, Consumer<Float> setter) {
    }

    private final List<Hit> hits = new ArrayList<>();
    private final List<SliderHit> sliders = new ArrayList<>();
    private SliderHit dragging;
    private int tab = lastTab;
    private final long openedAt = System.nanoTime();
    private long lastFrame = System.nanoTime();
    private final float[] tabHover = new float[TABS.length];

    public MenuScreen() {
        super(Text.literal("kujama09 visuals"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        VisualsConfig.save();
        super.close();
    }

    // ---------------------------------------------------------------- entrada

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT || input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            double mx = click.x();
            double my = click.y();
            for (SliderHit s : sliders) {
                if (Ui.inside(mx, my, s.x1, s.y1, s.x2, s.y2)) {
                    dragging = s;
                    applySlider(s, mx);
                    return true;
                }
            }
            for (Hit h : hits) {
                if (Ui.inside(mx, my, h.x1, h.y1, h.x2, h.y2)) {
                    h.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private static void applySlider(SliderHit s, double mx) {
        float f = (float) ((mx - s.x1) / Math.max(1, s.x2 - s.x1));
        s.setter.accept(Math.max(0f, Math.min(1f, f)));
    }

    // ---------------------------------------------------------------- dibujo

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        float open = Math.min(1f, (now - openedAt) / 220_000_000f);
        float ease = 1f - (1f - open) * (1f - open) * (1f - open);

        hits.clear();
        sliders.clear();

        if (dragging != null) {
            long handle = client.getWindow().getHandle();
            if (GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                applySlider(dragging, mouseX);
            } else {
                dragging = null;
            }
        }

        Theme theme = Theme.current();
        TextRenderer font = textRenderer;

        // Fondo oscurecido
        c.fill(0, 0, width, height, Ui.alpha(0xB0050508, ease));

        int w = Math.min(width - 20, 400);
        int h = Math.min(height - 20, 250);
        int x = (width - w) / 2;
        int y = (height - h) / 2 + Math.round((1f - ease) * 14f);

        Ui.glow(c, x, y, x + w, y + h, 6, Ui.alpha(theme.accent, ease));
        Ui.panel(c, x, y, x + w, y + h, 6, Ui.alpha(0xF0101016, ease), Ui.alpha(0x40FFFFFF, ease));

        // Barra lateral
        int sideW = 92;
        Ui.roundedFill(c, x + 4, y + 4, x + sideW, y + h - 4, 4, Ui.alpha(0x90000000, ease));
        c.drawText(font, "kujama09", x + 12, y + 12, Ui.alpha(0xFFFFFFFF, ease), true);
        c.drawText(font, "visuals", x + 12, y + 23, Ui.alpha(theme.accent, ease), true);
        c.fill(x + 12, y + 36, x + sideW - 8, y + 37, Ui.alpha(0x30FFFFFF, ease));

        for (int i = 0; i < TABS.length; i++) {
            int ty = y + 44 + i * 24;
            int tx1 = x + 8;
            int tx2 = x + sideW - 4;
            boolean hover = Ui.inside(mouseX, mouseY, tx1, ty, tx2, ty + 20);
            tabHover[i] = Ui.approach(tabHover[i], (hover || i == tab) ? 1f : 0f, dt, 14f);
            if (i == tab) {
                Ui.roundedFill(c, tx1, ty, tx2, ty + 20, 4, Ui.alpha(theme.accent2, 0.45f * ease));
                c.fill(tx1, ty + 4, tx1 + 2, ty + 16, Ui.alpha(theme.accent, ease));
            } else if (tabHover[i] > 0.02f) {
                Ui.roundedFill(c, tx1, ty, tx2, ty + 20, 4, Ui.alpha(0x30FFFFFF, tabHover[i] * ease));
            }
            int tc = Ui.lerp(0xFFA0A4B0, 0xFFFFFFFF, tabHover[i]);
            c.drawText(font, TABS[i], tx1 + 10, ty + 6, Ui.alpha(tc, ease), false);
            final int idx = i;
            hits.add(new Hit(tx1, ty, tx2, ty + 20, () -> {
                tab = idx;
                lastTab = idx;
            }));
        }

        c.drawText(font, "R-Shift: cerrar", x + 10, y + h - 14, Ui.alpha(0xFF7C7F8A, ease), false);

        // Contenido
        int cx = x + sideW + 12;
        int cw = w - sideW - 24;
        int cy = y + 12;
        c.drawText(font, TABS[tab], cx, cy, Ui.alpha(0xFFFFFFFF, ease), true);
        c.fill(cx, cy + 12, cx + cw, cy + 13, Ui.alpha(0x30FFFFFF, ease));
        cy += 20;

        VisualsConfig cfg = VisualsConfig.get();
        Ctx k = new Ctx(c, font, theme, ease, mouseX, mouseY, cx, cw);
        switch (tab) {
            case 0 -> {
                cy = toggleRow(k, cy, "HUD", cfg.hud, () -> cfg.hud = !cfg.hud);
                cy = toggleRow(k, cy, "Barra superior", cfg.topBar, () -> cfg.topBar = !cfg.topBar);
                cy = toggleRow(k, cy, "Coordenadas", cfg.coords, () -> cfg.coords = !cfg.coords);
                cy = toggleRow(k, cy, "Teclas y CPS", cfg.keystrokes, () -> {
                    boolean v = !cfg.keystrokes;
                    cfg.keystrokes = v;
                    cfg.cps = v;
                });
                cy = toggleRow(k, cy, "Spotify", cfg.music, () -> cfg.music = !cfg.music);
                cy = toggleRow(k, cy, "Panel de modulos", cfg.modulesPanel, () -> cfg.modulesPanel = !cfg.modulesPanel);
                cy = toggleRow(k, cy, "Panel de teclas", cfg.keybindsPanel, () -> cfg.keybindsPanel = !cfg.keybindsPanel);
                cy = toggleRow(k, cy, "Particulas", cfg.particles, () -> cfg.particles = !cfg.particles);
                cy = toggleRow(k, cy, "Manos negras", cfg.hands, () -> cfg.hands = !cfg.hands);
                sliderRow(k, cy, "Tamano HUD", (cfg.hudScale - 1) / 2f, "x" + cfg.hudScale,
                        f -> cfg.hudScale = 1 + Math.round(f * 2f));
            }
            case 1 -> {
                cy = toggleRow(k, cy, "Fullbright", cfg.fullbright, () -> cfg.fullbright = !cfg.fullbright);
                cy = sliderRow(k, cy, "Brillo fullbright", (cfg.fullbrightLevel - 2f) / 13f,
                        Math.round(cfg.fullbrightLevel) + "", f -> cfg.fullbrightLevel = 2f + Math.round(f * 13f));
                cy = toggleRow(k, cy, "Quitar lluvia", cfg.noRain, () -> cfg.noRain = !cfg.noRain);
                cy = toggleRow(k, cy, "Lineas del cielo", cfg.skyLines, () -> cfg.skyLines = !cfg.skyLines);
                cy = toggleRow(k, cy, "Logo en el cielo", cfg.skyLogo, () -> cfg.skyLogo = !cfg.skyLogo);
                cy = sliderRow(k, cy, "Tamano logo", cfg.logoSize / 100f, cfg.logoSize + "%",
                        f -> cfg.logoSize = Math.round(f * 100f));
                cy = sliderRow(k, cy, "Intensidad cielo", cfg.skyStrength / 100f, cfg.skyStrength + "%",
                        f -> cfg.skyStrength = Math.round(f * 100f));
                c.drawText(font, "Cielo", cx, cy + 3, Ui.alpha(0xFFA0A4B0, ease), false);
                cy += 14;
                int chipX = cx;
                for (SkyTheme st : SkyTheme.values()) {
                    int cwid = font.getWidth(st.id) + 12;
                    if (chipX + cwid > cx + cw) {
                        chipX = cx;
                        cy += 18;
                    }
                    boolean sel = cfg.sky == st;
                    boolean hov = Ui.inside(mouseX, mouseY, chipX, cy, chipX + cwid, cy + 15);
                    Ui.roundedFill(c, chipX, cy, chipX + cwid, cy + 15, 4,
                            Ui.alpha(sel ? theme.accent2 : (hov ? 0x50FFFFFF : 0x30FFFFFF), ease));
                    c.drawText(font, st.id, chipX + 6, cy + 4, Ui.alpha(sel ? 0xFFFFFFFF : 0xFFC0C4CE, ease), false);
                    hits.add(new Hit(chipX, cy, chipX + cwid, cy + 15, () -> cfg.sky = st));
                    chipX += cwid + 4;
                }
            }
            case 2 -> drawThemes(k, cfg, cy);
            default -> {
                c.drawText(font, "kujama09 visuals 1.1.0", cx, cy, Ui.alpha(0xFFFFFFFF, ease), false);
                c.drawText(font, "Comandos: /kv", cx, cy + 14, Ui.alpha(0xFFA0A4B0, ease), false);
                c.drawText(font, "Menu: Right Shift", cx, cy + 26, Ui.alpha(0xFFA0A4B0, ease), false);
                int by = cy + 48;
                boolean hov = Ui.inside(mouseX, mouseY, cx, by, cx + 110, by + 18);
                Ui.roundedFill(c, cx, by, cx + 110, by + 18, 4, Ui.alpha(hov ? 0xFFB5172B : 0xFF7A1020, ease));
                c.drawText(font, "Restaurar valores", cx + 9, by + 5, Ui.alpha(0xFFFFFFFF, ease), false);
                hits.add(new Hit(cx, by, cx + 110, by + 18, cfg::resetToDefaults));
            }
        }
    }

    private record Ctx(DrawContext c, TextRenderer font, Theme theme, float ease,
                       int mx, int my, int x, int w) {
    }

    private int toggleRow(Ctx k, int y, String label, boolean on, Runnable action) {
        DrawContext c = k.c;
        int rowH = 16;
        boolean hov = Ui.inside(k.mx, k.my, k.x, y, k.x + k.w, y + rowH);
        if (hov) {
            Ui.roundedFill(c, k.x - 3, y - 1, k.x + k.w, y + rowH - 1, 3, Ui.alpha(0x20FFFFFF, k.ease));
        }
        c.drawText(k.font, label, k.x, y + 4, Ui.alpha(0xFFE6E8EE, k.ease), false);
        int sw = 24;
        int sx = k.x + k.w - sw - 4;
        Ui.roundedFill(c, sx, y + 2, sx + sw, y + 12, 5,
                Ui.alpha(on ? k.theme.accent2 : 0xFF3A3C46, k.ease));
        int knob = on ? sx + sw - 9 : sx + 1;
        Ui.roundedFill(c, knob, y + 3, knob + 8, y + 11, 4, Ui.alpha(on ? 0xFFFFFFFF : 0xFF9A9CA8, k.ease));
        hits.add(new Hit(k.x, y, k.x + k.w, y + rowH, () -> {
            action.run();
            VisualsConfig.save();
        }));
        return y + rowH;
    }

    private int sliderRow(Ctx k, int y, String label, float frac, String valueText, Consumer<Float> setter) {
        DrawContext c = k.c;
        c.drawText(k.font, label, k.x, y + 4, Ui.alpha(0xFFE6E8EE, k.ease), false);
        c.drawText(k.font, valueText, k.x + k.w - k.font.getWidth(valueText) - 4, y + 4,
                Ui.alpha(k.theme.accent, k.ease), false);
        int sx1 = k.x;
        int sx2 = k.x + k.w - 4;
        int sy = y + 16;
        Ui.roundedFill(c, sx1, sy, sx2, sy + 4, 2, Ui.alpha(0xFF2A2C34, k.ease));
        int fx = sx1 + Math.round((sx2 - sx1) * Math.max(0f, Math.min(1f, frac)));
        Ui.roundedFill(c, sx1, sy, Math.max(sx1 + 2, fx), sy + 4, 2, Ui.alpha(k.theme.accent2, k.ease));
        Ui.roundedFill(c, fx - 3, sy - 2, fx + 3, sy + 6, 3, Ui.alpha(0xFFFFFFFF, k.ease));
        sliders.add(new SliderHit(sx1, sy - 4, sx2, sy + 8, setter));
        return y + 26;
    }

    private void drawThemes(Ctx k, VisualsConfig cfg, int top) {
        DrawContext c = k.c;
        Theme[] all = Theme.values();
        int cols = 4;
        int gap = 5;
        int cardW = (k.w - gap * (cols - 1)) / cols;
        int cardH = 34;
        for (int i = 0; i < all.length; i++) {
            Theme t = all[i];
            int cx = k.x + (i % cols) * (cardW + gap);
            int cy = top + (i / cols) * (cardH + gap);
            boolean sel = t.id.equals(cfg.theme);
            boolean hov = Ui.inside(k.mx, k.my, cx, cy, cx + cardW, cy + cardH);
            Ui.panel(c, cx, cy, cx + cardW, cy + cardH, 4,
                    Ui.alpha(hov ? 0xFF22222C : 0xFF16161E, k.ease),
                    Ui.alpha(sel ? t.accent : 0x30FFFFFF, k.ease));
            c.fillGradient(cx + 4, cy + 4, cx + cardW - 4, cy + 14, Ui.alpha(t.accent, k.ease),
                    Ui.alpha(t.accent2, k.ease));
            String name = k.font.trimToWidth(t.label, cardW - 6);
            c.drawText(k.font, name, cx + (cardW - k.font.getWidth(name)) / 2, cy + 20,
                    Ui.alpha(sel ? 0xFFFFFFFF : 0xFFB8BCC8, k.ease), false);
            hits.add(new Hit(cx, cy, cx + cardW, cy + cardH, () -> {
                cfg.theme = t.id;
                VisualsConfig.save();
            }));
        }
    }
}
