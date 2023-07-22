package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class LCCraftingConditions {

	public static class NetworkTrader extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "network_trader_craftable");
		public static final NetworkTrader INSTANCE = new NetworkTrader();
		public static final IConditionSerializer<NetworkTrader> SERIALIZER = new Serializer();
		private NetworkTrader() { super(TYPE, Config.COMMON.canCraftNetworkTraders); }
		private static class Serializer implements IConditionSerializer<NetworkTrader> {
			@Override
			public void write(JsonObject json, NetworkTrader value) {}
			@Override
			public NetworkTrader read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE;}
		}
	}

	public static class TraderInterface extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "trader_interface_craftable");
		public static final TraderInterface INSTANCE = new TraderInterface();
		public static final IConditionSerializer<TraderInterface> SERIALIZER = new Serializer();
		private TraderInterface() { super(TYPE, Config.COMMON.canCraftTraderInterfaces); }
		private static class Serializer implements IConditionSerializer<TraderInterface> {
			@Override
			public void write(JsonObject json, TraderInterface value) {}
			@Override
			public TraderInterface read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE;}
		}
	}

	public static class AuctionStand extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_stand_craftable");
		public static final AuctionStand INSTANCE = new AuctionStand();
		public static final IConditionSerializer<AuctionStand> SERIALIZER = new Serializer();
		private AuctionStand() { super(TYPE, Config.COMMON.canCraftAuctionStands); }
		private static class Serializer implements IConditionSerializer<AuctionStand> {
			@Override
			public void write(JsonObject json, AuctionStand value) {}
			@Override
			public AuctionStand read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}

	public static class CoinChest extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_craftable");
		public static final CoinChest INSTANCE = new CoinChest();
		public static final IConditionSerializer<CoinChest> SERIALIZER = new Serializer();
		private CoinChest() { super(TYPE, Config.COMMON.canCraftCoinChest); }
		private static class Serializer implements IConditionSerializer<CoinChest> {
			@Override
			public void write(JsonObject json, CoinChest value) {}
			@Override
			public CoinChest read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}

	public static class CoinChestUpgradeExchange extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_exchange_craftable");
		public static final CoinChestUpgradeExchange INSTANCE = new CoinChestUpgradeExchange();
		public static final IConditionSerializer<CoinChestUpgradeExchange> SERIALIZER = new Serializer();
		private CoinChestUpgradeExchange() { super(TYPE, Config.COMMON.canCraftCoinChestUpgradeExchange); }
		private static class Serializer implements IConditionSerializer<CoinChestUpgradeExchange> {
			@Override
			public void write(JsonObject json, CoinChestUpgradeExchange value) {}
			@Override
			public CoinChestUpgradeExchange read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}

	public static class CoinChestUpgradeBank extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_bank_craftable");
		public static final CoinChestUpgradeBank INSTANCE = new CoinChestUpgradeBank();
		public static final IConditionSerializer<CoinChestUpgradeBank> SERIALIZER = new Serializer();
		private CoinChestUpgradeBank() { super(TYPE, () -> false); }
		private static class Serializer implements IConditionSerializer<CoinChestUpgradeBank> {
			@Override
			public void write(JsonObject json, CoinChestUpgradeBank value) {}
			@Override
			public CoinChestUpgradeBank read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}

	public static class CoinChestUpgradeMagnet extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_magnet_craftable");
		public static final CoinChestUpgradeMagnet INSTANCE = new CoinChestUpgradeMagnet();
		public static final IConditionSerializer<CoinChestUpgradeMagnet> SERIALIZER = new Serializer();
		private CoinChestUpgradeMagnet() { super(TYPE, Config.COMMON.canCraftCoinChestUpgradeSecurity); }
		private static class Serializer implements IConditionSerializer<CoinChestUpgradeMagnet> {
			@Override
			public void write(JsonObject json, CoinChestUpgradeMagnet value) {}
			@Override
			public CoinChestUpgradeMagnet read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}

	public static class CoinChestUpgradeSecurity extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_security_craftable");
		public static final CoinChestUpgradeSecurity INSTANCE = new CoinChestUpgradeSecurity();
		public static final IConditionSerializer<CoinChestUpgradeSecurity> SERIALIZER = new Serializer();
		private CoinChestUpgradeSecurity() { super(TYPE, Config.COMMON.canCraftCoinChestUpgradeSecurity); }
		private static class Serializer implements IConditionSerializer<CoinChestUpgradeSecurity> {
			@Override
			public void write(JsonObject json, CoinChestUpgradeSecurity value) {}
			@Override
			public CoinChestUpgradeSecurity read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}

	public static void register()
	{
		try{
			//Register Crafting Conditions
			CraftingHelper.register(NetworkTrader.SERIALIZER);
			CraftingHelper.register(TraderInterface.SERIALIZER);
			CraftingHelper.register(AuctionStand.SERIALIZER);
			CraftingHelper.register(CoinChest.SERIALIZER);
			CraftingHelper.register(CoinChestUpgradeExchange.SERIALIZER);
			CraftingHelper.register(CoinChestUpgradeBank.SERIALIZER);
			CraftingHelper.register(CoinChestUpgradeMagnet.SERIALIZER);
			CraftingHelper.register(CoinChestUpgradeSecurity.SERIALIZER);
		} catch(IllegalStateException ignored) { }
	}

}