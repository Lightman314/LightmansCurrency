package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.SimpleCraftingCondition;
import net.minecraft.resources.ResourceLocation;

public class LCCraftingConditions {

    /**
     * Placeholder function to force the static class loading
     */
    public static void init() { }

    public static class NetworkTrader extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "network_trader_craftable");
        public static final NetworkTrader INSTANCE = new NetworkTrader();
        private NetworkTrader() { super(TYPE, Config.COMMON.canCraftNetworkTraders); }
    }

    public static class TraderInterface extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "trader_interface_craftable");
        public static final TraderInterface INSTANCE = new TraderInterface();
        private TraderInterface() { super(TYPE, Config.COMMON.canCraftTraderInterfaces); }
    }

    public static class AuctionStand extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_stand_craftable");
        public static final AuctionStand INSTANCE = new AuctionStand();
        private AuctionStand() { super(TYPE, Config.COMMON.canCraftAuctionStands); }
    }

    public static class CoinChest extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_craftable");
        public static final CoinChest INSTANCE = new CoinChest();

        private CoinChest() { super(TYPE, Config.COMMON.canCraftCoinChest); }
    }

    public static class CoinChestUpgradeExchange extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_exchange_craftable");
        public static final CoinChestUpgradeExchange INSTANCE = new CoinChestUpgradeExchange();
        private CoinChestUpgradeExchange() { super(TYPE, Config.COMMON.canCraftCoinChestUpgradeExchange); }
    }

    public static class CoinChestUpgradeMagnet extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_magnet_craftable");
        public static final CoinChestUpgradeMagnet INSTANCE = new CoinChestUpgradeMagnet();
        private CoinChestUpgradeMagnet() { super(TYPE, Config.COMMON.canCraftCoinChestUpgradeMagnet); }
    }

    public static class CoinChestUpgradeSecurity extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "coin_chest_security_craftable");
        public static final CoinChestUpgradeSecurity INSTANCE = new CoinChestUpgradeSecurity();
        private CoinChestUpgradeSecurity() { super(TYPE, Config.COMMON.canCraftCoinChestUpgradeSecurity); }
    }

    public static class TaxCollector extends SimpleCraftingCondition {
        public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "tax_collector_craftable");
        public static final TaxCollector INSTANCE = new TaxCollector();
        private TaxCollector() { super(TYPE, Config.COMMON.canCraftTaxBlock); }
    }
    
    
    static {
        ModRegistries.CRAFTING_CONDITIONS.register(NetworkTrader.TYPE.getPath(), NetworkTrader.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(TraderInterface.TYPE.getPath(), TraderInterface.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(AuctionStand.TYPE.getPath(), AuctionStand.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(CoinChest.TYPE.getPath(), CoinChest.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(CoinChestUpgradeExchange.TYPE.getPath(), CoinChestUpgradeExchange.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(CoinChestUpgradeMagnet.TYPE.getPath(), CoinChestUpgradeMagnet.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(CoinChestUpgradeSecurity.TYPE.getPath(), CoinChestUpgradeSecurity.INSTANCE::codec);
        ModRegistries.CRAFTING_CONDITIONS.register(TaxCollector.TYPE.getPath(), TaxCollector.INSTANCE::codec);
    }

}
