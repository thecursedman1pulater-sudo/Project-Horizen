package dev.projecthorizen.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

/** Sunset-over-the-sea main menu background with a light rain. */
public final class MenuBackground {
    private MenuBackground() {}

    private static final long START = System.nanoTime();

    private static float hash(int i, int k) {
        double s = Math.sin(i * 12.9898 + k * 78.233) * 43758.5453;
        return (float) (s - Math.floor(s));
    }

    private static void disc(DrawContext ctx, int cx, int cy, int r, int hy, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int y = cy + dy;
            if (y >= hy) break;
            if (y < 0) continue;
            int half = (int) Math.sqrt((double) r * r - (double) dy * dy);
            ctx.fill(cx - half, y, cx + half, y + 1, color);
        }
    }

    public static void render(DrawContext ctx, int w, int h) {
        float t = (System.nanoTime() - START) / 1e9f;
        int hy = (int) (h * 0.62f);

        // sky bands
        int[] stops = {0xFF0B1030, 0xFF2A1B5A, 0xFF7A2E7E, 0xFFE0566B, 0xFFFF9A52, 0xFFFFD27A};
        float[] frac = {0f, 0.25f, 0.45f, 0.65f, 0.85f, 1f};
        for (int i = 0; i < stops.length - 1; i++) {
            ctx.fillGradient(0, (int) (hy * frac[i]), w, (int) (hy * frac[i + 1]) + 1, stops[i], stops[i + 1]);
        }

        // faint stars in the upper sky
        for (int i = 0; i < 70; i++) {
            int x = (int) (hash(i, 1) * w);
            int y = (int) (hash(i, 2) * hy * 0.5f);
            float tw = 0.4f + 0.6f * (float) (Math.sin(t * 1.5f + i) * 0.5 + 0.5);
            int a = (int) (150 * tw * (1f - y / (hy * 0.5f)));
            ctx.fill(x, y, x + 1, y + 1, (Math.max(0, a) << 24) | 0xFFFFFF);
        }

        // sun slowly sinking toward the horizon
        int cx = (int) (w * 0.62f);
        int r = Math.max(24, h / 7);
        float prog = Math.min(1f, t / 150f);
        int cy = (int) (hy - r * 0.9f + prog * r * 0.75f);
        for (int k = 6; k >= 1; k--) {
            disc(ctx, cx, cy, (int) (r * (1f + k * 0.35f)), hy, ((7 - k) * 7 << 24) | 0xFFB347);
        }
        disc(ctx, cx, cy, r, hy, 0xFFFFD27A);

        // sea
        ctx.fillGradient(0, hy, w, h, 0xFF3A2147, 0xFF0B0B1E);
        for (int y = hy + 1; y < h; y += 2) {
            float d = y - hy;
            float shimmer = (float) (Math.sin(y * 0.6 + t * 2.0) * 0.5 + 0.5);
            int half = (int) ((r * 0.5f + d * 0.25f) * shimmer);
            int a = (int) (120 * (1f - d / (h - hy)) * shimmer);
            if (half > 0 && a > 0) ctx.fill(cx - half, y, cx + half, y + 1, (a << 24) | 0xFFC37A);
        }

        // hills on the horizon (lower in the middle so the sun shows)
        for (int layer = 0; layer < 2; layer++) {
            int col = layer == 0 ? 0xFF2A1238 : 0xFF120A22;
            float seed = layer * 3.7f + 1.3f;
            float amp = layer == 0 ? 26f : 40f;
            for (int x = 0; x < w; x += 2) {
                float n = (float) (Math.sin(x * 0.011 + seed) * 0.5 + Math.sin(x * 0.027 + seed * 2) * 0.3
                        + Math.sin(x * 0.063 + seed * 3) * 0.2) * 0.5f + 0.5f;
                float edge = Math.abs(x - cx) / (w / 2f);
                int ht = (int) (4 + n * amp * (0.15f + 0.85f * Math.min(1f, edge)));
                ctx.fill(x, hy - ht + layer * 6, x + 2, hy + 1, col);
            }
        }

        // horizon mist
        ctx.fillGradient(0, hy - 40, w, hy, 0x00FFC9A0, 0x33FFC9A0);

        // light rain
        ctx.fill(0, 0, w, h, 0x14101830);
        for (int i = 0; i < 140; i++) {
            float speed = 220f + hash(i, 3) * 160f;
            int x = (int) (hash(i, 4) * w);
            int y = (int) ((hash(i, 5) * h + t * speed) % h);
            int len = 5 + (int) (hash(i, 6) * 5);
            ctx.fill(x, y, x + 1, y + len, 0x55BFD4FF);
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        ctx.drawTextWithShadow(mc.textRenderer, "Project Horizen", 6, 6, 0xFFFFE3B0);
    }
}
