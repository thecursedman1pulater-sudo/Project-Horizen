package dev.projecthorizen.mixin;

import dev.projecthorizen.HorizenConfig;
import dev.projecthorizen.Palettes;
import net.minecraft.client.render.item.ItemModelManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * Tags every vanilla sword with a client-side custom_model_data string so the bundled
 * item definitions (assets/minecraft/items/*_sword.json) pick the butterfly knife model + color.
 */
@Mixin(ItemModelManager.class)
public abstract class ItemModelManagerMixin {
    @ModifyVariable(
            method = {"update", "clearAndUpdate", "updateForLiving", "updateForNonLivingEntity"},
            at = @At("HEAD"), argsOnly = true, require = 0)
    private ItemStack horizen$knife(ItemStack stack) {
        HorizenConfig c = HorizenConfig.get();
        if (!c.knifeEnabled || stack == null || stack.isEmpty()) return stack;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!"minecraft".equals(id.getNamespace()) || !id.getPath().endsWith("_sword")) return stack;
        if (stack.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) return stack;
        ItemStack copy = stack.copy();
        String color = Palettes.KNIFE_IDS[c.knifeColor % Palettes.KNIFE_IDS.length];
        copy.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(List.of(), List.of(), List.of(color), List.of()));
        return copy;
    }
}
