package io.github.lightman314.lightmanscurrency;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * @deprecated No longer used as @ObjectHolder is being used to store block/items
 */
@Deprecated
public class BlockItemPair implements ItemLike{
	public final Block block;
	public final Item item;
	
	public BlockItemPair(Block block, Item item)
	{
		this.block = block;
		this.item = item;
	}

	@Override
	public Item asItem() {
		return this.item;
	}
	
}
