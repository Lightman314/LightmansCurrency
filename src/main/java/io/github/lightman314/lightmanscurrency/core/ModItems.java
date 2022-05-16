package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.items.*;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(LightmansCurrency.MODID)
public class ModItems {
	
	//Register the items
	public static void init()
	{
		//Coins
		ModRegistries.ITEMS.register("coin_copper", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("coin_iron", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("coin_gold", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("coin_emerald", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("coin_diamond", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("coin_netherite", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP).fireResistant()));
		
		//Misc
		ModRegistries.ITEMS.register("trading_core", () -> new Item(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		
		//Ticket
		ModRegistries.ITEMS.register("ticket", () -> new TicketItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("master_ticket", () -> new TicketItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP).rarity(Rarity.RARE).stacksTo(1)));
		ModRegistries.ITEMS.register("ticket_stub", () -> new Item(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		
		//Wallets
		ModRegistries.ITEMS.register("wallet_copper", () -> new WalletItem(0, 6, "wallet_copper", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("wallet_iron", () -> new WalletItem(1, 12, "wallet_iron", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("wallet_gold", () -> new WalletItem(2, 18, "wallet_gold", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("wallet_emerald", () -> new WalletItem(3, 24, "wallet_emerald", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("wallet_diamond", () -> new WalletItem(4, 30, "wallet_diamond", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		ModRegistries.ITEMS.register("wallet_netherite", () -> new WalletItem(5, 36, "wallet_netherite", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).fireResistant()));
		
		//Portable Blocks
		ModRegistries.ITEMS.register("portable_terminal", () -> new PortableTerminalItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("portable_atm", () -> new PortableATMItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		//Item Capacity Upgrades
		ModRegistries.ITEMS.register("item_capacity_upgrade_1", () -> new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity1::get, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("item_capacity_upgrade_2", () -> new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity1::get, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("item_capacity_upgrade_3", () -> new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity1::get, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		//Speed Upgrades
		ModRegistries.ITEMS.register("speed_upgrade_1", () -> new SpeedUpgradeItem(4, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("speed_upgrade_2", () -> new SpeedUpgradeItem(8, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("speed_upgrade_3", () -> new SpeedUpgradeItem(12, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("speed_upgrade_4", () -> new SpeedUpgradeItem(16, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		ModRegistries.ITEMS.register("speed_upgrade_5", () -> new SpeedUpgradeItem(20, new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		//Freezer Door(s)
		ModRegistries.ITEMS.register("freezer_door", () -> new Item(new Item.Properties()));
		
	}
	
	//Hold the items for public access
	public static final Item COIN_COPPER = null;
	public static final Item COIN_IRON = null;
	public static final Item COIN_GOLD = null;
	public static final Item COIN_EMERALD = null;
	public static final Item COIN_DIAMOND = null;
	public static final Item COIN_NETHERITE = null;
	
	public static final Item TRADING_CORE = null;
	public static final Item TICKET = null;
	@ObjectHolder("master_ticket")
	public static final Item TICKET_MASTER = null;
	public static final Item TICKET_STUB = null;
	
	public static final WalletItem WALLET_COPPER = null;
	public static final WalletItem WALLET_IRON = null;
	public static final WalletItem WALLET_GOLD = null;
	public static final WalletItem WALLET_EMERALD = null;
	public static final WalletItem WALLET_DIAMOND = null;
	public static final WalletItem WALLET_NETHERITE = null;
	
	public static final Item PORTABLE_TERMINAL = null;
	public static final Item PORTABLE_ATM = null;
	
	public static final Item ITEM_CAPACITY_UPGRADE_1 = null;
	public static final Item ITEM_CAPACITY_UPGRADE_2 = null;
	public static final Item ITEM_CAPACITY_UPGRADE_3 = null;
	
	public static final Item SPEED_UPGRADE_1 = null;
	public static final Item SPEED_UPGRADE_2 = null;
	public static final Item SPEED_UPGRADE_3 = null;
	public static final Item SPEED_UPGRADE_4 = null;
	public static final Item SPEED_UPGRADE_5 = null;
	
	//Hidden item(s)
	public static final Item FREEZER_DOOR = null;
	
}
