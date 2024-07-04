package io.github.lightman314.lightmanscurrency.common.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class BlockEntityBlockHelper {

	public static final ResourceLocation ITEM_TRADER_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "item_trader");
	public static final ResourceLocation ARMOR_TRADER_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "armor_trader");
	public static final ResourceLocation TICKET_KIOSK_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "ticket_trader");
	public static final ResourceLocation FREEZER_TRADER_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "freezer_trader");
	public static final ResourceLocation BOOKSHELF_TRADER_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "bookshelf_trader");
	public static final ResourceLocation SLOT_MACHINE_TRADER_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "slot_machine_trader");
	public static final ResourceLocation CAPABILITY_INTERFACE_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "capability_interface");
	public static final ResourceLocation AUCTION_STAND_TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "auction_stand");

	private static final Map<ResourceLocation,List<Supplier<Block>>> blockList = new HashMap<>();
	
	public static Block[] getBlocksForBlockEntity(@Nonnull ResourceLocation beType) {
		List<Block> result = new ArrayList<>();
		for(Supplier<Block> blockSource : blockList.getOrDefault(beType, new ArrayList<>()))
		{
			try {
				Block b = blockSource.get();
				if(b != null)
					result.add(b);
			} catch(Throwable ignored) {}
		}
		return result.toArray(new Block[0]);
	}

	public static Block[] getBlocksForBlockEntities(@Nonnull ResourceLocation... beTypes)
	{
		List<Block> result = new ArrayList<>();
		for(ResourceLocation type : beTypes)
		{
			for(Supplier<Block> blockSource : blockList.getOrDefault(type,new ArrayList<>()))
			{
				try {
					Block b = blockSource.get();
					if(b != null)
						result.add(b);
				} catch (Throwable ignore) {}
			}
		}
		return result.toArray(new Block[0]);
	}

	public static <T extends Block> void addBlockToBlockEntity(ResourceLocation beType, Supplier<T> blockSource) { addBlocksToBlockEntity(beType, Lists.newArrayList(blockSource)); }
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, RegistryObjectBundle<T,?> blocks) { addBlocksToBlockEntity(beType, blocks.getSupplier()); }
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, RegistryObjectBiBundle<T,?,?> blocks) { addBlocksToBlockEntity(beType, blocks.getSupplier()); }
	@SafeVarargs
	public static <T extends Block> void addBlocksToBlockEntity(ResourceLocation beType, Supplier<T>... blocks) {
		List<Supplier<Block>> blockSources = new ArrayList<>();
		for(Supplier<T> block : blocks)
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
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.DISPLAY_CASE, ModBlocks.ITEM_NETWORK_TRADER_1, ModBlocks.ITEM_NETWORK_TRADER_2, ModBlocks.ITEM_NETWORK_TRADER_3, ModBlocks.ITEM_NETWORK_TRADER_4);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.SHELF);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.SHELF_2x2);
		addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.CARD_DISPLAY);

		//Freezer Blocks
		addBlocksToBlockEntity(FREEZER_TRADER_TYPE, ModBlocks.FREEZER);

		//Specialty Traders
		addBlocksToBlockEntity(ARMOR_TRADER_TYPE, ModBlocks.ARMOR_DISPLAY);
		addBlocksToBlockEntity(TICKET_KIOSK_TYPE, ModBlocks.TICKET_KIOSK);
		addBlocksToBlockEntity(BOOKSHELF_TRADER_TYPE, ModBlocks.BOOKSHELF_TRADER);

		//Slot Machine Blocks
		addBlocksToBlockEntity(SLOT_MACHINE_TRADER_TYPE, ModBlocks.SLOT_MACHINE);

		//Multi-block Capability Interface Blocks
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.VENDING_MACHINE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.FREEZER);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.ARMOR_DISPLAY);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.TICKET_KIOSK);
		addBlocksToBlockEntity(CAPABILITY_INTERFACE_TYPE, ModBlocks.SLOT_MACHINE);

		//External Trader Blocks
		addBlocksToBlockEntity(AUCTION_STAND_TYPE, ModBlocks.AUCTION_STAND);

		
	}
	
}
