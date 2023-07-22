package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.items.*;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	
	/**
	 * Placeholder function to force the static class loading
	 */
	public static void init() { }
	
	//Register the items
	static {
		//Coins
		COIN_COPPER = ModRegistries.ITEMS.register("coin_copper", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		COIN_IRON = ModRegistries.ITEMS.register("coin_iron", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		COIN_GOLD = ModRegistries.ITEMS.register("coin_gold", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		COIN_EMERALD = ModRegistries.ITEMS.register("coin_emerald", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		COIN_DIAMOND = ModRegistries.ITEMS.register("coin_diamond", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		COIN_NETHERITE = ModRegistries.ITEMS.register("coin_netherite", () -> new CoinItem(new Item.Properties().tab(LightmansCurrency.COIN_GROUP).fireResistant()));
		
		//Misc
		TRADING_CORE = ModRegistries.ITEMS.register("trading_core", () -> new Item(new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		
		//Ticket
		TICKET = ModRegistries.ITEMS.register("ticket", () -> new TicketItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		TICKET_PASS = ModRegistries.ITEMS.register("ticket_pass", () -> new TicketItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP).rarity(Rarity.UNCOMMON)));
		TICKET_MASTER = ModRegistries.ITEMS.register("master_ticket", () -> new TicketItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP).rarity(Rarity.RARE).stacksTo(1)));
		TICKET_STUB = ModRegistries.ITEMS.register("ticket_stub", () -> new Item(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		//Wallets
		WALLET_COPPER = ModRegistries.ITEMS.register("wallet_copper", () -> new WalletItem(0, 6, "wallet_copper", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		WALLET_IRON = ModRegistries.ITEMS.register("wallet_iron", () -> new WalletItem(1, 12, "wallet_iron", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		WALLET_GOLD = ModRegistries.ITEMS.register("wallet_gold", () -> new WalletItem(2, 18, "wallet_gold", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		WALLET_EMERALD = ModRegistries.ITEMS.register("wallet_emerald", () -> new WalletItem(3, 24, "wallet_emerald", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		WALLET_DIAMOND = ModRegistries.ITEMS.register("wallet_diamond", () -> new WalletItem(4, 30, "wallet_diamond", new Item.Properties().tab(LightmansCurrency.COIN_GROUP)));
		WALLET_NETHERITE = ModRegistries.ITEMS.register("wallet_netherite", () -> new WalletItem(5, 36, "wallet_netherite", new Item.Properties().tab(LightmansCurrency.COIN_GROUP).fireResistant()));
		
		//Portable Blocks
		PORTABLE_TERMINAL = ModRegistries.ITEMS.register("portable_terminal", () -> new PortableTerminalItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		PORTABLE_GEM_TERMINAL = ModRegistries.ITEMS.register("portable_gem_terminal", () -> new PortableTerminalItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		PORTABLE_ATM = ModRegistries.ITEMS.register("portable_atm", () -> new PortableATMItem(new Item.Properties().tab(LightmansCurrency.MACHINE_GROUP)));
		
		//Item Capacity Upgrades
		ITEM_CAPACITY_UPGRADE_1 = ModRegistries.ITEMS.register("item_capacity_upgrade_1", () -> new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity1, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		ITEM_CAPACITY_UPGRADE_2 = ModRegistries.ITEMS.register("item_capacity_upgrade_2", () -> new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity2, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		ITEM_CAPACITY_UPGRADE_3 = ModRegistries.ITEMS.register("item_capacity_upgrade_3", () -> new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, Config.SERVER.itemUpgradeCapacity3, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		
		//Speed Upgrades
		SPEED_UPGRADE_1 = ModRegistries.ITEMS.register("speed_upgrade_1", () -> new SpeedUpgradeItem(4, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		SPEED_UPGRADE_2 = ModRegistries.ITEMS.register("speed_upgrade_2", () -> new SpeedUpgradeItem(8, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		SPEED_UPGRADE_3 = ModRegistries.ITEMS.register("speed_upgrade_3", () -> new SpeedUpgradeItem(12, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		SPEED_UPGRADE_4 = ModRegistries.ITEMS.register("speed_upgrade_4", () -> new SpeedUpgradeItem(16, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		SPEED_UPGRADE_5 = ModRegistries.ITEMS.register("speed_upgrade_5", () -> new SpeedUpgradeItem(20, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		
		//Network Upgrade
		NETWORK_UPGRADE = ModRegistries.ITEMS.register("network_upgrade", () -> new UpgradeItem.Simple(UpgradeType.NETWORK, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		
		//Hopper Upgrade
		HOPPER_UPGRADE = ModRegistries.ITEMS.register("hopper_upgrade", () -> new UpgradeItem.Simple(UpgradeType.HOPPER, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));

		//Coin Chest Upgrades
		COIN_CHEST_EXCHANGE_UPGRADE = ModRegistries.ITEMS.register("coin_chest_exchange_upgrade", () -> new UpgradeItem.Simple(UpgradeType.COIN_CHEST_EXCHANGE, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		COIN_CHEST_BANK_UPGRADE = ModRegistries.ITEMS.register("coin_chest_bank_upgrade", () -> new UpgradeItem.Simple(UpgradeType.COIN_CHEST_BANK, new Item.Properties()/*.tab(LightmansCurrency.UPGRADE_GROUP)*/));
		COIN_CHEST_MAGNET_UPGRADE_1 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_1", () -> new MagnetUpgradeItem(Config.SERVER.coinChestMagnetRange1, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		COIN_CHEST_MAGNET_UPGRADE_2 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_2", () -> new MagnetUpgradeItem(Config.SERVER.coinChestMagnetRange2, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		COIN_CHEST_MAGNET_UPGRADE_3 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_3", () -> new MagnetUpgradeItem(Config.SERVER.coinChestMagnetRange3, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		COIN_CHEST_MAGNET_UPGRADE_4 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_4", () -> new MagnetUpgradeItem(Config.SERVER.coinChestMagnetRange4, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));
		COIN_CHEST_SECURITY_UPGRADE = ModRegistries.ITEMS.register("coin_chest_security_upgrade", () -> new UpgradeItem.Simple(UpgradeType.COIN_CHEST_SECURITY, new Item.Properties().tab(LightmansCurrency.UPGRADE_GROUP)));

	}
	
	//Hold the items for public access
	public static final RegistryObject<Item> COIN_COPPER;
	public static final RegistryObject<Item> COIN_IRON;
	public static final RegistryObject<Item> COIN_GOLD;
	public static final RegistryObject<Item> COIN_EMERALD;
	public static final RegistryObject<Item> COIN_DIAMOND;
	public static final RegistryObject<Item> COIN_NETHERITE;
	
	public static final RegistryObject<Item> TRADING_CORE;
	public static final RegistryObject<Item> TICKET;
	public static final RegistryObject<Item> TICKET_PASS;
	public static final RegistryObject<Item> TICKET_MASTER;
	public static final RegistryObject<Item> TICKET_STUB;
	
	public static final RegistryObject<WalletItem> WALLET_COPPER;
	public static final RegistryObject<WalletItem> WALLET_IRON;
	public static final RegistryObject<WalletItem> WALLET_GOLD;
	public static final RegistryObject<WalletItem> WALLET_EMERALD;
	public static final RegistryObject<WalletItem> WALLET_DIAMOND;
	public static final RegistryObject<WalletItem> WALLET_NETHERITE;
	
	public static final RegistryObject<Item> PORTABLE_TERMINAL;
	public static final RegistryObject<Item> PORTABLE_GEM_TERMINAL;
	public static final RegistryObject<Item> PORTABLE_ATM;
	
	public static final RegistryObject<Item> ITEM_CAPACITY_UPGRADE_1;
	public static final RegistryObject<Item> ITEM_CAPACITY_UPGRADE_2;
	public static final RegistryObject<Item> ITEM_CAPACITY_UPGRADE_3;
	
	public static final RegistryObject<Item> SPEED_UPGRADE_1;
	public static final RegistryObject<Item> SPEED_UPGRADE_2;
	public static final RegistryObject<Item> SPEED_UPGRADE_3;
	public static final RegistryObject<Item> SPEED_UPGRADE_4;
	public static final RegistryObject<Item> SPEED_UPGRADE_5;
	
	public static final RegistryObject<Item> NETWORK_UPGRADE;
	
	public static final RegistryObject<Item> HOPPER_UPGRADE;

	public static final RegistryObject<Item> COIN_CHEST_EXCHANGE_UPGRADE;
	public static final RegistryObject<Item> COIN_CHEST_BANK_UPGRADE;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_1;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_2;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_3;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_4;
	public static final RegistryObject<Item> COIN_CHEST_SECURITY_UPGRADE;

}