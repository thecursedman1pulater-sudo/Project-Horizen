package dev.projecthorizen.gui;

import dev.projecthorizen.HorizenConfig;
import dev.projecthorizen.Palettes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** The Project Horizen settings menu (Right Shift). Changes apply live. */
public class HorizenScreen extends Screen {
    private static final String[] TABS = {"Sky", "Stars", "HUD", "FX 1", "FX 2", "Cross", "Knife", "Hand", "Zoom"};
    private static final int PW = 380, CW = 182;
    private static int tab = 0;

    private final Screen parent;
    private int idx;
    private int panelBottom = 200;
    private String note = null;

    public HorizenScreen(Screen parent) {
        super(Text.literal("Project Horizen"));
        this.parent = parent;
    }

    private int colX(int i) { return width / 2 - PW / 2 + 4 + (i % 2) * (CW + 4); }
    private int rowY(int i) { return 48 + (i / 2) * 24; }

    @Override
    protected void init() {
        HorizenConfig c = HorizenConfig.get();
        int left = width / 2 - PW / 2;
        int tw = (PW - 8) / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            final int ti = i;
            ButtonWidget b = ButtonWidget.builder(Text.literal(TABS[i]), x -> { tab = ti; clearAndInit(); })
                    .dimensions(left + 4 + i * tw, 22, tw - 2, 18).build();
            b.active = (i != tab);
            addDrawableChild(b);
        }

        idx = 0;
        note = null;
        switch (tab) {
            case 0 -> {
                toggle("Aurora", () -> c.auroraEnabled, v -> c.auroraEnabled = v);
                toggle("Night only", () -> c.auroraNightOnly, v -> c.auroraNightOnly = v);
                toggle("Hide when indoors", () -> c.hideIndoors, v -> c.hideIndoors = v);
                cycle("Colors", Palettes.AURORA_NAMES, () -> c.auroraPalette, v -> c.auroraPalette = v);
                slider("Layers", 1, 4, true, () -> c.auroraLayers, v -> c.auroraLayers = (int) v);
                slider("Brightness", 0.1, 1.0, false, () -> c.auroraIntensity, v -> c.auroraIntensity = (float) v);
                slider("Speed", 0.1, 3.0, false, () -> c.auroraSpeed, v -> c.auroraSpeed = (float) v);
                slider("Height", 10, 60, true, () -> c.auroraHeight, v -> c.auroraHeight = (float) v);
                slider("Base angle", 5, 45, true, () -> c.auroraBase, v -> c.auroraBase = (float) v);
                note = "The aurora shows at night. Tip: set /time set night to preview it.";
            }
            case 1 -> {
                toggle("Stars", () -> c.starsEnabled, v -> c.starsEnabled = v);
                slider("Star count", 50, 600, true, () -> c.starCount, v -> c.starCount = (int) v);
                toggle("Shooting stars", () -> c.shootingEnabled, v -> c.shootingEnabled = v);
                slider("Per minute", 1, 40, true, () -> c.shootingRate, v -> c.shootingRate = (int) v);
                slider("Speed", 0.3, 2.5, false, () -> c.shootingSpeed, v -> c.shootingSpeed = (float) v);
                slider("Trail length", 6, 30, true, () -> c.trailLength, v -> c.trailLength = (int) v);
                cycle("Color", Palettes.COLOR_NAMES, () -> c.shootingColor, v -> c.shootingColor = v);
            }
            case 2 -> {
                toggle("FPS", () -> c.fpsEnabled, v -> c.fpsEnabled = v);
                toggle("Ping", () -> c.pingEnabled, v -> c.pingEnabled = v);
                toggle("Armor", () -> c.armorEnabled, v -> c.armorEnabled = v);
                toggle("Armor durability", () -> c.armorDurability, v -> c.armorDurability = v);
                toggle("Background", () -> c.hudBackground, v -> c.hudBackground = v);
                cycle("Corner", Palettes.CORNERS, () -> c.hudCorner, v -> c.hudCorner = v);
                cycle("Text color", Palettes.COLOR_NAMES, () -> c.hudColor, v -> c.hudColor = v);
            }
            case 3 -> {
                toggle("Fireflies", () -> c.firefliesEnabled, v -> c.firefliesEnabled = v);
                slider("Fireflies amount", 5, 120, true, () -> c.firefliesCount, v -> c.firefliesCount = (int) v);
                toggle("Falling particles", () -> c.particlesEnabled, v -> c.particlesEnabled = v);
                cycle("Particle type", Palettes.PARTICLES, () -> c.particleType, v -> c.particleType = v);
                slider("Particle amount", 10, 250, true, () -> c.particleCount, v -> c.particleCount = (int) v);
                toggle("Light rain overlay", () -> c.rainEnabled, v -> c.rainEnabled = v);
                slider("Rain amount", 0.1, 1.0, false, () -> c.rainIntensity, v -> c.rainIntensity = (float) v);
                toggle("Sunset horizon glow", () -> c.glowEnabled, v -> c.glowEnabled = v);
                slider("Glow strength", 0.1, 1.0, false, () -> c.glowStrength, v -> c.glowStrength = (float) v);
                cycle("Glow color", Palettes.GLOW_NAMES, () -> c.glowColor, v -> c.glowColor = v);
            }
            case 4 -> {
                toggle("Color tint", () -> c.tintEnabled, v -> c.tintEnabled = v);
                cycle("Tint color", Palettes.COLOR_NAMES, () -> c.tintColor, v -> c.tintColor = v);
                slider("Tint strength", 0.05, 1.0, false, () -> c.tintStrength, v -> c.tintStrength = (float) v);
                toggle("Low health vignette", () -> c.lowHealthEnabled, v -> c.lowHealthEnabled = v);
                toggle("Sprint speed lines", () -> c.speedLinesEnabled, v -> c.speedLinesEnabled = v);
                toggle("Cinematic bars", () -> c.letterboxEnabled, v -> c.letterboxEnabled = v);
                slider("Bar size", 0.03, 0.2, false, () -> c.letterboxSize, v -> c.letterboxSize = (float) v);
                toggle("Sunset main menu", () -> c.menuBackground, v -> c.menuBackground = v);
            }
            case 5 -> {
                toggle("Custom crosshair", () -> c.crossEnabled, v -> c.crossEnabled = v);
                cycle("Shape", Palettes.CROSS, () -> c.crossShape, v -> c.crossShape = v);
                slider("Size", 2, 14, true, () -> c.crossSize, v -> c.crossSize = (int) v);
                slider("Gap", 0, 8, true, () -> c.crossGap, v -> c.crossGap = (int) v);
                slider("Thickness", 1, 4, true, () -> c.crossThick, v -> c.crossThick = (int) v);
                cycle("Color", Palettes.COLOR_NAMES, () -> c.crossColor, v -> c.crossColor = v);
                toggle("Outline", () -> c.crossOutline, v -> c.crossOutline = v);
            }
            case 6 -> {
                toggle("Butterfly knives", () -> c.knifeEnabled, v -> c.knifeEnabled = v);
                cycle("Knife color", Palettes.COLOR_NAMES, () -> c.knifeColor, v -> c.knifeColor = v);
                note = "Turns every sword (wood to netherite) into a butterfly knife.";
            }
            case 7 -> {
                toggle("Hand positioner", () -> c.handEnabled, v -> c.handEnabled = v);
                slider("Scale", 0.3, 1.6, false, () -> c.handScale, v -> c.handScale = (float) v);
                slider("Move X", -1.0, 1.0, false, () -> c.handX, v -> c.handX = (float) v);
                slider("Move Y", -1.0, 1.0, false, () -> c.handY, v -> c.handY = (float) v);
                slider("Move Z", -1.0, 1.0, false, () -> c.handZ, v -> c.handZ = (float) v);
                slider("Rotate X", -90, 90, true, () -> c.handRX, v -> c.handRX = (float) v);
                slider("Rotate Y", -90, 90, true, () -> c.handRY, v -> c.handRY = (float) v);
                slider("Rotate Z", -90, 90, true, () -> c.handRZ, v -> c.handRZ = (float) v);
                int x = colX(idx), y = rowY(idx);
                idx++;
                addDrawableChild(ButtonWidget.builder(Text.literal("Reset hand"), b -> { c.resetHand(); clearAndInit(); })
                        .dimensions(x, y, CW, 20).build());
            }
            default -> {
                toggle("Zoom", () -> c.zoomEnabled, v -> c.zoomEnabled = v);
                slider("Zoom amount", 1.5, 10.0, false, () -> c.zoomFactor, v -> c.zoomFactor = (float) v);
                toggle("Smooth zoom", () -> c.zoomSmooth, v -> c.zoomSmooth = v);
                slider("FOV offset", -30, 30, true, () -> c.fovOffset, v -> c.fovOffset = (int) v);
                note = "Hold C to zoom (rebind in Options > Controls > Project Horizen).";
            }
        }

        int rows = (idx + 1) / 2;
        int doneY = 48 + rows * 24 + 4;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 60, doneY, 120, 20).build());
        panelBottom = doneY + 28 + (note != null ? 12 : 0);
    }

    // ---- widget helpers ----
    private void toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
        int x = colX(idx), y = rowY(idx);
        idx++;
        addDrawableChild(ButtonWidget.builder(onOff(label, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(onOff(label, get.getAsBoolean()));
        }).dimensions(x, y, CW, 20).build());
    }

    private static Text onOff(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private void cycle(String label, String[] options, IntSupplier get, IntConsumer set) {
        int x = colX(idx), y = rowY(idx);
        idx++;
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + options[get.getAsInt() % options.length]), b -> {
            set.accept((get.getAsInt() + 1) % options.length);
            b.setMessage(Text.literal(label + ": " + options[get.getAsInt() % options.length]));
        }).dimensions(x, y, CW, 20).build());
    }

    private void slider(String label, double min, double max, boolean integer, DoubleSupplier get, DoubleConsumer set) {
        int x = colX(idx), y = rowY(idx);
        idx++;
        addDrawableChild(new Slider(x, y, CW, 20, label, min, max, integer, get, set));
    }

    private static class Slider extends SliderWidget {
        private final String label;
        private final double min, max;
        private final boolean integer;
        private final DoubleConsumer set;

        Slider(int x, int y, int w, int h, String label, double min, double max, boolean integer,
               DoubleSupplier get, DoubleConsumer set) {
            super(x, y, w, h, Text.empty(), (get.getAsDouble() - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.set = set;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            double v = min + (max - min) * this.value;
            String s = integer ? String.valueOf((int) Math.round(v)) : String.format("%.2f", v);
            setMessage(Text.literal(label + ": " + s));
        }

        @Override
        protected void applyValue() {
            double v = min + (max - min) * this.value;
            if (integer) v = Math.round(v);
            set.accept(v);
        }
    }

    // ---- screen plumbing ----
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // In a world we skip the blur so you can watch changes live; on the title screen show the sunset.
        if (client != null && client.world == null) {
            super.renderBackground(ctx, mouseX, mouseY, delta);
        }
        int left = width / 2 - PW / 2;
        ctx.fill(left, 6, left + PW, panelBottom, 0xCC0C0C16);
        ctx.fill(left, 6, left + PW, 8, 0xFF8A6CFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Project Horizen", width / 2, 11, 0xFFFFFFFF);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        if (note != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, note, width / 2, panelBottom - 14, 0xFFBBBBCC);
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void removed() {
        HorizenConfig.save();
    }
}
