package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.ConfigReloadable;
import io.github.lightman314.lightmanscurrency.api.misc.BlockProtectionHelper;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.NullCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.*;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.PlayerOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.TeamOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.builtin.BookTextWriter;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.stats.types.*;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types.*;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientCoinSorter;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyType;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.SeasonalEventManager;
import io.github.lightman314.lightmanscurrency.common.traders.commands.CommandTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.advancements.LCAdvancementTriggers;
import io.github.lightman314.lightmanscurrency.common.blocks.CoinBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesCollectedNotification;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.TaxesPaidNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin.TaxableTraderReference;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import io.github.lightman314.lightmanscurrency.integration.biomesoplenty.BOPCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.integration.bwg.BWGCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.integration.claiming.cadmus.LCCadmusIntegration;
import io.github.lightman314.lightmanscurrency.integration.claiming.flan.LCFlanIntegration;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputercraftSetup;
import io.github.lightman314.lightmanscurrency.integration.create.LCCreate;
import io.github.lightman314.lightmanscurrency.integration.discord.LCDiscord;
import io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks.LCFTBChunksIntegration;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.LCFTBTeams;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorCompat;
import io.github.lightman314.lightmanscurrency.integration.quark.QuarkCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.proxy.*;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import io.github.lightman314.lightmanscurrency.integration.immersiveengineering.LCImmersive;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
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
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.time.SPacketSyncTime;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@Mod("lightmanscurrency")
public class LightmansCurrency {

	public static final String MODID = "lightmanscurrency";

	private static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	@Nonnull
	public static CommonProxy getProxy() { return PROXY; }


    private static final Logger LOGGER = LogManager.getLogger();

	public LightmansCurrency() {

		LootManager.registerDroplistListeners();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);

        //Register configs
		LCConfig.init();
		LootManager.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

		//Setup Wood Compatibilities before registering blocks/items
		IntegrationUtil.SafeRunIfLoaded("biomesoplenty", BOPCustomWoodTypes::setupWoodTypes, "Error setting up BOP wood types! BOP has probably changed their API!");
		IntegrationUtil.SafeRunIfLoaded("quark", QuarkCustomWoodTypes::setupWoodTypes, "Error setting up Quark wood types! Quark has probably changed their API!");
		IntegrationUtil.SafeRunIfLoaded("biomeswevegone", BWGCustomWoodTypes::setupWoodTypes, "Error setting up BWG wood types! BWG has probably changed their API!");
		//IntegrationUtil.SafeRunIfLoaded("tconstruct", TinkersCustomWoodTypes::setupWoodTypes, "Error setting up Tinkers' Construct wood types! Tinkers has probably changed their API!");

        //Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());

		//Initialize the proxy
        getProxy().init();

		IntegrationUtil.SafeRunIfLoaded("lightmansdiscord", LCDiscord::setup, null);
		IntegrationUtil.SafeRunIfLoaded("ftbchunks", LCFTBChunksIntegration::setup, "Error setting up FTB Chunks chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("flan", LCFlanIntegration::setup, "Error setting up Flans chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("immersiveengineering", LCImmersive::registerRotationBlacklists, null);
		IntegrationUtil.SafeRunIfLoaded("create", LCCreate::init, "Error settings up Create Integration!");
		IntegrationUtil.SafeRunIfLoaded("computercraft", LCComputercraftSetup::setup, "Error settings up ComputerCraft Integration!");

    }

    private void commonSetup(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

	private void commonSetupWork(FMLCommonSetupEvent event) {

		//Manually load common config for villager edit purposes
		ConfigFile.loadServerFiles(ConfigFile.LoadPhase.SETUP);

		//Setup Impactor compatibility
		IntegrationUtil.SafeRunIfLoaded("impactor", LCImpactorCompat::setup,"Error setting up Impactor Economy Compatibility");

		//Setup Cadmus Integration during common setup so that other mods will have already registered their claim providers
		IntegrationUtil.SafeRunIfLoaded("cadmus", LCCadmusIntegration::setup,null);

		//Setup Money System
		CoinAPI.getApi().Setup();
		//Register built-in Currency Types
		MoneyAPI.getApi().RegisterCurrencyType(CoinCurrencyType.INSTANCE);
		MoneyAPI.getApi().RegisterCurrencyType(NullCurrencyType.INSTANCE);
		//Ancient Money
		MoneyAPI.getApi().RegisterCurrencyType(AncientMoneyType.INSTANCE);
		CoinAPI.getApi().RegisterCustomSorter(AncientCoinSorter.INSTANCE);

		LightmansCurrencyPacketHandler.init();

		//Register Crafting Conditions
		LCCraftingConditions.register();

		//Ownership API data
		OwnershipAPI.getApi().registerOwnerType(Owner.NULL_TYPE);
		OwnershipAPI.getApi().registerOwnerType(FakeOwner.TYPE);
		OwnershipAPI.getApi().registerOwnerType(PlayerOwner.TYPE);
		OwnershipAPI.getApi().registerOwnerType(TeamOwner.TYPE);

		OwnershipAPI.getApi().registerPotentialOwnerProvider(PlayerOwnerProvider.INSTANCE);
		OwnershipAPI.getApi().registerPotentialOwnerProvider(TeamOwnerProvider.INSTANCE);

		//Initialize the TraderData deserializers
		TraderAPI.getApi().RegisterTrader(ItemTraderData.TYPE);
		TraderAPI.getApi().RegisterTrader(ItemTraderDataArmor.TYPE);
		TraderAPI.getApi().RegisterTrader(ItemTraderDataTicket.TYPE);
		TraderAPI.getApi().RegisterTrader(ItemTraderDataBook.TYPE);
		TraderAPI.getApi().RegisterTrader(SlotMachineTraderData.TYPE);
		TraderAPI.getApi().RegisterTrader(PaygateTraderData.TYPE);
		TraderAPI.getApi().RegisterTrader(AuctionHouseTrader.TYPE);
		TraderAPI.getApi().RegisterTrader(CommandTrader.TYPE);
		TraderAPI.getApi().RegisterTrader(GachaTrader.TYPE);

		//Register the custom game rules
		ModGameRules.registerRules();

		//Initialize the Trade Rule deserializers
		TraderAPI.getApi().RegisterTradeRule(PlayerListing.TYPE);
		TraderAPI.getApi().RegisterTradeRule(PlayerTradeLimit.TYPE);
		TraderAPI.getApi().RegisterTradeRule(PlayerDiscounts.TYPE);
		TraderAPI.getApi().RegisterTradeRule(TimedSale.TYPE);
		TraderAPI.getApi().RegisterTradeRule(TradeLimit.TYPE);
		TraderAPI.getApi().RegisterTradeRule(FreeSample.TYPE);
		TraderAPI.getApi().RegisterTradeRule(PriceFluctuation.TYPE);
		TraderAPI.getApi().RegisterTradeRule(DemandPricing.TYPE);
		TraderAPI.getApi().RegisterTradeRule(DailyTrades.TYPE);
		TraderAPI.getApi().RegisterTradeRule(DiscountCodes.TYPE);

		TradeRule.addLoadListener(PlayerListing.LISTENER);
		TradeRule.addIgnoreMissing("lightmanscurrency:whitelist");
		TradeRule.addIgnoreMissing("lightmanscurrency:blacklist");

		//Initialize the Notification deserializers
		NotificationAPI.getApi().RegisterNotification(ItemTradeNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(PaygateNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(SlotMachineTradeNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(OutOfStockNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(LowBalanceNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AuctionHouseSellerNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AuctionHouseBuyerNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AuctionHouseSellerNobidNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AuctionHouseBidNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AuctionHouseCancelNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(TextNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AddRemoveAllyNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(AddRemoveTradeNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeAllyPermissionNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeCreativeNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeNameNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeOwnerNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeSettingNotification.SIMPLE_TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeSettingNotification.ADVANCED_TYPE);
		NotificationAPI.getApi().RegisterNotification(ChangeSettingNotification.DUMB_TYPE);
		NotificationAPI.getApi().RegisterNotification(DepositWithdrawNotification.PLAYER_TYPE);
		NotificationAPI.getApi().RegisterNotification(DepositWithdrawNotification.CUSTOM_TYPE);
		NotificationAPI.getApi().RegisterNotification(DepositWithdrawNotification.SERVER_TYPE);
		NotificationAPI.getApi().RegisterNotification(BankTransferNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(BankInterestNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(TaxesCollectedNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(TaxesPaidNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(OwnableBlockEjectedNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(CommandTradeNotification.TYPE);
		NotificationAPI.getApi().RegisterNotification(GachaTradeNotification.TYPE);

		//Initialize the Notification Category deserializers
		NotificationAPI.getApi().RegisterCategory(NotificationCategory.GENERAL_TYPE);
		NotificationAPI.getApi().RegisterCategory(NullCategory.TYPE);
		NotificationAPI.getApi().RegisterCategory(EventCategory.TYPE);
		NotificationAPI.getApi().RegisterCategory(TraderCategory.TYPE);
		NotificationAPI.getApi().RegisterCategory(BankCategory.TYPE);
		NotificationAPI.getApi().RegisterCategory(AuctionHouseCategory.TYPE);
		NotificationAPI.getApi().RegisterCategory(TaxEntryCategory.TYPE);

		//Register Trader Search Filters
		TraderAPI.getApi().RegisterTraderSearchFilter(new BasicSearchFilter());
		TraderAPI.getApi().RegisterSearchFilter(new ItemTraderSearchFilter());
		TraderAPI.getApi().RegisterSearchFilter(new SlotMachineSearchFilter());
		TraderAPI.getApi().RegisterSearchFilter(new AuctionSearchFilter());
		TraderAPI.getApi().RegisterSearchFilter(new DescriptionSearchFilter());

        //Register Terminal Sort Types
        TraderAPI.getApi().RegisterSortType(SortByName.INSTANCE);
        TraderAPI.getApi().RegisterSortType(SortByID.INSTANCE);
        TraderAPI.getApi().RegisterSortType(SortByOffers.INSTANCE);
        TraderAPI.getApi().RegisterSortType(SortByPopularity.INSTANCE);
        TraderAPI.getApi().RegisterSortType(SortByRecent.INSTANCE);

		//Register Tax Reference Types (in case I add more taxable blocks in the future)
		TaxAPI.getApi().RegisterReferenceType(TaxableTraderReference.TYPE);

		//Register Bank Account Reference Types
		BankAPI.getApi().RegisterReferenceType(PlayerBankReference.TYPE);
		BankAPI.getApi().RegisterReferenceType(TeamBankReference.TYPE);

		//Register Menu Validator Types
		MenuValidatorType.register(SimpleValidator.TYPE);
		MenuValidatorType.register(BlockEntityValidator.TYPE);
		MenuValidatorType.register(BlockValidator.TYPE);
		MenuValidatorType.register(EntityValidator.TYPE);
		MenuValidatorType.register(ItemValidator.TYPE);

		//Initialize the Item Trade Restrictions
		ItemTradeRestriction.init();
		//Set up the ticket data for paygate traders
		//No longer used for Ticket Kiosk recipes, it will now obey kiosk-specific variants of the trades so that any trade disabling will be ignored
		TicketData.create(ModItems.TICKET_MASTER.get(), ModItems.TICKET.get(), ModItems.TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_PAPER);
		TicketData.create(ModItems.GOLDEN_TICKET_MASTER.get(), ModItems.GOLDEN_TICKET.get(), ModItems.GOLDEN_TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_GOLD);

		//Villager Trades
		ItemListingSerializer.registerDefaultSerializers();
		VillagerTradeManager.registerDefaultTrades();

		//Register Icon Data
		IconData.registerDefaultIcons();

		//Register Stat Types
		StatType.register(IntegerStat.INSTANCE);
		StatType.register(MultiMoneyStat.INSTANCE);

		//Register Advancement Triggers
		LCAdvancementTriggers.setup();

		//Setup Block Protection
		BlockProtectionHelper.ProtectBlock(b -> b instanceof IOwnableBlock);
		BlockProtectionHelper.ProtectBlock(b -> b instanceof CoinBlock);

		PrettyTextWriter.register(BookTextWriter.INSTANCE);

		//Register Custom Item Trades
		ItemTradeData.registerCustomItemTrade(TicketItemTrade.TYPE,TicketItemTrade::new);

        //Setup Config API Hooks
        //MONEY_PHASE delay so that it loads after common/client configs
        ConfigAPI.getApi().registerCustomReloadable(ConfigReloadable.simpleReloader(VersionUtil.lcResource("master_coin_list"),ConfigReloadable.PRIORITY_MONEY_PHASE,stack -> CoinAPI.getApi().ReloadCoinDataFromFile()));
        //AFTER_MONEY_PHASE so that it loads after server and money configs
        ConfigAPI.getApi().registerCustomReloadable(ConfigReloadable.simpleReloader(VersionUtil.lcResource("persistent_traders"),ConfigReloadable.PRIORITY_AFTER_ALL,stack -> TraderDataCache.TYPE.get(false).reloadPersistentTraders()));
        //Seasonal event don't care about money, so default priority it is
        ConfigAPI.getApi().registerCustomReloadable(ConfigReloadable.simpleReloader(VersionUtil.lcResource("seasonal_events"),stack -> SeasonalEventManager.reload()));

		//Setup Mod Compats
		IntegrationUtil.SafeRunIfLoaded("ftbteams", LCFTBTeams::setup,"Error setting up FTB Teams compat!");

	}

    private void clientSetup(final FMLClientSetupEvent event) { safeEnqueueWork(event, "Error during client setup!", getProxy()::setupClient); }

    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
    	event.register(IWalletHandler.class);
		event.register(IMoneyHandler.class);
    	event.register(IMoneyViewer.class);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
    	//Preload target
    	PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getEntity());
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

}
