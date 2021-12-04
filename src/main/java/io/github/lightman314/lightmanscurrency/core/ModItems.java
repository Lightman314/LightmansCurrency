package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.Lists;

import io.github.lightman314.lightmanscurrency.items.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
	
	private static final List<Item> ITEMS = Lists.newArrayList();
	
	public static final Item COIN_COPPER = register("coin_copper", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_IRON = register("coin_iron", new CoinItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_GOLD = register("coin_gold", new CoinItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_EMERALD = register("coin_emerald", new CoinItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_DIAMOND = register("coin_diamond", new CoinItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_NETHERITE = register("coin_netherite", new CoinItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP).isImmuneToFire()));
	
	public static final Item TRADING_CORE = register("trading_core", new Item(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	public static final Item TICKET = register("ticket", new TicketItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	public static final Item TICKET_MASTER = register("master_ticket", new TicketItem(new Item.Properties().group(LightmansCurrency.COIN_GROUP).rarity(Rarity.RARE).maxStackSize(1)));
	public static final Item TICKET_STUB = register("ticket_stub", new Item(new Item.Properties().group(LightmansCurrency.COIN_GROUP)));
	
	public static final WalletItem WALLET_COPPER = register("wallet_copper", new WalletItem(false, false, 6, "wallet_copper", new Item.Properties().group(LightmansCurrency.COIN_GROUP).maxStackSize(1)));
	public static final WalletItem WALLET_IRON = register("wallet_iron", new WalletItem(false, false, 12, "wallet_iron" , new Item.Properties().group(LightmansCurrency.COIN_GROUP).maxStackSize(1)));
	public static final WalletItem WALLET_GOLD = register("wallet_gold", new WalletItem(true, false, 18, "wallet_gold", new Item.Properties().group(LightmansCurrency.COIN_GROUP).maxStackSize(1)));
	public static final WalletItem WALLET_EMERALD = register("wallet_emerald", new WalletItem(true, true, 24, "wallet_emerald", new Item.Properties().group(LightmansCurrency.COIN_GROUP).maxStackSize(1)));
	public static final WalletItem WALLET_DIAMOND = register("wallet_diamond", new WalletItem(true, true, 30, "wallet_diamond", new Item.Properties().group(LightmansCurrency.COIN_GROUP).maxStackSize(1)));
	public static final WalletItem WALLET_NETHERITE = register("wallet_netherite", new WalletItem(true, true, 36, "wallet_netherite", new Item.Properties().group(LightmansCurrency.COIN_GROUP).maxStackSize(1).isImmuneToFire()));
	
	public static final Item PORTABLE_TERMINAL = register("portable_terminal", new PortableTerminalItem(new Item.Properties().group(LightmansCurrency.MACHINE_GROUP).maxStackSize(1)));
	
	//Hidden item
	public static final Item FREEZER_DOOR = register("freezer_door", new Item(new Item.Properties()));
	
	private static <T extends Item> T register(String name, T item)
	{
		item.setRegistryName(name);
		ITEMS.add(item);
		return item;
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
