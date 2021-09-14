package io.github.lightman314.lightmanscurrency;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;

public class BlockItemPair implements IItemProvider{
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
