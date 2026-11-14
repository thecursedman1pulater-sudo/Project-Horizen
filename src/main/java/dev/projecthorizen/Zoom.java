package dev.projecthorizen;

import net.minecraft.client.MinecraftClient;

public final class Zoom {
    private Zoom() {}

    private static float progress = 0f;
    private static float lastFov = 0f;
    private static long last = System.nanoTime();

    /** Called from the GameRenderer FOV hook. */
    public static float apply(float fov) {
        HorizenConfig c = HorizenConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1e9f);
        last = now;

        boolean on = c.zoomEnabled && HorizenClient.zoomKey != null
                && HorizenClient.zoomKey.isPressed() && mc.currentScreen == null;
        float target = on ? 1f : 0f;
        float k = c.zoomSmooth ? 1f - (float) Math.exp(-dt * 12f) : 1f;
        progress += (target - progress) * k;

        fov += c.fovOffset;
        if (progress > 0.001f) {
            double t = Math.tan(Math.toRadians(fov) / 2.0) / (1.0 + (c.zoomFactor - 1.0) * progress);
            fov = (float) Math.toDegrees(2.0 * Math.atan(t));
        }
        lastFov = fov;
        return fov;
    }

    /** Vertical FOV currently used for the world (falls back to the options value). */
    public static float currentFov() {
        if (lastFov > 1f) return lastFov;
        return (float) (int) MinecraftClient.getInstance().options.getFov().getValue();
    }
}
