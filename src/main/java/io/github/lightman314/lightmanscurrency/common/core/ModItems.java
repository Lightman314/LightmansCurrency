package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.*;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
		COIN_COPPER = ModRegistries.ITEMS.register("coin_copper", () -> new Item(new Item.Properties()));
		COIN_IRON = ModRegistries.ITEMS.register("coin_iron", () -> new Item(new Item.Properties()));
		COIN_GOLD = ModRegistries.ITEMS.register("coin_gold", () -> new Item(new Item.Properties()));
		COIN_EMERALD = ModRegistries.ITEMS.register("coin_emerald", () -> new Item(new Item.Properties()));
		COIN_DIAMOND = ModRegistries.ITEMS.register("coin_diamond", () -> new Item(new Item.Properties()));
		COIN_NETHERITE = ModRegistries.ITEMS.register("coin_netherite", () -> new Item(new Item.Properties().fireResistant()));

		//Chocolate Coins
		COIN_CHOCOLATE_COPPER = ModRegistries.ITEMS.register("coin_chocolate_copper", () -> new ChocolateCoinItem(1f));
		COIN_CHOCOLATE_IRON = ModRegistries.ITEMS.register("coin_chocolate_iron", () -> new ChocolateCoinItem(
				new MobEffectInstance(MobEffects.DIG_SPEED, 600)
		));
		COIN_CHOCOLATE_GOLD = ModRegistries.ITEMS.register("coin_chocolate_gold", () -> new ChocolateCoinItem(
				new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800)
		));
		COIN_CHOCOLATE_EMERALD = ModRegistries.ITEMS.register("coin_chocolate_emerald", () -> new ChocolateCoinItem(
				new MobEffectInstance(MobEffects.LUCK, 1000)
		));
		COIN_CHOCOLATE_DIAMOND = ModRegistries.ITEMS.register("coin_chocolate_diamond", () -> new ChocolateCoinItem(
				new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200)
		));
		COIN_CHOCOLATE_NETHERITE = ModRegistries.ITEMS.register("coin_chocolate_netherite", () -> new ChocolateCoinItem(
				new Item.Properties().fireResistant(),
				new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400),
				new MobEffectInstance(MobEffects.ABSORPTION, 2400, 4),
				new MobEffectInstance(MobEffects.REGENERATION, 100, 1)
		));
		
		//Misc
		TRADING_CORE = ModRegistries.ITEMS.register("trading_core", () -> new Item(new Item.Properties()));
		
		//Ticket
		TICKET = ModRegistries.ITEMS.register("ticket", () -> new TicketItem(new Item.Properties()));
		TICKET_PASS = ModRegistries.ITEMS.register("ticket_pass", () -> new TicketItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
		TICKET_MASTER = ModRegistries.ITEMS.register("master_ticket", () -> new TicketItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
		TICKET_STUB = ModRegistries.ITEMS.register("ticket_stub", () -> new Item(new Item.Properties()));

		//Golden Ticket
		GOLDEN_TICKET = ModRegistries.ITEMS.register("golden_ticket", () -> new TicketItem(new Item.Properties()));
		GOLDEN_TICKET_PASS = ModRegistries.ITEMS.register("golden_ticket_pass", () -> new TicketItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
		GOLDEN_TICKET_MASTER = ModRegistries.ITEMS.register("golden_master_ticket", () -> new TicketItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
		GOLDEN_TICKET_STUB = ModRegistries.ITEMS.register("golden_ticket_stub", () -> new Item(new Item.Properties()));
		
		//Wallets
		WALLET_COPPER = ModRegistries.ITEMS.register("wallet_copper", () -> new WalletItem(0, 6, "wallet_copper", new Item.Properties()));
		WALLET_IRON = ModRegistries.ITEMS.register("wallet_iron", () -> new WalletItem(1, 12, "wallet_iron", new Item.Properties()));
		WALLET_GOLD = ModRegistries.ITEMS.register("wallet_gold", () -> new WalletItem(2, 18, "wallet_gold", new Item.Properties()));
		WALLET_EMERALD = ModRegistries.ITEMS.register("wallet_emerald", () -> new WalletItem(3, 24, "wallet_emerald", new Item.Properties()));
		WALLET_DIAMOND = ModRegistries.ITEMS.register("wallet_diamond", () -> new WalletItem(4, 30, "wallet_diamond", new Item.Properties()));
		WALLET_NETHERITE = ModRegistries.ITEMS.register("wallet_netherite", () -> new WalletItem(5, 36, "wallet_netherite", new Item.Properties().fireResistant()));
		
		//Portable Blocks
		PORTABLE_TERMINAL = ModRegistries.ITEMS.register("portable_terminal", () -> new PortableTerminalItem(new Item.Properties()));
		PORTABLE_GEM_TERMINAL = ModRegistries.ITEMS.register("portable_gem_terminal", () -> new PortableTerminalItem(new Item.Properties()));
		PORTABLE_ATM = ModRegistries.ITEMS.register("portable_atm", () -> new PortableATMItem(new Item.Properties()));
		
		//Item Capacity Upgrades
		ITEM_CAPACITY_UPGRADE_1 = ModRegistries.ITEMS.register("item_capacity_upgrade_1", () -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade1, new Item.Properties()));
		ITEM_CAPACITY_UPGRADE_2 = ModRegistries.ITEMS.register("item_capacity_upgrade_2", () -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade2, new Item.Properties()));
		ITEM_CAPACITY_UPGRADE_3 = ModRegistries.ITEMS.register("item_capacity_upgrade_3", () -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade3, new Item.Properties()));
		ITEM_CAPACITY_UPGRADE_4 = ModRegistries.ITEMS.register("item_capacity_upgrade_4", () -> new CapacityUpgradeItem(Upgrades.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade4, new Item.Properties()));

		//Speed Upgrades
		SPEED_UPGRADE_1 = ModRegistries.ITEMS.register("speed_upgrade_1", () -> new SpeedUpgradeItem(4, new Item.Properties()));
		SPEED_UPGRADE_2 = ModRegistries.ITEMS.register("speed_upgrade_2", () -> new SpeedUpgradeItem(8, new Item.Properties()));
		SPEED_UPGRADE_3 = ModRegistries.ITEMS.register("speed_upgrade_3", () -> new SpeedUpgradeItem(12, new Item.Properties()));
		SPEED_UPGRADE_4 = ModRegistries.ITEMS.register("speed_upgrade_4", () -> new SpeedUpgradeItem(16, new Item.Properties()));
		SPEED_UPGRADE_5 = ModRegistries.ITEMS.register("speed_upgrade_5", () -> new SpeedUpgradeItem(20, new Item.Properties()));
		
		//Network Upgrade
		NETWORK_UPGRADE = ModRegistries.ITEMS.register("network_upgrade", () -> new UpgradeItem.Simple(Upgrades.NETWORK, new Item.Properties()));
		
		//Hopper Upgrade
		HOPPER_UPGRADE = ModRegistries.ITEMS.register("hopper_upgrade", () -> new UpgradeItem.Simple(Upgrades.HOPPER, new Item.Properties()));

		//Coin Chest Upgrades
		COIN_CHEST_EXCHANGE_UPGRADE = ModRegistries.ITEMS.register("coin_chest_exchange_upgrade", () -> new UpgradeItem.Simple(Upgrades.COIN_CHEST_EXCHANGE, new Item.Properties()));
		COIN_CHEST_MAGNET_UPGRADE_1 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_1", () -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange1, new Item.Properties()));
		COIN_CHEST_MAGNET_UPGRADE_2 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_2", () -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange2, new Item.Properties()));
		COIN_CHEST_MAGNET_UPGRADE_3 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_3", () -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange3, new Item.Properties()));
		COIN_CHEST_MAGNET_UPGRADE_4 = ModRegistries.ITEMS.register("coin_chest_magnet_upgrade_4", () -> new MagnetUpgradeItem(LCConfig.SERVER.coinChestMagnetRange4, new Item.Properties()));
		COIN_CHEST_SECURITY_UPGRADE = ModRegistries.ITEMS.register("coin_chest_security_upgrade", () -> new UpgradeItem.Simple(Upgrades.COIN_CHEST_SECURITY, new Item.Properties()));

		//Smithing Templates (1.20 only)
		UPGRADE_SMITHING_TEMPLATE = ModRegistries.ITEMS.register("upgrade_smithing_template", () -> new LCUpgradeSmithingTemplateItem(LCText.TOOLTIP_UPGRADE_TEMPLATE, new Item.Properties()));

	}
	
	//Hold the items for public access
	public static final RegistryObject<Item> COIN_COPPER;
	public static final RegistryObject<Item> COIN_IRON;
	public static final RegistryObject<Item> COIN_GOLD;
	public static final RegistryObject<Item> COIN_EMERALD;
	public static final RegistryObject<Item> COIN_DIAMOND;
	public static final RegistryObject<Item> COIN_NETHERITE;

	public static final RegistryObject<Item> COIN_CHOCOLATE_COPPER;
	public static final RegistryObject<Item> COIN_CHOCOLATE_IRON;
	public static final RegistryObject<Item> COIN_CHOCOLATE_GOLD;
	public static final RegistryObject<Item> COIN_CHOCOLATE_EMERALD;
	public static final RegistryObject<Item> COIN_CHOCOLATE_DIAMOND;
	public static final RegistryObject<Item> COIN_CHOCOLATE_NETHERITE;
	
	public static final RegistryObject<Item> TRADING_CORE;
	public static final RegistryObject<Item> TICKET;
	public static final RegistryObject<Item> TICKET_PASS;
	public static final RegistryObject<Item> TICKET_MASTER;
	public static final RegistryObject<Item> TICKET_STUB;

	public static final RegistryObject<Item> GOLDEN_TICKET;
	public static final RegistryObject<Item> GOLDEN_TICKET_PASS;
	public static final RegistryObject<Item> GOLDEN_TICKET_MASTER;
	public static final RegistryObject<Item> GOLDEN_TICKET_STUB;
	
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
	public static final RegistryObject<Item> ITEM_CAPACITY_UPGRADE_4;

	public static final RegistryObject<Item> SPEED_UPGRADE_1;
	public static final RegistryObject<Item> SPEED_UPGRADE_2;
	public static final RegistryObject<Item> SPEED_UPGRADE_3;
	public static final RegistryObject<Item> SPEED_UPGRADE_4;
	public static final RegistryObject<Item> SPEED_UPGRADE_5;
	
	public static final RegistryObject<Item> NETWORK_UPGRADE;
	
	public static final RegistryObject<Item> HOPPER_UPGRADE;

	public static final RegistryObject<Item> COIN_CHEST_EXCHANGE_UPGRADE;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_1;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_2;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_3;
	public static final RegistryObject<Item> COIN_CHEST_MAGNET_UPGRADE_4;
	public static final RegistryObject<Item> COIN_CHEST_SECURITY_UPGRADE;

	public static final RegistryObject<Item> UPGRADE_SMITHING_TEMPLATE;

}
