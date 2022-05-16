package io.github.lightman314.lightmanscurrency;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.capability.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification.Category;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.ItemTraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.core.LootManager;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.datagen.RecipeGen;
import io.github.lightman314.lightmanscurrency.discord.CurrencyMessages;
import io.github.lightman314.lightmanscurrency.discord.DiscordListenerRegistration;
import io.github.lightman314.lightmanscurrency.enchantments.LCEnchantmentCategories;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.CustomPointsOfInterest;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.CustomProfessions;
import io.github.lightman314.lightmanscurrency.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.time.MessageSyncClientTime;
import io.github.lightman314.lightmanscurrency.proxy.*;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.*;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

@Mod("lightmanscurrency")
public class LightmansCurrency {
	
	public static final String MODID = "lightmanscurrency";
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final CustomCreativeTab COIN_GROUP = new CustomCreativeTab(MODID + ".coins", () -> ModBlocks.COINPILE_GOLD);
    public static final CustomCreativeTab MACHINE_GROUP = new CustomCreativeTab(MODID + ".machines", () -> ModBlocks.MACHINE_ATM);
    public static final CustomCreativeTab UPGRADE_GROUP = new CustomCreativeTab(MODID + ".upgrades", () -> ModItems.ITEM_CAPACITY_UPGRADE_1);
    public static final CustomCreativeTab TRADING_GROUP = new CustomCreativeTab(MODID + ".trading", () -> ModBlocks.DISPLAY_CASE);
    
    private static boolean discordIntegrationLoaded = false;
    public static boolean isDiscordIntegrationLoaded() { return discordIntegrationLoaded; }
    
	public LightmansCurrency() {
    	
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onDataSetup);
        
        //Setup Deferred Registries
        ModRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        //Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, ModBlockEntities::registerTypes);
        
        //Pre-register items/blocks
        ModItems.init();
        ModBlocks.init();
        CustomProfessions.init();
        CustomPointsOfInterest.init();
        
        //Register the proxy so that it can run custom events
        MinecraftForge.EVENT_BUS.register(PROXY);
        
        discordIntegrationLoaded = ModList.get().isLoaded("lightmansdiscord");
        
        if(discordIntegrationLoaded)
        {
        	MinecraftForge.EVENT_BUS.register(DiscordListenerRegistration.class);
        	MinecraftForge.EVENT_BUS.register(CurrencyMessages.class);
        }
        
    }
    
    private void doCommonStuff(final FMLCommonSetupEvent event)
    {
    	//LOGGER.info("PacketHandler init");
    	LightmansCurrencyPacketHandler.init();
    	
    	//Initialize the UniversalTraderData deserializers
    	TradingOffice.RegisterDataType(UniversalItemTraderData.TYPE, () -> new UniversalItemTraderData());
    	
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
    	
    	//Initialize the Notification deserializers
    	Notification.register(ItemTradeNotification.TYPE, ItemTradeNotification::new);
    	Notification.register(OutOfStockNotification.TYPE, OutOfStockNotification::new);
    	Notification.register(LowBalanceNotification.TYPE, LowBalanceNotification::new);
    	
    	//Initialize the Notification Category deserializers
    	Category.register(Category.GENERAL_TYPE, compound -> Category.GENERAL);
    	Category.register(TraderCategory.TYPE, TraderCategory::new);
    	Category.register(BankCategory.TYPE, BankCategory::new);
    	
    	//Register Trader Search Filters
    	TraderSearchFilter.addFilter(new ItemTraderSearchFilter());
    	
    	//Register Upgrade Types
    	MinecraftForge.EVENT_BUS.post(new UpgradeType.RegisterUpgradeTypeEvent());
    	
    	//Initialized the sorting lists
    	COIN_GROUP.setEnchantmentCategories(LCEnchantmentCategories.WALLET_CATEGORY, LCEnchantmentCategories.WALLET_PICKUP_CATEGORY);
		COIN_GROUP.initSortingList(Lists.newArrayList(ModItems.COIN_COPPER, ModItems.COIN_IRON, ModItems.COIN_GOLD,
				ModItems.COIN_EMERALD, ModItems.COIN_DIAMOND, ModItems.COIN_NETHERITE, ModBlocks.COINPILE_COPPER,
				ModBlocks.COINPILE_IRON, ModBlocks.COINPILE_GOLD, ModBlocks.COINPILE_EMERALD,
				ModBlocks.COINPILE_DIAMOND, ModBlocks.COINPILE_NETHERITE, ModBlocks.COINBLOCK_COPPER,
				ModBlocks.COINBLOCK_IRON, ModBlocks.COINBLOCK_GOLD, ModBlocks.COINBLOCK_EMERALD,
				ModBlocks.COINBLOCK_DIAMOND, ModBlocks.COINBLOCK_NETHERITE, ModItems.TRADING_CORE, ModItems.TICKET,
				ModItems.TICKET_MASTER, ModItems.TICKET_STUB, ModItems.WALLET_COPPER, ModItems.WALLET_IRON, ModItems.WALLET_GOLD,
				ModItems.WALLET_EMERALD, ModItems.WALLET_DIAMOND, ModItems.WALLET_NETHERITE
			));
		
		MACHINE_GROUP.initSortingList(Lists.newArrayList(ModBlocks.MACHINE_ATM, ModItems.PORTABLE_ATM, ModBlocks.MACHINE_MINT, ModBlocks.CASH_REGISTER,
				ModBlocks.TERMINAL, ModItems.PORTABLE_TERMINAL, ModBlocks.ITEM_TRADER_INTERFACE, ModBlocks.PAYGATE, ModBlocks.TICKET_MACHINE
			));
		
		UPGRADE_GROUP.initSortingList(Lists.newArrayList(ModItems.ITEM_CAPACITY_UPGRADE_1, ModItems.ITEM_CAPACITY_UPGRADE_2,
				ModItems.ITEM_CAPACITY_UPGRADE_3, ModItems.SPEED_UPGRADE_1, ModItems.SPEED_UPGRADE_2, ModItems.SPEED_UPGRADE_3,
				ModItems.SPEED_UPGRADE_4, ModItems.SPEED_UPGRADE_5
			));
		
		TRADING_GROUP.initSortingList(Lists.newArrayList(ModBlocks.SHELF_OAK, ModBlocks.SHELF_BIRCH, ModBlocks.SHELF_SPRUCE,
				ModBlocks.SHELF_JUNGLE, ModBlocks.SHELF_ACACIA, ModBlocks.SHELF_DARK_OAK, ModBlocks.SHELF_CRIMSON,
				ModBlocks.SHELF_WARPED, ModBlocks.DISPLAY_CASE, ModBlocks.ARMOR_DISPLAY, ModBlocks.CARD_DISPLAY_OAK,
				ModBlocks.CARD_DISPLAY_BIRCH, ModBlocks.CARD_DISPLAY_SPRUCE, ModBlocks.CARD_DISPLAY_JUNGLE,
				ModBlocks.CARD_DISPLAY_ACACIA, ModBlocks.CARD_DISPLAY_DARK_OAK, ModBlocks.CARD_DISPLAY_CRIMSON,
				ModBlocks.CARD_DISPLAY_WARPED, ModBlocks.VENDING_MACHINE, ModBlocks.VENDING_MACHINE_ORANGE,
				ModBlocks.VENDING_MACHINE_MAGENTA, ModBlocks.VENDING_MACHINE_LIGHTBLUE, ModBlocks.VENDING_MACHINE_YELLOW,
				ModBlocks.VENDING_MACHINE_LIME, ModBlocks.VENDING_MACHINE_PINK, ModBlocks.VENDING_MACHINE_GRAY,
				ModBlocks.VENDING_MACHINE_LIGHTGRAY, ModBlocks.VENDING_MACHINE_CYAN, ModBlocks.VENDING_MACHINE_PURPLE,
				ModBlocks.VENDING_MACHINE_BLUE, ModBlocks.VENDING_MACHINE_BROWN, ModBlocks.VENDING_MACHINE_GREEN,
				ModBlocks.VENDING_MACHINE_RED, ModBlocks.VENDING_MACHINE_BLACK, ModBlocks.FREEZER,
				ModBlocks.VENDING_MACHINE_LARGE, ModBlocks.VENDING_MACHINE_LARGE_ORANGE,
				ModBlocks.VENDING_MACHINE_LARGE_MAGENTA, ModBlocks.VENDING_MACHINE_LARGE_LIGHTBLUE,
				ModBlocks.VENDING_MACHINE_LARGE_YELLOW, ModBlocks.VENDING_MACHINE_LARGE_LIME,
				ModBlocks.VENDING_MACHINE_LARGE_PINK, ModBlocks.VENDING_MACHINE_LARGE_GRAY,
				ModBlocks.VENDING_MACHINE_LARGE_LIGHTGRAY, ModBlocks.VENDING_MACHINE_LARGE_CYAN,
				ModBlocks.VENDING_MACHINE_LARGE_PURPLE, ModBlocks.VENDING_MACHINE_LARGE_BLUE,
				ModBlocks.VENDING_MACHINE_LARGE_BROWN, ModBlocks.VENDING_MACHINE_LARGE_GREEN,
				ModBlocks.VENDING_MACHINE_LARGE_RED, ModBlocks.VENDING_MACHINE_LARGE_BLACK,
				ModBlocks.TICKET_KIOSK, ModBlocks.ITEM_TRADER_SERVER_SMALL, ModBlocks.ITEM_TRADER_SERVER_MEDIUM,
				ModBlocks.ITEM_TRADER_SERVER_LARGE, ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE
			));
		
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
    	
        PROXY.setupClient();
        
    }
    
    private void onConfigLoad(ModConfigEvent.Loading event)
    {
    	if(event.getConfig().getModId().equals(MODID) && event.getConfig().getSpec() == Config.commonSpec)
    	{
    		//Have the loot manager validate the entity loot contents
    		LootManager.validateEntityDropList();
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
    	PacketTarget target = LightmansCurrencyPacketHandler.getTarget(event.getPlayer());
    	//Sync time
    	LightmansCurrencyPacketHandler.instance.send(target, new MessageSyncClientTime());
    	//Sync admin list
    	LightmansCurrencyPacketHandler.instance.send(target, TradingOffice.getAdminSyncMessage());
    	
    }
    
    private void onDataSetup(GatherDataEvent event)
    {
    	DataGenerator dataGenerator = event.getGenerator();
    	dataGenerator.addProvider(new RecipeGen(dataGenerator));
    }
    
    /**
     * Easy public access to the equipped wallet.
     * Also confirms that the equipped wallet is either empty or a valid WalletItem.
     * Returns an empty stack if no wallet is equipped, or if the equipped item is not a valid wallet.
     */
    public static ItemStack getWalletStack(Player player)
    {
    	AtomicReference<ItemStack> wallet = new AtomicReference<>(ItemStack.EMPTY);
    	WalletCapability.getWalletHandler(player).ifPresent(walletHandler ->{
			wallet.set(walletHandler.getWallet());
		});
    	//Safety check to confirm that the Item Stack found is a valid wallet
    	if(!WalletItem.validWalletStack(wallet.get()))
    	{
    		LightmansCurrency.LogError(player.getName().getString() + "'s equipped wallet is not a valid WalletItem.");
    		LightmansCurrency.LogError("Equipped wallet is of type " + wallet.get().getItem().getClass().getName());
			return ItemStack.EMPTY;
    	}
    	return wallet.get();
    	
    }
    
    public static void LogDebug(String message)
    {
    	LOGGER.debug(message);
    }
    
    public static void LogInfo(String message)
    {
    	if(Config.COMMON != null && Config.COMMON.debugLevel.get() > 0)
    		LOGGER.debug("INFO: " + message);
    	else
    		LOGGER.info(message);
    }
    
    public static void LogWarning(String message)
    {
    	if(Config.COMMON != null && Config.COMMON.debugLevel.get() > 1)
    		LOGGER.debug("WARN: " + message);
    	else
    		LOGGER.warn(message);
    }
    
    public static void LogError(String message, Object... objects)
    {
    	if(Config.COMMON != null && Config.COMMON.debugLevel.get() > 2)
    		LOGGER.debug("ERROR: " + message, objects);
    	else
    		LOGGER.error(message, objects);
    }
    
    public static void LogError(String message)
    {
    	if(Config.COMMON != null && Config.COMMON.debugLevel.get() > 2)
    		LOGGER.debug("ERROR: " + message);
    	else
    		LOGGER.error(message);
    }
    
}
