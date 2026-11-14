package dev.projecthorizen.mixin;

import dev.projecthorizen.Zoom;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom + custom FOV. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void horizen$fov(CallbackInfoReturnable<Float> cir) {
        Float v = cir.getReturnValue();
        if (v != null) cir.setReturnValue(Zoom.apply(v));
    }
}
