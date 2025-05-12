package io.github.lightman314.lightmanscurrency.common.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityBlockHelper {

	public static final ResourceLocation ITEM_TRADER_TYPE = VersionUtil.lcResource("item_trader");
	public static final ResourceLocation FREEZER_TRADER_TYPE = VersionUtil.lcResource("freezer_trader");
	public static final ResourceLocation BOOKSHELF_TRADER_TYPE = VersionUtil.lcResource("bookshelf_trader");
	public static final ResourceLocation SLOT_MACHINE_TRADER_TYPE = VersionUtil.lcResource("slot_machine_trader");
	public static final ResourceLocation CAPABILITY_INTERFACE_TYPE = VersionUtil.lcResource("capability_interface");
	public static final ResourceLocation AUCTION_STAND_TYPE = VersionUtil.lcResource("auction_stand");
	public static final ResourceLocation GACHA_MACHINE_TYPE = VersionUtil.lcResource("gacha_machine");

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
		return result.toArray(Block[]::new);
	}

	public static Block[] getVariantBlocks()
	{
		List<Block> blocks = new ArrayList<>();
		for(Block b : ForgeRegistries.BLOCKS)
		{
			if(b instanceof IVariantBlock)
				blocks.add(b);
		}
		return blocks.toArray(Block[]::new);
	}

	public static <T extends Block> void addBlockToBlockEntity(ResourceLocation beType, Supplier<T> blockSource) { addBlocksToBlockEntity(beType, Lists.newArrayList(blockSource)); }
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, RegistryObjectBundle<T,?> blocks) { addBlocksToBlockEntity(beType, blocks.getSupplier()); }
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, RegistryObjectBiBundle<T,?,?> blocks) { addBlocksToBlockEntity(beType, blocks.getSupplier()); }
	@SafeVarargs
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, RegistryObject<T>... blocks) {
		List<Supplier<Block>> blockSources = new ArrayList<>();
		for(RegistryObject<T> block : blocks)
		{
			if(block != null)
				blockSources.add(block::get);
		}
		addBlocksToBlockEntity(beType, blockSources);
	}
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, List<Supplier<T>> blockSources)
	{
		List<Supplier<Block>> list = blockList.getOrDefault(beType, new ArrayList<>());
		for(Supplier<? extends Block> s : blockSources)
			list.add(s::get);
		blockList.put(beType, list);
	}
	
	static {
		
		//Item Trader Blocks
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.ITEM_NETWORK_TRADER_1, ModBlocks.ITEM_NETWORK_TRADER_2, ModBlocks.ITEM_NETWORK_TRADER_3, ModBlocks.ITEM_NETWORK_TRADER_4);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.DISPLAY_CASE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.SHELF);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.SHELF_2x2);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.CARD_DISPLAY);

		//Freezer Blocks
		addBlocksToBlockEntity(FREEZER_TRADER_TYPE, ModBlocks.FREEZER);

		//Bookshelf Blocks
		addBlocksToBlockEntity(BOOKSHELF_TRADER_TYPE, ModBlocks.BOOKSHELF_TRADER);

		//Slot Machine Blocks
		addBlocksToBlockEntity(SLOT_MACHINE_TRADER_TYPE, ModBlocks.SLOT_MACHINE);

		//Gacha Machine Blocks
		addBlocksToBlockEntity(GACHA_MACHINE_TYPE,ModBlocks.GACHA_MACHINE);

		//Multi-block Capability Interface Blocks
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.VENDING_MACHINE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.FREEZER);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.ARMOR_DISPLAY);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.TICKET_KIOSK);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.SLOT_MACHINE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.ATM);

		//External Trader Blocks
		addBlocksToBlockEntity(AUCTION_STAND_TYPE, ModBlocks.AUCTION_STAND);

		
	}
	
}
