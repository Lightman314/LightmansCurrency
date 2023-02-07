package io.github.lightman314.lightmanscurrency.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.core.groups.RegistryObjectBundle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityBlockHelper {

	public static final ResourceLocation ITEM_TRADER_TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	public static final ResourceLocation CAPABILITY_INTERFACE_TYPE = new ResourceLocation(LightmansCurrency.MODID, "capability_interface");
	public static final ResourceLocation AUCTION_STAND_TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_stand");

	private static final Map<ResourceLocation,List<Supplier<Block>>> blockList = new HashMap<>();
	
	public static Block[] getBlocksForBlockEntity(ResourceLocation beType) {
		List<Block> result = new ArrayList<>();
		for(Supplier<Block> blockSource : blockList.getOrDefault(beType, new ArrayList<>()))
		{
			try {
				Block b = blockSource.get();
				if(b != null)
					result.add(b);
			} catch(Throwable ignored) {}
		}
		return result.toArray(new Block[result.size()]);
	}
	
	public static void addBlockToBlockEntity(ResourceLocation beType, RegistryObject<Block> block) { addBlockToBlockEntity(beType, block::get); }
	public static void addBlockToBlockEntity(ResourceLocation beType, Supplier<Block> blockSource) { addBlocksToBlockEntity(beType, Lists.newArrayList(blockSource)); }
	public static void addBlocksToBlockEntity(ResourceLocation beType, RegistryObjectBundle<Block,?> blocks) { addBlocksToBlockEntity(beType, blocks.getSupplier()); }
	public static void addBlocksToBlockEntity(ResourceLocation beType, RegistryObjectBiBundle<Block,?,?> blocks) { addBlocksToBlockEntity(beType, blocks.getSupplier()); }
	@SafeVarargs
	public static void addBlocksToBlockEntity(ResourceLocation beType, RegistryObject<Block>... blocks) {
		List<Supplier<Block>> blockSources = new ArrayList<>();
		for(RegistryObject<Block> block : blocks)
		{
			if(block != null)
				blockSources.add(block::get);
		}
		addBlocksToBlockEntity(beType, blockSources);
	}
	public static void addBlocksToBlockEntity(ResourceLocation beType, List<Supplier<Block>> blockSources)
	{
		List<Supplier<Block>> list = blockList.getOrDefault(beType, new ArrayList<>());
		list.addAll(blockSources);
		blockList.put(beType, list);
	}
	
	static {
		
		//Item Trader Blocks
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.DISPLAY_CASE, ModBlocks.ITEM_NETWORK_TRADER_1, ModBlocks.ITEM_NETWORK_TRADER_2, ModBlocks.ITEM_NETWORK_TRADER_3, ModBlocks.ITEM_NETWORK_TRADER_4);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.SHELF);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.CARD_DISPLAY);
		
		//Multi-block Capability Interface Blocks
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.VENDING_MACHINE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.ARMOR_DISPLAY, ModBlocks.FREEZER);

		//External Trader Blocks
		addBlocksToBlockEntity(AUCTION_STAND_TYPE, ModBlocks.AUCTION_STAND);
		
	}
	
}