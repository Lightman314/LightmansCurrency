package io.github.lightman314.lightmanscurrency.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {

    @Invoker("validateComponents")
    static DataResult<ItemStack> validateComponents(DataComponentMap components) { throw new NotImplementedException(); }

}