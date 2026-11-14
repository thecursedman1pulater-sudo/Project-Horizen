package dev.projecthorizen.render;

import dev.projecthorizen.HorizenConfig;
import dev.projecthorizen.Palettes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/** Screen-space visual effects and the custom crosshair. */
public final class Effects {
    private Effects() {}

    private static final int MAX = 300;
    private static final float[] px = new float[MAX], py = new float[MAX], pv = new float[MAX], pw = new float[MAX];
    private static final float[] fx = new float[MAX], fy = new float[MAX], fp = new float[MAX];
    private static final float[] rx = new float[MAX], ry = new float[MAX], rv = new float[MAX];

    static {
        Random r = new Random(7);
        for (int i = 0; i < MAX; i++) {
            px[i] = r.nextFloat(); py[i] = r.nextFloat(); pv[i] = 0.6f + r.nextFloat() * 0.8f; pw[i] = r.nextFloat() * 6.28f;
            fx[i] = r.nextFloat(); fy[i] = r.nextFloat(); fp[i] = r.nextFloat() * 6.28f;
            rx[i] = r.nextFloat(); ry[i] = r.nextFloat(); rv[i] = 0.7f + r.nextFloat() * 0.6f;
        }
    }

    private static long last = System.nanoTime();
    private static float dt, time;

    private static void tick() {
        long n = System.nanoTime();
        dt = Math.min(0.1f, (n - last) / 1e9f);
        last = n;
        time = (float) ((n / 1_000_000L) % 100_000_000L) / 1000f;
    }

    /** Things that sit behind the sky: color tint and the horizon glow. */
    public static void renderBack(DrawContext ctx, float tp) {
        HorizenConfig c = HorizenConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null) return;
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        if (c.tintEnabled) {
            ctx.fill(0, 0, w, h, Palettes.argb((int) (c.tintStrength * 110f), Palettes.COLORS[c.tintColor]));
        }

        if (c.glowEnabled && world.getRegistryKey() == World.OVERWORLD) {
            float f = SkyOverlay.duskFactor(world) * (1f - world.getRainGradient(tp) * 0.7f);
            int hy = (int) SkyOverlay.vy(0f);
            if (f > 0.02f && hy > 0 && hy < h + 100) {
                int col = Palettes.GLOW[c.glowColor];
                int top = hy - (int) (h * 0.25f);
                int bottom = Math.min(hy, h);
                ctx.fillGradient(0, top, w, bottom,
                        Palettes.argb(0, col), Palettes.argb((int) (150f * c.glowStrength * f), col));
            }
        }
    }

    /** Overlay effects drawn over the sky. */
    public static void renderFront(DrawContext ctx, float tp) {
        tick();
        HorizenConfig c = HorizenConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        ClientPlayerEntity p = mc.player;
        if (world == null || p == null) return;
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        // Fireflies
        if (c.firefliesEnabled && world.getRegistryKey() == World.OVERWORLD) {
            float night = SkyOverlay.nightFactor(world);
            if (night > 0.15f) {
                int n = Math.min(MAX, c.firefliesCount);
                for (int i = 0; i < n; i++) {
                    fx[i] += MathHelper.sin(time * 0.5f + fp[i]) * dt * 0.02f;
                    fy[i] += MathHelper.cos(time * 0.4f + fp[i] * 1.3f) * dt * 0.015f;
                    if (fx[i] < 0f) fx[i] += 1f; if (fx[i] > 1f) fx[i] -= 1f;
                    if (fy[i] < 0.25f) fy[i] = 0.95f; if (fy[i] > 0.98f) fy[i] = 0.3f;
                    int x = (int) (fx[i] * w), y = (int) (fy[i] * h);
                    float pulse = 0.5f + 0.5f * MathHelper.sin(time * 2.4f + fp[i] * 3f);
                    int a = (int) (255 * pulse * night);
                    ctx.fill(x - 1, y - 1, x + 2, y + 2, Palettes.argb(a / 5, 0xCCFF55));
                    ctx.fill(x, y, x + 1, y + 1, Palettes.argb(a, 0xE8FF8A));
                }
            }
        }

        // Falling particles (snow / petals / leaves / sparkles)
        if (c.particlesEnabled) {
            int n = Math.min(MAX, c.particleCount);
            int col = Palettes.PARTICLE_COLORS[c.particleType];
            for (int i = 0; i < n; i++) {
                py[i] += (40f * pv[i] * dt) / h;
                px[i] += MathHelper.sin(time * 0.8f + pw[i]) * dt * 0.03f;
                if (py[i] > 1f) { py[i] = -0.02f; }
                if (px[i] < 0f) px[i] += 1f; if (px[i] > 1f) px[i] -= 1f;
                int x = (int) (px[i] * w), y = (int) (py[i] * h);
                if (c.particleType == 3) {
                    float tw = 0.4f + 0.6f * (MathHelper.sin(time * 4f + pw[i]) * 0.5f + 0.5f);
                    ctx.fill(x, y, x + 1, y + 1, Palettes.argb((int) (255 * tw), col));
                } else {
                    ctx.fill(x, y, x + 2, y + (c.particleType == 0 ? 2 : 3), Palettes.argb(200, col));
                }
            }
        }

        // Light rain overlay
        if (c.rainEnabled) {
            int n = Math.min(MAX, (int) (c.rainIntensity * MAX));
            for (int i = 0; i < n; i++) {
                ry[i] += (420f * rv[i] * dt) / h;
                if (ry[i] > 1f) { ry[i] = -0.05f; rx[i] = (rx[i] + 0.37f) % 1f; }
                int x = (int) (rx[i] * w), y = (int) (ry[i] * h);
                ctx.fill(x, y, x + 1, y + 6, Palettes.argb(80, 0xBFD4FF));
            }
        }

        // Speed lines while sprinting
        if (c.speedLinesEnabled && p.isSprinting()) {
            for (int i = 0; i < 18; i++) {
                int y = (int) (((i * 37 + time * 500f) % h + h) % h);
                int len = 14 + (i * 7) % 26;
                ctx.fill(0, y, len, y + 1, 0x66FFFFFF);
                ctx.fill(w - len, y, w, y + 1, 0x66FFFFFF);
            }
        }

        // Low health vignette
        if (c.lowHealthEnabled && p.getHealth() <= 6f && p.getHealth() > 0f) {
            float pulse = 0.6f + 0.4f * MathHelper.sin(time * 5f);
            for (int i = 0; i < 14; i++) {
                int a = (int) ((1f - i / 14f) * 110f * pulse);
                int col = Palettes.argb(a, 0xFF0000);
                ctx.fill(0, i * 4, w, i * 4 + 4, col);
                ctx.fill(0, h - i * 4 - 4, w, h - i * 4, col);
                ctx.fill(i * 4, 0, i * 4 + 4, h, col);
                ctx.fill(w - i * 4 - 4, 0, w - i * 4, h, col);
            }
        }

        // Cinematic bars
        if (c.letterboxEnabled) {
            int bar = (int) (h * c.letterboxSize);
            ctx.fill(0, 0, w, bar, 0xFF000000);
            ctx.fill(0, h - bar, w, h, 0xFF000000);
        }
    }

    private static void box(DrawContext ctx, int x1, int y1, int x2, int y2, int col, boolean outline) {
        if (outline) ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        ctx.fill(x1, y1, x2, y2, col);
    }

    public static void crosshair(DrawContext ctx) {
        HorizenConfig c = HorizenConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || !mc.options.getPerspective().isFirstPerson()) return;
        int cx = mc.getWindow().getScaledWidth() / 2;
        int cy = mc.getWindow().getScaledHeight() / 2;
        int s = c.crossSize, g = c.crossGap, t = Math.max(1, c.crossThick);
        int col = 0xFF000000 | Palettes.COLORS[c.crossColor];
        boolean o = c.crossOutline;
        int top = cy - t / 2, left = cx - t / 2;

        switch (c.crossShape) {
            case 0 -> {
                box(ctx, cx - g - s, top, cx - g, top + t, col, o);
                box(ctx, cx + g, top, cx + g + s, top + t, col, o);
                box(ctx, left, cy - g - s, left + t, cy - g, col, o);
                box(ctx, left, cy + g, left + t, cy + g + s, col, o);
            }
            case 1 -> {
                int d = Math.max(1, s / 3);
                box(ctx, cx - d, cy - d, cx + d, cy + d, col, o);
            }
            default -> {
                int r = s + g;
                box(ctx, cx - r, cy - r, cx + r, cy - r + t, col, o);
                box(ctx, cx - r, cy + r - t, cx + r, cy + r, col, o);
                box(ctx, cx - r, cy - r, cx - r + t, cy + r, col, o);
                box(ctx, cx + r - t, cy - r, cx + r, cy + r, col, o);
            }
        }
    }
}
