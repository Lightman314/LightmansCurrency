package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.config.ConfigAPI;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.ConfigReloadable;
import io.github.lightman314.lightmanscurrency.api.misc.BlockProtectionHelper;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnershipAPI;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.PlayerOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin.TeamOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.builtin.BookTextWriter;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.builtin.BasicSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.builtin.DescriptionSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types.*;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.common.blocks.MoneyBagBlock;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientCoinSorter;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.SeasonalEventManager;
import io.github.lightman314.lightmanscurrency.common.blocks.CoinBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.*;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.builtin.TaxableTraderReference;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.common.traders.search_filters.AuctionSearchFilter;
import io.github.lightman314.lightmanscurrency.common.traders.search_filters.ItemTraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.traders.search_filters.SlotMachineSearchFilter;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import io.github.lightman314.lightmanscurrency.integration.biomesoplenty.BOPCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.integration.claiming.flan.LCFlanIntegration;
import io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks.LCFTBChunksIntegration;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCComputercraftLauncher;
import io.github.lightman314.lightmanscurrency.integration.create.LCCreate;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.integration.ftb_filter.LCFTBFilterSystemLauncher;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.LCFTBTeams;
import io.github.lightman314.lightmanscurrency.integration.immersiveengineering.LCImmersive;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorLauncher;
import io.github.lightman314.lightmanscurrency.network.message.data.SPacketSyncCoinData;
import io.github.lightman314.lightmanscurrency.proxy.*;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmanscurrency.common.core.ModRegistrySetup;
import io.github.lightman314.lightmanscurrency.common.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.network.message.time.SPacketSyncTime;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Consumer;

@Mod("lightmanscurrency")
public class LightmansCurrency {
	
	public static final String MODID = "lightmanscurrency";

	private static CommonProxy PROXY;
	public static CommonProxy getProxy() { return Objects.requireNonNull(PROXY,"Attempted to get the proxy before the mod was initialized!"); }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MODID,path); }

    private static final Logger LOGGER = LogManager.getLogger();
	public LightmansCurrency(ModContainer modContainer, IEventBus eventBus, Dist side) {

		//Init the proxy
		PROXY = side.isClient() ? new ClientProxy() : new CommonProxy();

        //Setup Wood Compatibilities super-early so we don't accidentally load items before they're initialized
        IntegrationUtil.SafeRunIfLoaded("biomesoplenty", BOPCustomWoodTypes::setupWoodTypes, "Error setting up BOP wood types! BOP has probably changed their API!");
        //IntegrationUtil.SafeRunIfLoaded("quark", QuarkCustomWoodTypes::setupWoodTypes, "Error setting up Quark wood types! Quark has probably changed their API!");

		LootManager.registerDroplistListeners();

		eventBus.addListener(this::commonSetup);
		eventBus.addListener(this::clientSetup);

        //Register configs
		LCConfig.init();
		LootManager.init();

        // Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

        //Setup Deferred Registries
        ModRegistrySetup.register(eventBus);
        
        //Initialize the proxy
		getProxy().init(eventBus,modContainer);

		IntegrationUtil.SafeRunIfLoaded("ftbchunks", LCFTBChunksIntegration::setup, "Error setting up FTB Chunks chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("flan", LCFlanIntegration::setup, "Error setting up Flans chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("immersiveengineering", LCImmersive::registerRotationBlacklists, null);
		IntegrationUtil.SafeRunIfLoaded("curios", () -> LCCurios.setup(eventBus), "Error setting up Curios Compatibility!");
		IntegrationUtil.SafeRunIfLoaded("create", () -> LCCreate.init(eventBus), "Error setting up Create Integration!");
		IntegrationUtil.SafeRunIfLoaded("computercraft", () -> LCComputercraftLauncher.setup(eventBus),"Error setting up ComputerCraft Integration!");
        IntegrationUtil.SafeRunIfLoaded("ftbfiltersystem", LCFTBFilterSystemLauncher::launch,"Error setting up FTB Filter System Integration!");
        //Setup here as it has to register an owner type
        IntegrationUtil.SafeRunIfLoaded("ftbteams",LCFTBTeams::setup,"Error setting up FTB Teams compat!");
        //Setup Impactor compatibility (moved to ctor since Currency Type is now a registry)
        IntegrationUtil.SafeRunIfLoaded("impactor", LCImpactorLauncher::setup,"Error setting up Impactor Economy Compatibility");

        //Register item variant providers for basic items
        VariantProvider.registerBasicVariantItem(ModItems.TRADING_CORE,ModItems.VARIANT_WAND,ModItems.ITEM_TRADE_FILTER);

    }
    
    private void commonSetup(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

	private void commonSetupWork(FMLCommonSetupEvent event) {

		//Manually load common config for villager edit purposes
		ConfigFile.loadServerFiles(ConfigFile.LoadPhase.SETUP);

		//Setup Cadmus Integration during common setup so that other mods will have already registered their claim providers
		//IntegrationUtil.SafeRunIfLoaded("cadmus",LCCadmusIntegration::setup,null);

		//Setup Money System
		CoinAPI.getApi().Setup();
		CoinAPI.getApi().RegisterCustomSorter(AncientCoinSorter.INSTANCE);

		OwnershipAPI.getApi().registerPotentialOwnerProvider(PlayerOwnerProvider.INSTANCE);
		OwnershipAPI.getApi().registerPotentialOwnerProvider(TeamOwnerProvider.INSTANCE);

		//Register the custom game rules
		ModGameRules.registerRules();

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
		TicketGroupData.create(ModItems.TICKET_MASTER.get(), ModItems.TICKET.get(), ModItems.TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_PAPER);
		TicketGroupData.create(ModItems.GOLDEN_TICKET_MASTER.get(), ModItems.GOLDEN_TICKET.get(), ModItems.GOLDEN_TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_GOLD);

		//Villager Trades
		ItemListingSerializer.registerDefaultSerializers();
		VillagerTradeManager.registerDefaultTrades();

		//Setup Block Protection
		BlockProtectionHelper.ProtectBlock(b -> b instanceof IOwnableBlock);
		BlockProtectionHelper.ProtectBlock(b -> b instanceof CoinBlock);
		BlockProtectionHelper.ProtectBlock(b -> b instanceof MoneyBagBlock);

		PrettyTextWriter.register(BookTextWriter.INSTANCE);

        //Setup Config API Hooks
        //Delay of 100 so that it loads after common/client configs
        ConfigAPI.getApi().registerCustomReloadable(ConfigReloadable.simpleReloader(LightmansCurrency.id("master_coin_list"),ConfigReloadable.PRIORITY_MONEY_PHASE, stack -> CoinAPI.getApi().ReloadCoinDataFromFile()));
        //Delay of 1000 so that it loads after server/money configs
        ConfigAPI.getApi().registerCustomReloadable(ConfigReloadable.simpleReloader(LightmansCurrency.id("persistent_traders"),ConfigReloadable.PRIORITY_AFTER_ALL,stack -> TraderDataCache.TYPE.get(false).reloadPersistentTraders()));
        //Seasonal events don't care about money or other such nonsense, so default priority is fine :)
        ConfigAPI.getApi().registerCustomReloadable(ConfigReloadable.simpleReloader(LightmansCurrency.id("seasonal_events"),stack -> SeasonalEventManager.reload()));

	}
    
    private void clientSetup(final FMLClientSetupEvent event) { safeEnqueueWork(event, "Error during client setup!", getProxy()::setupClient); }

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
		event.register(new SyncCoinDataTask());
	}

	private static class SyncCoinDataTask implements ICustomConfigurationTask {

		private SyncCoinDataTask() { }
		@Override
		public Type type() { return SPacketSyncCoinData.CONFIG_TYPE; }
		@Override
		public void run(Consumer<CustomPacketPayload> sender) {
			sender.accept(CoinAPI.getApi().getSyncPacket().configTask());
		}

	}
    
}
