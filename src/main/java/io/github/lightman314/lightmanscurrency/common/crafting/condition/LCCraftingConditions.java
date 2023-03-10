package io.github.lightman314.lightmanscurrency.common.crafting.condition;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class LCCraftingConditions {

	public static class NetworkTrader extends SimpleCraftingCondition {
		public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "network_trader_craftable");
		private static final NetworkTrader INSTANCE = new NetworkTrader();
		public static final IConditionSerializer<NetworkTrader> SERIALIZER = new Serializer();
		private NetworkTrader() { super(TYPE, Config.COMMON.canCraftNetworkTraders::get); }
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
		private static final TraderInterface INSTANCE = new TraderInterface();
		public static final IConditionSerializer<TraderInterface> SERIALIZER = new Serializer();
		private TraderInterface() { super(TYPE, Config.COMMON.canCraftTraderInterfaces::get); }
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
		private static final AuctionStand INSTANCE = new AuctionStand();
		public static final IConditionSerializer<AuctionStand> SERIALIZER = new Serializer();
		private AuctionStand() { super(TYPE, Config.COMMON.canCraftAuctionStands::get); }
		private static class Serializer implements IConditionSerializer<AuctionStand> {
			@Override
			public void write(JsonObject json, AuctionStand value) {}
			@Override
			public AuctionStand read(JsonObject json) { return INSTANCE; }
			@Override
			public ResourceLocation getID() { return TYPE; }
		}
	}
	
}