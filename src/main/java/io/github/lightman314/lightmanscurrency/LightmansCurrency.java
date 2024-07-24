package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
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
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
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
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.advancements.LCAdvancementTriggers;
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
import io.github.lightman314.lightmanscurrency.api.ticket.TicketData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import io.github.lightman314.lightmanscurrency.integration.biomesoplenty.BOPCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.integration.claiming.cadmus.LCCadmusIntegration;
import io.github.lightman314.lightmanscurrency.integration.claiming.flan.LCFlanIntegration;
import io.github.lightman314.lightmanscurrency.integration.discord.LCDiscord;
import io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks.LCFTBChunksIntegration;
import io.github.lightman314.lightmanscurrency.integration.ftbteams.LCFTBTeams;
import io.github.lightman314.lightmanscurrency.integration.quark.QuarkCustomWoodTypes;
import io.github.lightman314.lightmanscurrency.integration.supplementaries.LCSupplementaries;
import io.github.lightman314.lightmanscurrency.proxy.ClientProxy;
import io.github.lightman314.lightmanscurrency.proxy.CommonProxy;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.VillagerTradeManager;
import io.github.lightman314.lightmanscurrency.integration.immersiveengineering.LCImmersive;
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
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.time.SPacketSyncTime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);

        //Register configs
		LCConfig.init();
		LootManager.init();
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

		//Setup Wood Compatibilities before registering blocks/items
		IntegrationUtil.SafeRunIfLoaded("biomesoplenty", BOPCustomWoodTypes::setupWoodTypes, "Error setting up BOP wood types! BOP has probably changed their API!");
		IntegrationUtil.SafeRunIfLoaded("quark", QuarkCustomWoodTypes::setupWoodTypes, "Error setting up Quark wood types! Quark has probably changed their API!");

        //Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        //Register the proxy so that it can run custom events
        MinecraftForge.EVENT_BUS.register(PROXY);

		IntegrationUtil.SafeRunIfLoaded("lightmansdiscord", LCDiscord::setup, null);
		IntegrationUtil.SafeRunIfLoaded("ftbchunks", LCFTBChunksIntegration::setup, "Error setting up FTB Chunks chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("flan", LCFlanIntegration::setup, "Error setting up Flans chunk purchasing integration!");
		IntegrationUtil.SafeRunIfLoaded("immersiveengineering", LCImmersive::registerRotationBlacklists, null);
		IntegrationUtil.SafeRunIfLoaded("supplementaries", LCSupplementaries::setup, null);
        
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) { safeEnqueueWork(event, "Error during common setup!", this::commonSetupWork); }

	private void commonSetupWork(FMLCommonSetupEvent event) {

		//Manually load common config for villager edit purposes
		ConfigFile.loadServerFiles(ConfigFile.LoadPhase.SETUP);

		//Setup Cadmus Integration during common setup so that other mods will have already registered their claim providers
		IntegrationUtil.SafeRunIfLoaded("cadmus", LCCadmusIntegration::setup,null);

		//Setup Money System
		CoinAPI.API.Setup();
		//Register built-in Currency Types
		MoneyAPI.API.RegisterCurrencyType(CoinCurrencyType.INSTANCE);
		MoneyAPI.API.RegisterCurrencyType(NullCurrencyType.INSTANCE);

		LightmansCurrencyPacketHandler.init();

		//Register Crafting Conditions
		LCCraftingConditions.register();

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
		TraderAPI.API.RegisterTradeRule(DemandPricing.TYPE);

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
		TicketData.create(ModItems.TICKET_MASTER.get(), ModItems.TICKET.get(), ModItems.TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_PAPER);
		TicketData.create(ModItems.GOLDEN_TICKET_MASTER.get(), ModItems.GOLDEN_TICKET.get(), ModItems.GOLDEN_TICKET_STUB.get(), LCTags.Items.TICKET_MATERIAL_GOLD);

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

		//Register Advancement Triggers
		LCAdvancementTriggers.setup();

		//Setup Block Protection
		BlockProtectionHelper.ProtectBlock(b -> b instanceof IOwnableBlock);
		BlockProtectionHelper.ProtectBlock(b -> b instanceof CoinBlock);

		//Setup Mod Compats
		IntegrationUtil.SafeRunIfLoaded("ftbteams", LCFTBTeams::setup,"Error setting up FTB Teams compat!");

	}
    
    private void clientSetup(final FMLClientSetupEvent event) { safeEnqueueWork(event, "Error during client setup!", PROXY::setupClient); }
    
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
    
    /**
     * Easy public access to the equipped wallet.
     * Also confirms that the equipped wallet is either empty or a valid WalletItem.
     * Returns an empty stack if no wallet is equipped, or if the equipped item is not a valid wallet.
	 * @deprecated Use {@link CoinAPI#getWalletStack(Player)} instead
     */
	@Deprecated(since = "2.2.0.0")
    public static ItemStack getWalletStack(Player player)
    {
		if(player == null)
			return ItemStack.EMPTY;
    	return CoinAPI.API.getEquippedWallet(player);
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
