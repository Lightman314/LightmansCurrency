package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.proxy.ClientProxy;
import io.github.lightman314.lightmanscurrency.proxy.CommonProxy;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.VillagerTradeManager;
import io.github.lightman314.lightmanscurrency.integration.immersiveengineering.LCImmersive;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import io.github.lightman314.lightmanscurrency.common.capability.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.*;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.*;
import io.github.lightman314.lightmanscurrency.common.traders.item.*;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.*;
import io.github.lightman314.lightmanscurrency.common.traders.rules.*;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.*;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.*;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import io.github.lightman314.lightmanscurrency.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.discord.DiscordListenerRegistration;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.time.MessageSyncClientTime;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

import java.util.function.Consumer;

@Mod("lightmanscurrency")
public class LightmansCurrency {
	
	public static final String MODID = "lightmanscurrency";
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
    private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Whether the Curios API mod is installed
	 */
	public static boolean isCuriosLoaded() { return ModList.get().isLoaded("curios"); }

	/**
	 * Whether the Curios API mod is installed, and a valid Wallet Slot is present on the given entity.
	 */
	public static boolean isCuriosValid(LivingEntity player) {
		try {
			if(isCuriosLoaded())
				return LCCurios.hasWalletSlot(player);
		} catch(Throwable ignored) { }
		return false;
	}
    
	public LightmansCurrency() {

		LootManager.registerDroplistListeners();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imc);

        //Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        //Register the proxy so that it can run custom events
        MinecraftForge.EVENT_BUS.register(PROXY);
        
        if(ModList.get().isLoaded("lightmansdiscord"))
        {
        	MinecraftForge.EVENT_BUS.register(DiscordListenerRegistration.class);
        	CurrencyMessages.init();
        }

		if(ModList.get().isLoaded("immersiveengineering"))
			LCImmersive.registerRotationBlacklists();
        
    }
	
	private void imc(InterModEnqueueEvent event) {
		if(isCuriosLoaded())
			safeEnqueueWork(event, "Error during LC ==> Curios Inter-mod Communications!", this::curiosIMC);
	}

	private void curiosIMC() { /*Done via datapack now, no longer need to register these in the code*/ }
    
    private void commonSetup(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

	private void commonSetupWork(FMLCommonSetupEvent event) {

		LightmansCurrencyPacketHandler.init();

		//Register Crafting Conditions
		LCCraftingConditions.register();

		//Initialize the TraderData deserializers
		TraderData.register(ItemTraderData.TYPE, ItemTraderData::new);
		TraderData.register(ItemTraderDataArmor.TYPE, ItemTraderDataArmor::new);
		TraderData.register(ItemTraderDataTicket.TYPE, ItemTraderDataTicket::new);
		TraderData.register(ItemTraderDataBook.TYPE, ItemTraderDataBook::new);
		TraderData.register(SlotMachineTraderData.TYPE, SlotMachineTraderData::new);
		TraderData.register(PaygateTraderData.TYPE, PaygateTraderData::new);
		TraderData.register(AuctionHouseTrader.TYPE, AuctionHouseTrader::new);

		//Register the custom game rules
		ModGameRules.registerRules();

		//Initialize the Trade Rule deserializers
		TradeRule.RegisterDeserializer(PlayerWhitelist.TYPE, PlayerWhitelist::new);
		TradeRule.RegisterDeserializer(PlayerBlacklist.TYPE, PlayerBlacklist::new);
		TradeRule.RegisterDeserializer(PlayerTradeLimit.TYPE, PlayerTradeLimit::new);
		TradeRule.RegisterDeserializer(PlayerTradeLimit.OLD_TYPE, PlayerTradeLimit::new, true);
		TradeRule.RegisterDeserializer(PlayerDiscounts.TYPE, PlayerDiscounts::new);
		TradeRule.RegisterDeserializer(TimedSale.TYPE, TimedSale::new);
		TradeRule.RegisterDeserializer(TradeLimit.TYPE, TradeLimit::new);
		TradeRule.RegisterDeserializer(TradeLimit.OLD_TYPE, TradeLimit::new, true);
		TradeRule.RegisterDeserializer(FreeSample.TYPE, FreeSample::new);
		TradeRule.RegisterDeserializer(PriceFluctuation.TYPE, PriceFluctuation::new);

		//Initialize the Notification deserializers
		Notification.register(ItemTradeNotification.TYPE, ItemTradeNotification::new);
		Notification.register(PaygateNotification.TYPE, PaygateNotification::new);
		Notification.register(SlotMachineTradeNotification.TYPE, SlotMachineTradeNotification::new);
		Notification.register(OutOfStockNotification.TYPE, OutOfStockNotification::new);
		Notification.register(LowBalanceNotification.TYPE, LowBalanceNotification::new);
		Notification.register(AuctionHouseSellerNotification.TYPE, AuctionHouseSellerNotification::new);
		Notification.register(AuctionHouseBuyerNotification.TYPE, AuctionHouseBuyerNotification::new);
		Notification.register(AuctionHouseSellerNobidNotification.TYPE, AuctionHouseSellerNobidNotification::new);
		Notification.register(AuctionHouseBidNotification.TYPE, AuctionHouseBidNotification::new);
		Notification.register(AuctionHouseCancelNotification.TYPE, AuctionHouseCancelNotification::new);
		Notification.register(TextNotification.TYPE, TextNotification::new);
		Notification.register(AddRemoveAllyNotification.TYPE, AddRemoveAllyNotification::new);
		Notification.register(AddRemoveTradeNotification.TYPE, AddRemoveTradeNotification::new);
		Notification.register(ChangeAllyPermissionNotification.TYPE, ChangeAllyPermissionNotification::new);
		Notification.register(ChangeCreativeNotification.TYPE, ChangeCreativeNotification::new);
		Notification.register(ChangeNameNotification.TYPE, ChangeNameNotification::new);
		Notification.register(ChangeOwnerNotification.TYPE, ChangeOwnerNotification::new);
		Notification.register(ChangeSettingNotification.SIMPLE_TYPE, ChangeSettingNotification.Simple::new);
		Notification.register(ChangeSettingNotification.ADVANCED_TYPE, ChangeSettingNotification.Advanced::new);
		Notification.register(DepositWithdrawNotification.PLAYER_TYPE, DepositWithdrawNotification.Player::new);
		Notification.register(DepositWithdrawNotification.TRADER_TYPE, DepositWithdrawNotification.Trader::new);
		Notification.register(DepositWithdrawNotification.SERVER_TYPE, DepositWithdrawNotification.Server::new);
		Notification.register(BankTransferNotification.TYPE, BankTransferNotification::new);

		//Initialize the Notification Category deserializers
		NotificationCategory.register(NotificationCategory.GENERAL_TYPE, c -> NotificationCategory.GENERAL);
		NotificationCategory.register(NullCategory.TYPE, c -> NullCategory.INSTANCE);
		NotificationCategory.register(TraderCategory.TYPE, TraderCategory::new);
		NotificationCategory.register(BankCategory.TYPE, BankCategory::new);
		NotificationCategory.register(AuctionHouseCategory.TYPE, c -> AuctionHouseCategory.INSTANCE);

		//Register Trader Search Filters
		TraderSearchFilter.addFilter(new BasicSearchFilter());
		TraderSearchFilter.addFilter(new ItemTraderSearchFilter());

		//Register Upgrade Types
		MinecraftForge.EVENT_BUS.post(new UpgradeType.RegisterUpgradeTypeEvent());

		ATMIconData.init();

		//Initialize the Item Trade Restrictions
		ItemTradeRestriction.init();

		//Villager Trades
		VillagerTradeManager.registerDefaultTrades();
		ItemListingSerializer.registerDefaultSerializers();
	}
    
    private void clientSetup(final FMLClientSetupEvent event) { safeEnqueueWork(event, "Error during client setup!", PROXY::setupClient); }
    
    private void onConfigLoad(ModConfigEvent event)
    {
    	if(event.getConfig().getModId().equals(MODID) && event.getConfig().getSpec() == Config.commonSpec)
    	{
    		//Have the loot manager validate the entity loot contents
    		LootManager.validateEntityDropList();
    		LootManager.debugLootConfigs();

			//Regenerate the loot tables so that any itemLoot entries being changed will be reflected in-game.
			LootManager.regenerateLootTables();

			//Only reload villager overrides on the initial load, as it's impossible to change the values after the villager trades have been loaded.
			Config.reloadVillagerOverrides();
    	}
    }
    
    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
    	event.register(IWalletHandler.class);
    	event.register(ISpawnTracker.class);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
    	
    	//Preload target
    	PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
    	//Sync time
    	LightmansCurrencyPacketHandler.instance.send(target, new MessageSyncClientTime());
    	//Sync admin list
    	LightmansCurrencyPacketHandler.instance.send(target, CommandLCAdmin.getAdminSyncMessage());
    	
    }
    
    /**
     * Easy public access to the equipped wallet.
     * Also confirms that the equipped wallet is either empty or a valid WalletItem.
     * Returns an empty stack if no wallet is equipped, or if the equipped item is not a valid wallet.
     */
    public static ItemStack getWalletStack(Player player)
    {

		if(player == null)
			return ItemStack.EMPTY;

    	ItemStack wallet = ItemStack.EMPTY;
    	
    	IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
    	if(walletHandler != null)
    		wallet = walletHandler.getWallet();
    	//Safety check to confirm that the Item Stack found is a valid wallet
    	if(!WalletItem.validWalletStack(wallet))
    	{
    		LightmansCurrency.LogDebug(player.getName().getString() + "'s equipped wallet is not a valid WalletItem.");
    		LightmansCurrency.LogDebug("Equipped wallet is of type " + wallet.getItem().getClass().getName());
			return ItemStack.EMPTY;
    	}
    	return wallet;
    }

    public static void LogDebug(String message) { LOGGER.debug(message); }
    public static void LogDebug(String message, Object... objects) { LOGGER.debug(message, objects); }

    public static void LogInfo(String message)
    {
    	if(Config.commonSpec.isLoaded() && Config.COMMON.debugLevel.get() > 0)
    		LOGGER.debug("INFO: " + message);
    	else
    		LOGGER.info(message);
    }
    
    public static void LogWarning(String message)
    {
    	if(Config.commonSpec.isLoaded() && Config.COMMON.debugLevel.get() > 1)
    		LOGGER.debug("WARN: " + message);
    	else
    		LOGGER.warn(message);
    }
    
    public static void LogError(String message, Object... objects)
    {
    	if(Config.commonSpec.isLoaded() && Config.COMMON.debugLevel.get() > 2)
    		LOGGER.debug("ERROR: " + message, objects);
    	else
    		LOGGER.error(message, objects);
    }
    
    public static void LogError(String message)
    {
    	if(Config.commonSpec.isLoaded() && Config.COMMON.debugLevel.get() > 2)
    		LOGGER.debug("ERROR: " + message);
    	else
    		LOGGER.error(message);
    }

	public static void safeEnqueueWork(ParallelDispatchEvent event, String errorMessage, Runnable work) {
		event.enqueueWork(() -> {
			try{
				work.run();
			} catch(Throwable t) {
				LogError(errorMessage, t);
			}
		});
	}

	public static <T extends ParallelDispatchEvent> void safeEnqueueWork(T event, String errorMessage, Consumer<T> work) {
		event.enqueueWork(() -> {
			try{
				work.accept(event);
			} catch(Throwable t) {
				LogError(errorMessage, t);
			}
		});
	}
    
}
