package dev.projecthorizen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class HorizenConfig {
    private static HorizenConfig INSTANCE = new HorizenConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- Sky ----
    public boolean auroraEnabled = true;
    public boolean auroraNightOnly = true;
    public boolean hideIndoors = true;
    public int auroraPalette = 0;
    public int auroraLayers = 3;
    public float auroraIntensity = 0.75f;
    public float auroraSpeed = 1.0f;
    public float auroraHeight = 35f;
    public float auroraBase = 18f;

    // ---- Stars ----
    public boolean starsEnabled = true;
    public int starCount = 250;
    public boolean shootingEnabled = true;
    public int shootingRate = 8;
    public float shootingSpeed = 1.0f;
    public int trailLength = 16;
    public int shootingColor = 0;

    // ---- HUD ----
    public boolean fpsEnabled = true;
    public boolean pingEnabled = true;
    public boolean armorEnabled = true;
    public boolean armorDurability = true;
    public boolean hudBackground = true;
    public int hudCorner = 0;
    public int hudColor = 0;

    // ---- Effects 1 ----
    public boolean firefliesEnabled = true;
    public int firefliesCount = 40;
    public boolean particlesEnabled = false;
    public int particleType = 0;
    public int particleCount = 60;
    public boolean rainEnabled = false;
    public float rainIntensity = 0.5f;
    public boolean glowEnabled = true;
    public float glowStrength = 0.8f;
    public int glowColor = 0;

    // ---- Effects 2 ----
    public boolean tintEnabled = false;
    public int tintColor = 6;
    public float tintStrength = 0.25f;
    public boolean lowHealthEnabled = true;
    public boolean speedLinesEnabled = false;
    public boolean letterboxEnabled = false;
    public float letterboxSize = 0.08f;
    public boolean menuBackground = true;

    // ---- Crosshair ----
    public boolean crossEnabled = true;
    public int crossShape = 0;
    public int crossSize = 5;
    public int crossGap = 2;
    public int crossThick = 1;
    public int crossColor = 5;
    public boolean crossOutline = true;

    // ---- Butterfly knife ----
    public boolean knifeEnabled = true;
    public int knifeColor = 1;

    // ---- Hand positioner ----
    public boolean handEnabled = false;
    public float handX = 0f, handY = 0f, handZ = 0f;
    public float handRX = 0f, handRY = 0f, handRZ = 0f;
    public float handScale = 1f;

    // ---- Zoom / FOV ----
    public boolean zoomEnabled = true;
    public float zoomFactor = 4f;
    public boolean zoomSmooth = true;
    public int fovOffset = 0;

    public void resetHand() {
        handX = handY = handZ = 0f;
        handRX = handRY = handRZ = 0f;
        handScale = 1f;
    }

    public static HorizenConfig get() { return INSTANCE; }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("projecthorizen.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p)) {
                    HorizenConfig c = GSON.fromJson(r, HorizenConfig.class);
                    if (c != null) INSTANCE = c;
                }
            }
        } catch (Exception e) {
            HorizenClient.LOGGER.warn("Could not load config, using defaults", e);
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            HorizenClient.LOGGER.warn("Could not save config", e);
        }
    }
}
