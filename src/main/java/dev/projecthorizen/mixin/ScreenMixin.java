package dev.projecthorizen.mixin;

import dev.projecthorizen.HorizenConfig;
import dev.projecthorizen.render.MenuBackground;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Paints the sunset + rain scene over the vanilla panorama on every menu screen outside a world. */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("TAIL"), require = 0)
    private void horizen$menuBackground(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HorizenConfig.get().menuBackground) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) return;
        Screen self = (Screen) (Object) this;
        MenuBackground.render(ctx, self.width, self.height);
    }
}
