package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.lightman314.lightmanscurrency.common.blocks.MoneyBagBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    protected Entity lightmanscurrency$self() { return (Entity)(Object)this; }

    @WrapMethod(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;")
    private ItemEntity spawnAtLocation(ItemStack stack, float offsetY, Operation<ItemEntity> original)
    {
        if(this.lightmanscurrency$self() instanceof FallingBlockEntity fallingBlock && fallingBlock.blockData != null && fallingBlock.getBlockState().getBlock() instanceof MoneyBagBlock moneyBag && stack.getItem() == moneyBag.asItem() && !stack.isEmpty())
        {
            stack = moneyBag.getDropItem(stack,fallingBlock.blockData);
        }
        return original.call(stack,offsetY);
    }

}
