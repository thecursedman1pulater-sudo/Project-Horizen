package dev.projecthorizen.mixin;

import dev.projecthorizen.HorizenConfig;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hand positioner: offsets/rotates/scales the first-person hand and held item. */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    private static MatrixStack horizen$pending;

    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true, require = 0)
    private MatrixStack horizen$pushHand(MatrixStack m) {
        HorizenConfig c = HorizenConfig.get();
        if (c.handEnabled) {
            m.push();
            m.translate(c.handX, c.handY, c.handZ);
            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(c.handRX));
            m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.handRY));
            m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(c.handRZ));
            m.scale(c.handScale, c.handScale, c.handScale);
            horizen$pending = m;
        }
        return m;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"), require = 0)
    private void horizen$popHand(CallbackInfo ci) {
        if (horizen$pending != null) {
            horizen$pending.pop();
            horizen$pending = null;
        }
    }
}
