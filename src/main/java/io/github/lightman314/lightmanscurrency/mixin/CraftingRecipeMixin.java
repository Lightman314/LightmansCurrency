package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {ShapedRecipe.class,ShapelessRecipe.class})
public class CraftingRecipeMixin {

    @WrapMethod(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;")
    private ItemStack assemble(CraftingInput input,Operation<ItemStack> original) {
        ItemStack stack = original.call(input);
        for(ItemStack s : input.items()) {
            if(ItemHelper.copyComponent(LCDataComponents.STORED_TRADER,s,stack))
                return stack;
        }
        return stack;
    }

}
