package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.misc.BlockProtectionHelper;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.NullCurrencyType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.*;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.PlayerOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.TeamOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.stats.types.*;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.blocks.CoinBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.*;
import io.github.lightman314.lightmanscurrency.common.event_coins.ChocolateEventCoins;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.taxes.TaxesCollectedNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.taxes.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin.TaxableTraderReference;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import io.github.lightman314.lightmanscurrency.integration.biomesoplenty.BOPCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.integration.claiming.flan.LCFlanIntegration;
import io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks.LCFTBChunksIntegration;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import io.github.lightman314.lightmanscurrency.proxy.ClientProxy;
import io.github.lightman314.lightmanscurrency.proxy.CommonProxy;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmanscurrency.common.notifications.categories.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.*;
import io.github.lightman314.lightmanscurrency.common.traders.auction.*;
import io.github.lightman314.lightmanscurrency.common.traders.item.*;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.*;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.*;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.*;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.network.message.time.SPacketSyncTime;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@Mod("lightmanscurrency")
public class LightmansCurrency {
	
	public static final String MODID = "lightmanscurrency";
	
	public static final CommonProxy PROXY;

	static {
		if(FMLLoader.getDist() == Dist.CLIENT)
			PROXY = new ClientProxy();
		else
			PROXY = new CommonProxy();
	}

    private static final Logger LOGGER = LogManager.getLogger();
    
	public LightmansCurrency(@Nonnull IEventBus eventBus) {

		LootManager.registerDroplistListeners();

		eventBus.addListener(this::commonSetup);
		eventBus.addListener(this::clientSetup);

        //Register configs
		LCConfig.init();
		LootManager.init();
        
        // Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

		//Setup Wood Compatibilities before registering blocks/items
		IntegrationUtil.SafeRunIfLoaded("biomesoplenty", BOPCustomWoodTypes::setupWoodTypes, "Error setting up BOP wood types! BOP has probably changed their API!");
		//IntegrationUtil.SafeRunIfLoaded("quark", QuarkCustomWoodTypes::setupWoodTypes, "Error setting up Quark wood types! Quark has probably changed their API!");

        //Setup Deferred Registries
        ModRegistries.register(eventBus);
        
        //Register the proxy so that it can run custom events
		NeoForge.EVENT_BUS.register(PROXY);

		IntegrationUtil.SafeRunIfLoaded("ftbchunks", LCFTBChunksIntegration::setup, "Error setting up FTB Chunks chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("flan", LCFlanIntegration::setup, "Error setting up Flans chunk purchasing integration!");
		//IntegrationUtil.SafeRunIfLoaded("immersiveengineering", LCImmersive::registerRotationBlacklists, null);
		//IntegrationUtil.SafeRunIfLoaded("supplementaries", LCSupplementaries::setup, null);
        
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

	private void commonSetupWork(FMLCommonSetupEvent event) {

		//Manually load common config for villager edit purposes
		ConfigFile.loadServerFiles(ConfigFile.LoadPhase.SETUP);

		//Setup Cadmus Integration during common setup so that other mods will have already registered their claim providers
		//IntegrationUtil.SafeRunIfLoaded("cadmus", LCCadmusIntegration::setup,null);

		//Setup Money System
		CoinAPI.API.Setup();
		//Register built-in Currency Types
		MoneyAPI.API.RegisterCurrencyType(CoinCurrencyType.INSTANCE);
		MoneyAPI.API.RegisterCurrencyType(NullCurrencyType.INSTANCE);

		//Ownership API data
		OwnershipAPI.API.registerOwnerType(Owner.NULL_TYPE);
		OwnershipAPI.API.registerOwnerType(FakeOwner.TYPE);
		OwnershipAPI.API.registerOwnerType(PlayerOwner.TYPE);
		OwnershipAPI.API.registerOwnerType(TeamOwner.TYPE);

		OwnershipAPI.API.registerPotentialOwnerProvider(PlayerOwnerProvider.INSTANCE);
		OwnershipAPI.API.registerPotentialOwnerProvider(TeamOwnerProvider.INSTANCE);

		//Initialize the TraderData deserializers
		TraderAPI.API.RegisterTrader(ItemTraderData.TYPE);
		TraderAPI.API.RegisterTrader(ItemTraderDataArmor.TYPE);
		TraderAPI.API.RegisterTrader(ItemTraderDataTicket.TYPE);
		TraderAPI.API.RegisterTrader(ItemTraderDataBook.TYPE);
		TraderAPI.API.RegisterTrader(SlotMachineTraderData.TYPE);
		TraderAPI.API.RegisterTrader(PaygateTraderData.TYPE);
		TraderAPI.API.RegisterTrader(AuctionHouseTrader.TYPE);

		//Register the custom game rules
		ModGameRules.registerRules();

		//Initialize the Trade Rule deserializers
		TraderAPI.API.RegisterTradeRule(PlayerListing.TYPE);
		TraderAPI.API.RegisterTradeRule(PlayerTradeLimit.TYPE);
		TraderAPI.API.RegisterTradeRule(PlayerDiscounts.TYPE);
		TraderAPI.API.RegisterTradeRule(TimedSale.TYPE);
		TraderAPI.API.RegisterTradeRule(TradeLimit.TYPE);
		TraderAPI.API.RegisterTradeRule(FreeSample.TYPE);
		TraderAPI.API.RegisterTradeRule(PriceFluctuation.TYPE);

		TradeRule.addLoadListener(PlayerListing.LISTENER);
		TradeRule.addIgnoreMissing("lightmanscurrency:whitelist");
		TradeRule.addIgnoreMissing("lightmanscurrency:blacklist");

		//Initialize the Notification deserializers
		NotificationAPI.registerNotification(ItemTradeNotification.TYPE);
		NotificationAPI.registerNotification(PaygateNotification.TYPE);
		NotificationAPI.registerNotification(SlotMachineTradeNotification.TYPE);
		NotificationAPI.registerNotification(OutOfStockNotification.TYPE);
		NotificationAPI.registerNotification(LowBalanceNotification.TYPE);
		NotificationAPI.registerNotification(AuctionHouseSellerNotification.TYPE);
		NotificationAPI.registerNotification(AuctionHouseBuyerNotification.TYPE);
		NotificationAPI.registerNotification(AuctionHouseSellerNobidNotification.TYPE);
		NotificationAPI.registerNotification(AuctionHouseBidNotification.TYPE);
		NotificationAPI.registerNotification(AuctionHouseCancelNotification.TYPE);
		NotificationAPI.registerNotification(TextNotification.TYPE);
		NotificationAPI.registerNotification(AddRemoveAllyNotification.TYPE);
		NotificationAPI.registerNotification(AddRemoveTradeNotification.TYPE);
		NotificationAPI.registerNotification(ChangeAllyPermissionNotification.TYPE);
		NotificationAPI.registerNotification(ChangeCreativeNotification.TYPE);
		NotificationAPI.registerNotification(ChangeNameNotification.TYPE);
		NotificationAPI.registerNotification(ChangeOwnerNotification.TYPE);
		NotificationAPI.registerNotification(ChangeSettingNotification.SIMPLE_TYPE);
		NotificationAPI.registerNotification(ChangeSettingNotification.ADVANCED_TYPE);
		NotificationAPI.registerNotification(DepositWithdrawNotification.PLAYER_TYPE);
		NotificationAPI.registerNotification(DepositWithdrawNotification.TRADER_TYPE);
		NotificationAPI.registerNotification(DepositWithdrawNotification.SERVER_TYPE);
		NotificationAPI.registerNotification(BankTransferNotification.TYPE);
		NotificationAPI.registerNotification(BankInterestNotification.TYPE);
		NotificationAPI.registerNotification(TaxesCollectedNotification.TYPE);
		NotificationAPI.registerNotification(TaxesPaidNotification.TYPE);
		NotificationAPI.registerNotification(OwnableBlockEjectedNotification.TYPE);

		//Initialize the Notification Category deserializers
		NotificationAPI.registerCategory(NotificationCategory.GENERAL_TYPE);
		NotificationAPI.registerCategory(NullCategory.TYPE);
		NotificationAPI.registerCategory(TraderCategory.TYPE);
		NotificationAPI.registerCategory(BankCategory.TYPE);
		NotificationAPI.registerCategory(AuctionHouseCategory.TYPE);
		NotificationAPI.registerCategory(TaxEntryCategory.TYPE);

		//Register Trader Search Filters
		TraderAPI.API.RegisterTraderSearchFilter(new BasicSearchFilter());
		TraderAPI.API.RegisterSearchFilter(new ItemTraderSearchFilter());
		TraderAPI.API.RegisterSearchFilter(new SlotMachineSearchFilter());
		TraderAPI.API.RegisterSearchFilter(new AuctionSearchFilter());

		//Register Tax Reference Types (in case I add more taxable blocks in the future)
		TaxAPI.registerReferenceType(TaxableTraderReference.TYPE);

		//Register Bank Account Reference Types
		BankAPI.API.RegisterReferenceType(PlayerBankReference.TYPE);
		BankAPI.API.RegisterReferenceType(TeamBankReference.TYPE);

		//Register Menu Validator Types
		MenuValidatorType.register(SimpleValidator.TYPE);
		MenuValidatorType.register(BlockEntityValidator.TYPE);
		MenuValidatorType.register(BlockValidator.TYPE);

		//Initialize the Item Trade Restrictions
		ItemTradeRestriction.init();
		//Setup the ticket data for ticket kiosks and paygate traders
		TicketGroupData.create(ModItems.TICKET_MASTER.get(), ModItems.TICKET.get(), ModItems.TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_PAPER);
		TicketGroupData.create(ModItems.GOLDEN_TICKET_MASTER.get(), ModItems.GOLDEN_TICKET.get(), ModItems.GOLDEN_TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_GOLD);

		//Villager Trades
		VillagerTradeManager.registerDefaultTrades();
		ItemListingSerializer.registerDefaultSerializers();

		//Register Loot Modifiers
		LootManager.addLootModifier(ChocolateEventCoins.LOOT_MODIFIER);

		//Register Icon Data
		IconData.registerDefaultIcons();

		//Register Stat Types
		StatType.register(IntegerStat.INSTANCE);
		StatType.register(MultiMoneyStat.INSTANCE);

		//Setup Block Protection
		BlockProtectionHelper.ProtectBlock(b -> b instanceof IOwnableBlock);
		BlockProtectionHelper.ProtectBlock(b -> b instanceof CoinBlock);

	}
    
    private void clientSetup(final FMLClientSetupEvent event) { safeEnqueueWork(event, "Error during client setup!", PROXY::setupClient); }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
    	//Preload target
		Player target = event.getEntity();
    	//Sync time
		SPacketSyncTime.syncWith(target);
    	//Sync admin list
		LCAdminMode.sendSyncPacket(target);
    }

    public static void LogDebug(String message) { LOGGER.debug(message); }
    public static void LogDebug(String message, Object... objects) { LOGGER.debug(message, objects); }

    public static void LogInfo(String message)
    {
    	if(LCConfig.COMMON.debugLevel.get() > 0)
    		LOGGER.debug("INFO: " + message);
    	else
    		LOGGER.info(message);
    }

	public static void LogInfo(String message, Object... objects)
	{
		if(LCConfig.COMMON.debugLevel.get() > 0)
			LOGGER.debug("INFO: " + message, objects);
		else
			LOGGER.info(message, objects);
	}
    
    public static void LogWarning(String message)
    {
    	if(LCConfig.COMMON.debugLevel.get() > 1)
    		LOGGER.debug("WARN: " + message);
    	else
    		LOGGER.warn(message);
    }

	public static void LogWarning(String message, Object... objects)
	{
		if(LCConfig.COMMON.debugLevel.get() > 1)
			LOGGER.debug("WARN: " + message, objects);
		else
			LOGGER.warn(message, objects);
	}

    public static void LogError(String message)
    {
    	if(LCConfig.COMMON.debugLevel.get() > 2)
    		LOGGER.debug("ERROR: " + message);
    	else
    		LOGGER.error(message);
    }

	public static void LogError(String message, Object... objects)
	{
		if(LCConfig.COMMON.debugLevel.get() > 2)
			LOGGER.debug("ERROR: " + message, objects);
		else
			LOGGER.error(message, objects);
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

	private void setupConfigTasks(RegisterConfigurationTasksEvent event)
	{
		event.register(new SyncCoinDataTask(event.getListener()));
	}

	private static class SyncCoinDataTask implements ICustomConfigurationTask {

		private final ServerConfigurationPacketListener listener;
		private SyncCoinDataTask(@Nonnull ServerConfigurationPacketListener listener) { this.listener = listener; }

		@Override
		@Nonnull
		public Type type() { return SPacketSyncCoinData.CONFIG_TYPE; }

		@Override
		public void run(@Nonnull Consumer<CustomPacketPayload> sender) {
			sender.accept(CoinAPI.API.getSyncPacket().configTask());
			this.listener.finishCurrentTask(SPacketSyncCoinData.CONFIG_TYPE);
		}

	}
    
}
