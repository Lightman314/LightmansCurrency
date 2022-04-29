package io.github.lightman314.lightmanscurrency.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.items.*;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.RegistryEvent;

public class ModItems {
	
	private static final List<Item> ITEMS = Lists.newArrayList();
	
	public static final Item COIN_COPPER = register("coin_copper", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_IRON = register("coin_iron", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_GOLD = register("coin_gold", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_EMERALD = register("coin_emerald", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_DIAMOND = register("coin_diamond", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item COIN_NETHERITE = register("coin_netherite", new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP).fireResistant()));
	
	public static final Item TRADING_CORE = register("trading_core", new Item(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item TICKET = register("ticket", new TicketItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	public static final Item TICKET_MASTER = register("master_ticket", new TicketItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP).rarity(Rarity.RARE).stacksTo(1)));
	public static final Item TICKET_STUB = register("ticket_stub", new Item(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
	
	public static final WalletItem WALLET_COPPER = register("wallet_copper", new WalletItem(0, 6, "wallet_copper", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).stacksTo(1)));
	public static final WalletItem WALLET_IRON = register("wallet_iron", new WalletItem(1, 12, "wallet_iron" , new Item.Properties().tab(LightmansCurrency.COIN_GROUP).stacksTo(1)));
	public static final WalletItem WALLET_GOLD = register("wallet_gold", new WalletItem(2, 18, "wallet_gold", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).stacksTo(1)));
	public static final WalletItem WALLET_EMERALD = register("wallet_emerald", new WalletItem(3, 24, "wallet_emerald", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).stacksTo(1)));
	public static final WalletItem WALLET_DIAMOND = register("wallet_diamond", new WalletItem(4, 30, "wallet_diamond", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).stacksTo(1)));
	public static final WalletItem WALLET_NETHERITE = register("wallet_netherite", new WalletItem(5, 36, "wallet_netherite", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).stacksTo(1).fireResistant()));
	
	public static final Item PORTABLE_TERMINAL = register("portable_terminal", new PortableTerminalItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP).stacksTo(1)));
	public static final Item PORTABLE_ATM = register("portable_atm", new PortableATMItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP).stacksTo(1)));
	
	public static final Item ITEM_CAPACITY_UPGRADE_1 = register("item_capacity_upgrade_1", new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity1::get, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
	public static final Item ITEM_CAPACITY_UPGRADE_2 = register("item_capacity_upgrade_2", new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity2::get, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
	public static final Item ITEM_CAPACITY_UPGRADE_3 = register("item_capacity_upgrade_3", new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity3::get, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
	
	//Hidden item(s)
	public static final Item FREEZER_DOOR = register("freezer_door", new Item(new Item.Properties()));
	
	private static <T extends Item> T register(String name, T item)
	{
		item.setRegistryName(name);
		ITEMS.add(item);
		return item;
	}
	
	//@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		ITEMS.forEach(item -> event.getRegistry().register(item));
		ITEMS.clear();
	}
	
}
