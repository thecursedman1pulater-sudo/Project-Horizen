package dev.projecthorizen.render;

import dev.projecthorizen.HorizenConfig;
import dev.projecthorizen.Palettes;
import dev.projecthorizen.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Screen-space sky: stars, aurora curtains and shooting stars, projected from the camera yaw/pitch. */
public final class SkyOverlay {
    private SkyOverlay() {}

    private static final Random RNG = new Random(1337);
    private static final List<Shooter> SHOOTERS = new ArrayList<>();
    private static float[] sAz, sEl, sPh;
    private static int sCount = -1;
    private static long last = System.nanoTime();

    // view state
    private static float yaw, pitch, vfov, hfov;
    private static int w, h;

    private static final class Shooter { float az, el, vAz, vEl, age, life; }

    public static void prepare(float tp) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        w = mc.getWindow().getScaledWidth();
        h = mc.getWindow().getScaledHeight();
        yaw = mc.player.getYaw(tp);
        pitch = mc.player.getPitch(tp);
        vfov = Zoom.currentFov();
        double aspect = (double) w / (double) h;
        hfov = (float) Math.toDegrees(2.0 * Math.atan(Math.tan(Math.toRadians(vfov) / 2.0) * aspect));
    }

    public static float vx(float az) { return w / 2f + wrap(az - yaw) / hfov * w; }
    public static float vy(float el) { return h / 2f - (el + pitch) / vfov * h; }

    private static float wrap(float a) {
        a %= 360f;
        if (a > 180f) a -= 360f;
        if (a < -180f) a += 360f;
        return a;
    }

    public static float nightFactor(ClientWorld world) {
        long t = world.getTimeOfDay() % 24000L;
        if (t < 0) t += 24000L;
        if (t < 12000) return 0f;
        if (t < 13500) return (t - 12000) / 1500f;
        if (t < 22500) return 1f;
        return 1f - (t - 22500) / 1500f;
    }

    public static float duskFactor(ClientWorld world) {
        long t = world.getTimeOfDay() % 24000L;
        if (t < 0) t += 24000L;
        float d1 = 1f - Math.min(1f, Math.abs(t - 12500f) / 2500f);
        float d2 = 1f - Math.min(1f, Math.min(Math.abs(t - 23000f), Math.abs(t + 1000f)) / 2500f);
        return Math.max(d1, d2);
    }

    public static void render(DrawContext ctx, float tp) {
        HorizenConfig c = HorizenConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        ClientPlayerEntity p = mc.player;
        if (world == null || p == null) return;

        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1e9f);
        last = now;

        if (world.getRegistryKey() != World.OVERWORLD) return;
        if (c.hideIndoors && !world.isSkyVisible(p.getBlockPos())) return;

        float rainF = 1f - world.getRainGradient(tp) * 0.9f;
        float night = nightFactor(world);
        float time = (float) ((now / 1_000_000L) % 100_000_000L) / 1000f;
        float horizonY = vy(0f);

        // ---- Stars ----
        if (c.starsEnabled && night > 0.02f) {
            if (sCount != c.starCount) {
                sCount = c.starCount;
                sAz = new float[sCount]; sEl = new float[sCount]; sPh = new float[sCount];
                for (int i = 0; i < sCount; i++) {
                    sAz[i] = RNG.nextFloat() * 360f;
                    sEl[i] = (float) Math.toDegrees(Math.asin(RNG.nextFloat()));
                    sPh[i] = RNG.nextFloat() * 6.28f;
                }
            }
            float nf = night * rainF;
            for (int i = 0; i < sCount; i++) {
                int x = (int) vx(sAz[i]);
                int y = (int) vy(sEl[i]);
                if (x < 0 || x >= w || y < 0 || y >= horizonY - 1) continue;
                float tw = 0.45f + 0.55f * (MathHelper.sin(time * 2.2f + sPh[i]) * 0.5f + 0.5f);
                int a = (int) (230 * tw * nf);
                int size = (sPh[i] > 5.4f) ? 2 : 1;
                ctx.fill(x, y, x + size, y + size, Palettes.argb(a, 0xFFFFFF));
            }
        }

        // ---- Aurora ----
        float auroraF = c.auroraNightOnly ? night : 1f;
        if (c.auroraEnabled && auroraF > 0.02f) {
            float sp = c.auroraSpeed;
            for (int L = 0; L < c.auroraLayers; L++) {
                float layerFade = 1f - L * 0.18f;
                for (int x = 0; x < w; x += 3) {
                    float az = yaw + (x - w / 2f) / w * hfov;
                    float azw = ((az % 360f) + 360f) % 360f;
                    float r = azw * 0.0174533f;

                    float n = MathHelper.sin(r * 3f + time * sp * 0.35f + L * 1.7f) * 0.5f
                            + MathHelper.sin(r * 7f - time * sp * 0.23f + L * 3.1f) * 0.3f
                            + MathHelper.sin(r * 2f + time * sp * 0.11f + L) * 0.45f;
                    float presence = MathHelper.clamp((n + 0.6f) / 1.1f, 0f, 1f);
                    float base = c.auroraBase + L * 4f + n * 5f;
                    float height = c.auroraHeight * (0.7f + 0.3f * MathHelper.sin(r * 5f + time * sp * 0.4f + L));
                    float ray = 0.55f + 0.45f * MathHelper.sin(r * 70f + time * sp * 0.9f + L * 2f);

                    int alpha = (int) (170f * c.auroraIntensity * auroraF * rainF * presence * ray * layerFade);
                    if (alpha < 3) continue;

                    int yBot = (int) Math.min(vy(base), horizonY);
                    int yTop = (int) vy(base + height);
                    if (yBot < 0 || yTop > h || yBot <= yTop) continue;

                    int prim, sec;
                    if (c.auroraPalette == 5) {
                        float hue = (((azw / 360f) * 2f + time * 0.02f) % 1f + 1f) % 1f;
                        prim = MathHelper.hsvToRgb(hue, 0.7f, 1f);
                        sec = MathHelper.hsvToRgb((hue + 0.25f) % 1f, 0.8f, 1f);
                    } else {
                        prim = Palettes.AURORA[c.auroraPalette][0];
                        sec = Palettes.AURORA[c.auroraPalette][1];
                    }
                    ctx.fillGradient(x, yTop, x + 3, yBot, Palettes.argb(0, sec), Palettes.argb(alpha, prim));
                    int yGlow = (int) Math.min(horizonY, yBot + 6);
                    if (yGlow > yBot) {
                        ctx.fillGradient(x, yBot, x + 3, yGlow, Palettes.argb((int) (alpha * 0.6f), prim), Palettes.argb(0, prim));
                    }
                }
            }
        }

        // ---- Shooting stars ----
        if (c.shootingEnabled) {
            if (night > 0.4f && rainF > 0.5f && SHOOTERS.size() < 6
                    && RNG.nextFloat() < c.shootingRate / 60f * dt) {
                Shooter s = new Shooter();
                s.az = yaw + (RNG.nextFloat() - 0.5f) * 110f;
                s.el = 20f + RNG.nextFloat() * 50f;
                float dir = RNG.nextBoolean() ? 1f : -1f;
                s.vAz = dir * (50f + RNG.nextFloat() * 60f) * c.shootingSpeed;
                s.vEl = -(25f + RNG.nextFloat() * 35f) * c.shootingSpeed;
                s.life = 0.6f + RNG.nextFloat() * 0.6f;
                SHOOTERS.add(s);
            }
            int col = Palettes.COLORS[c.shootingColor];
            Iterator<Shooter> it = SHOOTERS.iterator();
            while (it.hasNext()) {
                Shooter s = it.next();
                s.age += dt;
                if (s.age > s.life) { it.remove(); continue; }
                s.az += s.vAz * dt;
                s.el += s.vEl * dt;
                float fade = MathHelper.sin((float) Math.PI * s.age / s.life);
                for (int i = 0; i < c.trailLength; i++) {
                    float k = i * 0.012f;
                    int x = (int) vx(s.az - s.vAz * k);
                    int y = (int) vy(s.el - s.vEl * k);
                    if (x < 0 || x >= w || y < 0 || y >= horizonY) continue;
                    float a = fade * (1f - (float) i / c.trailLength);
                    int size = i < 2 ? 2 : 1;
                    ctx.fill(x, y, x + size, y + size, Palettes.argb((int) (255 * a * Math.min(1f, night + 0.3f)), col));
                }
            }
        }
    }
}
