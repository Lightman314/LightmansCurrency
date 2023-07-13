package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IDeprecatedBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class DeprecatedBlockItem extends BlockItem {

    public DeprecatedBlockItem(Block block) { this(block, new Item.Properties()); }
    public DeprecatedBlockItem(Block block, Properties properties) { super(block, properties); }

    @Override
    protected BlockState getPlacementState(@Nonnull BlockPlaceContext context) {
        IDeprecatedBlock block = this.getDeprecatedBlock();
        if(block != null)
        {
            //Copy of vanilla getPlacementState, but using the replacement block instead.
            BlockState state = block.replacementBlock().getStateForPlacement(context);
            return state != null && this.canPlace(context, state) ? state : null;
        }
        return super.getPlacementState(context);
    }

    private IDeprecatedBlock getDeprecatedBlock()
    {
        if(this.getBlock() instanceof IDeprecatedBlock block)
            return block;
        return null;
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int slot, boolean selected) {
        IDeprecatedBlock block = this.getDeprecatedBlock();
        if(block != null && entity instanceof Player p)
        {
            ItemStack newStack = new ItemStack(block.replacementBlock(), stack.getCount());
            CompoundTag tag = stack.getTag();
            if(tag != null)
                newStack.setTag(tag.copy());
            p.getInventory().setItem(slot, newStack);
        }
    }
}
