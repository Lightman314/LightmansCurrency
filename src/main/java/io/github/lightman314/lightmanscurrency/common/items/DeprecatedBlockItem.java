package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IDeprecatedBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class DeprecatedBlockItem extends BlockItem {

    public DeprecatedBlockItem(Block block) { this(block, new Item.Properties()); }
    public DeprecatedBlockItem(Block block, Properties properties) { super(block, properties); }

    @Nonnull
    @Override
    public Block getBlock() {
        if(this.getBlockRaw() instanceof IDeprecatedBlock block)
            return block.replacementBlock();
        return this.getBlockRaw();
    }

    @Nonnull
    private Block getBlockRaw() { return super.getBlock(); }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int slot, boolean selected) {
        if(super.getBlock() instanceof IDeprecatedBlock block && entity instanceof Player p)
        {
            ItemStack newStack = new ItemStack(block.replacementBlock(), stack.getCount());
            newStack.setTag(stack.getTag().copy());
            p.getInventory().setItem(slot, newStack);
        }
    }
}
