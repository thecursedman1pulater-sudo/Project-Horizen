package dev.projecthorizen;

import dev.projecthorizen.gui.HorizenScreen;
import dev.projecthorizen.render.Effects;
import dev.projecthorizen.render.HudInfo;
import dev.projecthorizen.render.SkyOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizenClient implements ClientModInitializer {
    public static final String MOD_ID = "projecthorizen";
    public static final Logger LOGGER = LoggerFactory.getLogger("ProjectHorizen");

    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        HorizenConfig.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.projecthorizen.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.projecthorizen.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) {
                if (mc.currentScreen == null) {
                    mc.setScreen(new HorizenScreen(null));
                }
            }
        });

        // Sky + screen effects (drawn behind crosshair/hotbar)
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR,
                Identifier.of(MOD_ID, "visuals"), (ctx, tc) -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.world == null || mc.player == null) return;
                    float tp = tc.getTickProgress(false);
                    SkyOverlay.prepare(tp);
                    Effects.renderBack(ctx, tp);
                    SkyOverlay.render(ctx, tp);
                    Effects.renderFront(ctx, tp);
                });

        // Custom crosshair replaces the vanilla one when enabled
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (ctx, tc) -> {
            if (HorizenConfig.get().crossEnabled) {
                Effects.crosshair(ctx);
            } else {
                original.render(ctx, tc);
            }
        });

        // FPS / ping / armor
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "info"), (ctx, tc) -> HudInfo.render(ctx));

        // Menu button on the title screen and pause screen (Right Shift works in-game)
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen || screen instanceof GameMenuScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Horizen"),
                        b -> client.setScreen(new HorizenScreen(screen)))
                        .dimensions(w - 78, 6, 70, 20).build());
            }
        });

        LOGGER.info("Project Horizen loaded. Press Right Shift in game for the menu.");
    }
}
