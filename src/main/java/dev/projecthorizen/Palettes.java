package dev.projecthorizen;

public final class Palettes {
    private Palettes() {}

    public static final String[] COLOR_NAMES = {"White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Black"};
    public static final int[] COLORS = {0xFFFFFF, 0xFF4040, 0xFF9A2E, 0xFFE14D, 0x4DFF6A, 0x3DE6FF, 0x4D7CFF, 0xA855FF, 0xFF6EC7, 0x1A1A1A};
    // must match the "when" names in the generated item definitions
    public static final String[] KNIFE_IDS = {"white", "red", "orange", "yellow", "green", "cyan", "blue", "purple", "pink", "black"};

    public static final String[] AURORA_NAMES = {"Green", "Purple", "Blue", "Pink", "Red", "Rainbow"};
    public static final int[][] AURORA = {
            {0x3CFF8C, 0x2A9DFF}, {0xB45CFF, 0x4C6BFF}, {0x38C8FF, 0x5A5CFF},
            {0xFF6AD5, 0x9B5CFF}, {0xFF4D5E, 0xFF9A3C}, {0, 0}
    };

    public static final String[] GLOW_NAMES = {"Sunset", "Pink", "Gold", "Teal"};
    public static final int[] GLOW = {0xFF7A3C, 0xFF6EA8, 0xFFC24D, 0x3CD7C8};

    public static final String[] CORNERS = {"Top Left", "Top Right", "Bottom Left", "Bottom Right"};
    public static final String[] PARTICLES = {"Snow", "Petals", "Leaves", "Sparkles"};
    public static final int[] PARTICLE_COLORS = {0xFFFFFF, 0xFFB7D5, 0xD9A441, 0xFFF3A0};
    public static final String[] CROSS = {"Cross", "Dot", "Square"};

    public static int argb(int a, int rgb) {
        a = Math.max(0, Math.min(255, a));
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
