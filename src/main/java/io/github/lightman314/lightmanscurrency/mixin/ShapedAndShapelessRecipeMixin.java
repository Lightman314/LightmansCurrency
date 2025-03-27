package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.mixinsupport.RecipeMixinHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = { ShapedRecipe.class, ShapelessRecipe.class })
public class ShapedAndShapelessRecipeMixin {

    @Inject(at = @At("RETURN"),method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", cancellable = true)
    public void matches(CraftingContainer input, Level level, CallbackInfoReturnable<Boolean> ci) {
        //Only check if we should block the recipe if it's already confirmed to match
        //so that we don't inject code into a million other recipe checks that are already going to fail
        if(ci.getReturnValue() && RecipeMixinHelper.shouldBlockRecipe(input))
            ci.setReturnValue(false);
    }

}