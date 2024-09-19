package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;

public class LCCraftingConditions {

	public static void init() {}

	public static class NetworkTrader extends SimpleCraftingCondition {
		public static final NetworkTrader INSTANCE = new NetworkTrader();
		private static final MapCodec<NetworkTrader> CODEC = MapCodec.unit(() -> INSTANCE);
		private NetworkTrader() { super(() -> CODEC,LCConfig.COMMON.canCraftNetworkTraders); }
	}
	
	public static class TraderInterface extends SimpleCraftingCondition {
		public static final TraderInterface INSTANCE = new TraderInterface();
		private static final MapCodec<TraderInterface> CODEC = MapCodec.unit(() -> INSTANCE);
		private TraderInterface() { super(() -> CODEC,LCConfig.COMMON.canCraftTraderInterfaces); }
	}

	public static class AuctionStand extends SimpleCraftingCondition {
		public static final AuctionStand INSTANCE = new AuctionStand();
		private static final MapCodec<AuctionStand> CODEC = MapCodec.unit(() -> INSTANCE);
		private AuctionStand() { super(() -> CODEC,LCConfig.COMMON.canCraftAuctionStands); }
	}

	public static class CoinChest extends SimpleCraftingCondition {
		public static final CoinChest INSTANCE = new CoinChest();
		private static final MapCodec<CoinChest> CODEC = MapCodec.unit(() -> INSTANCE);
		private CoinChest() { super(() -> CODEC,LCConfig.COMMON.canCraftCoinChest); }
	}

	public static class CoinChestUpgradeExchange extends SimpleCraftingCondition {
		public static final CoinChestUpgradeExchange INSTANCE = new CoinChestUpgradeExchange();
		private static final MapCodec<CoinChestUpgradeExchange> CODEC = MapCodec.unit(() -> INSTANCE);
		private CoinChestUpgradeExchange() { super(() -> CODEC,LCConfig.COMMON.canCraftCoinChestUpgradeExchange); }
	}

	public static class CoinChestUpgradeMagnet extends SimpleCraftingCondition {
		public static final CoinChestUpgradeMagnet INSTANCE = new CoinChestUpgradeMagnet();
		private static final MapCodec<CoinChestUpgradeMagnet> CODEC = MapCodec.unit(() -> INSTANCE);
		private CoinChestUpgradeMagnet() { super(() -> CODEC,LCConfig.COMMON.canCraftCoinChestUpgradeMagnet); }
	}

	public static class CoinChestUpgradeBank extends SimpleCraftingCondition {
		public static final CoinChestUpgradeBank INSTANCE = new CoinChestUpgradeBank();
		private static final MapCodec<CoinChestUpgradeBank> CODEC = MapCodec.unit(() -> INSTANCE);
		private CoinChestUpgradeBank() { super(() -> CODEC, LCConfig.COMMON.canCraftCoinChestUpgradeBank); }
	}

	public static class CoinChestUpgradeSecurity extends SimpleCraftingCondition {
		public static final CoinChestUpgradeSecurity INSTANCE = new CoinChestUpgradeSecurity();
		private static final MapCodec<CoinChestUpgradeSecurity> CODEC = MapCodec.unit(() -> INSTANCE);
		private CoinChestUpgradeSecurity() { super(() -> CODEC,LCConfig.COMMON.canCraftCoinChestUpgradeSecurity); }
	}

	public static class TaxCollector extends SimpleCraftingCondition {
		public static final TaxCollector INSTANCE = new TaxCollector();
		private static final MapCodec<TaxCollector> CODEC = MapCodec.unit(() -> INSTANCE);
		private TaxCollector() { super(() -> CODEC,LCConfig.COMMON.canCraftTaxBlock); }
	}

	public static class ATMCard extends SimpleCraftingCondition {
		public static final ATMCard INSTANCE = new ATMCard();
		private static final MapCodec<ATMCard> CODEC = MapCodec.unit(() -> INSTANCE);
		private ATMCard() { super(() -> CODEC,LCConfig.COMMON.canCraftATMCard); }
	}

	static {

		LightmansCurrency.LogDebug("Registering LC Crafting Conditions");
		ModRegistries.CRAFTING_CONDITIONS.register("network_trader_craftable", () -> NetworkTrader.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("trader_interface_craftable", () -> TraderInterface.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("auction_stand_craftable", () -> AuctionStand.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("coin_chest_craftable", () -> CoinChest.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("coin_chest_exchange_craftable", () -> CoinChestUpgradeExchange.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("coin_chest_magnet_craftable", () -> CoinChestUpgradeMagnet.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("coin_chest_bank_craftable", () -> CoinChestUpgradeBank.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("coin_chest_security_craftable", () -> CoinChestUpgradeSecurity.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("tax_collector_craftable", () -> TaxCollector.CODEC);
		ModRegistries.CRAFTING_CONDITIONS.register("atm_card_craftable", () -> ATMCard.CODEC);

	}
	
}
