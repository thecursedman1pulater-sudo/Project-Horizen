package dev.projecthorizen.render;

import dev.projecthorizen.HorizenConfig;
import dev.projecthorizen.Palettes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/** FPS, ping and armor readout. */
public final class HudInfo {
    private HudInfo() {}

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void render(DrawContext ctx) {
        HorizenConfig c = HorizenConfig.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;

        List<String> lines = new ArrayList<>();
        if (c.fpsEnabled) lines.add("FPS: " + mc.getCurrentFps());
        if (c.pingEnabled) {
            String ping = "local";
            if (!mc.isInSingleplayer() && mc.getNetworkHandler() != null) {
                PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
                ping = e != null ? e.getLatency() + "ms" : "--";
            }
            lines.add("Ping: " + ping);
        }

        List<ItemStack> armor = new ArrayList<>();
        if (c.armorEnabled) {
            for (EquipmentSlot slot : ARMOR) {
                ItemStack s = p.getEquippedStack(slot);
                if (!s.isEmpty()) armor.add(s);
            }
        }
        if (lines.isEmpty() && armor.isEmpty()) return;

        int lh = 10, pad = 3, rowH = 18;
        int textW = 0;
        for (String l : lines) textW = Math.max(textW, mc.textRenderer.getWidth(l));
        int armorW = armor.isEmpty() ? 0 : 18 + (c.armorDurability ? mc.textRenderer.getWidth("100%") : 0);
        int bw = Math.max(textW, armorW) + pad * 2;
        int bh = lines.size() * lh + armor.size() * rowH + pad * 2 + ((!lines.isEmpty() && !armor.isEmpty()) ? 2 : 0);

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int m = 4;
        int x = (c.hudCorner == 1 || c.hudCorner == 3) ? sw - bw - m : m;
        int y = (c.hudCorner >= 2) ? sh - bh - m : m;

        if (c.hudBackground) ctx.fill(x, y, x + bw, y + bh, 0x66000000);

        int col = 0xFF000000 | Palettes.COLORS[c.hudColor];
        int cy = y + pad;
        for (String l : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, l, x + pad, cy, col);
            cy += lh;
        }
        if (!lines.isEmpty() && !armor.isEmpty()) cy += 2;
        for (ItemStack s : armor) {
            ctx.drawItem(s, x + pad, cy);
            if (c.armorDurability && s.isDamageable()) {
                int max = s.getMaxDamage();
                int pct = Math.max(0, 100 * (max - s.getDamage()) / Math.max(1, max));
                int dc = 0xFF000000 | MathHelper.hsvToRgb(pct / 100f * 0.33f, 1f, 1f);
                ctx.drawTextWithShadow(mc.textRenderer, pct + "%", x + pad + 18, cy + 5, dc);
            }
            cy += rowH;
        }
    }
}
