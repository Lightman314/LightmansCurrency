package io.github.lightman314.lightmanscurrency;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.Reference.Colors;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.common.capability.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.ItemTraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.universal_traders.traderSearching.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.core.LootManager;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.datagen.RecipeGen;
import io.github.lightman314.lightmanscurrency.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.config.MessageSyncConfig;
import io.github.lightman314.lightmanscurrency.network.message.time.MessageSyncClientTime;
import io.github.lightman314.lightmanscurrency.proxy.*;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.PlayerBlacklist;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.PlayerDiscounts;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.PlayerTradeLimit;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.PlayerWhitelist;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TimedSale;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeLimit;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
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
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final CustomCreativeTab COIN_GROUP = new CustomCreativeTab(MODID + ".coins", () -> ModBlocks.COINPILE_GOLD);
    public static final CustomCreativeTab MACHINE_GROUP = new CustomCreativeTab(MODID + ".machines", () -> ModBlocks.MACHINE_ATM);
    public static final CustomCreativeTab TRADING_GROUP = new CustomCreativeTab(MODID + ".trading", () -> ModBlocks.DISPLAY_CASE);
    
    public LightmansCurrency() {
    	
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onDataSetup);
        
        //Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        //Register the proxy so that it can run custom events
        MinecraftForge.EVENT_BUS.register(PROXY);
        
    }
    
    private void doCommonStuff(final FMLCommonSetupEvent event)
    {
    	//LOGGER.info("PacketHandler init");
    	LightmansCurrencyPacketHandler.init();
    	
    	//Initialize coinList
    	MoneyUtil.init();
    	
    	//Initialize the UniversalTraderData deserializers
    	TradingOffice.RegisterDataType(UniversalItemTraderData.TYPE, () -> new UniversalItemTraderData());
    	
    	//Register the custom game rules
    	ModGameRules.registerRules();
    	
    	//Initialize the Trade Rule deserializers
    	TradeRule.RegisterDeserializer(PlayerWhitelist.TYPE, () -> new PlayerWhitelist());
    	TradeRule.RegisterDeserializer(PlayerBlacklist.TYPE, () -> new PlayerBlacklist());
    	TradeRule.RegisterDeserializer(PlayerTradeLimit.TYPE, () -> new PlayerTradeLimit());
    	TradeRule.RegisterDeserializer(PlayerDiscounts.TYPE, () -> new PlayerDiscounts());
    	TradeRule.RegisterDeserializer(TimedSale.TYPE, () -> new TimedSale());
    	TradeRule.RegisterDeserializer(TradeLimit.TYPE, () -> new TradeLimit());
    	
    	//Register Trader Search Filters
    	TraderSearchFilter.addFilter(new ItemTraderSearchFilter());
    	
    	//Initialized the sorting lists
		COIN_GROUP.initSortingList(Lists.newArrayList(ModItems.COIN_COPPER, ModItems.COIN_IRON, ModItems.COIN_GOLD,
				ModItems.COIN_EMERALD, ModItems.COIN_DIAMOND, ModItems.COIN_NETHERITE, ModBlocks.COINPILE_COPPER.item,
				ModBlocks.COINPILE_IRON.item, ModBlocks.COINPILE_GOLD.item, ModBlocks.COINPILE_EMERALD.item,
				ModBlocks.COINPILE_DIAMOND.item, ModBlocks.COINPILE_NETHERITE.item, ModBlocks.COINBLOCK_COPPER.item,
				ModBlocks.COINBLOCK_IRON.item, ModBlocks.COINBLOCK_GOLD.item, ModBlocks.COINBLOCK_EMERALD.item,
				ModBlocks.COINBLOCK_DIAMOND.item, ModBlocks.COINBLOCK_NETHERITE.item, ModItems.TRADING_CORE, ModItems.TICKET,
				ModItems.TICKET_MASTER, ModItems.WALLET_COPPER, ModItems.WALLET_IRON, ModItems.WALLET_GOLD, ModItems.WALLET_EMERALD,
				ModItems.WALLET_DIAMOND, ModItems.WALLET_NETHERITE
			));
		
		MACHINE_GROUP.initSortingList(Lists.newArrayList(ModBlocks.MACHINE_ATM.item, ModItems.PORTABLE_ATM, ModBlocks.MACHINE_MINT.item, ModBlocks.CASH_REGISTER.item,
				ModBlocks.TERMINAL.item, ModItems.PORTABLE_TERMINAL, ModBlocks.PAYGATE.item, ModBlocks.TICKET_MACHINE.item
			));
		
		TRADING_GROUP.initSortingList(Lists.newArrayList(ModBlocks.SHELF.getItem(WoodType.OAK), ModBlocks.SHELF.getItem(WoodType.BIRCH),
				ModBlocks.SHELF.getItem(WoodType.SPRUCE), ModBlocks.SHELF.getItem(WoodType.JUNGLE),
				ModBlocks.SHELF.getItem(WoodType.ACACIA), ModBlocks.SHELF.getItem(WoodType.DARK_OAK),
				ModBlocks.SHELF.getItem(WoodType.CRIMSON), ModBlocks.SHELF.getItem(WoodType.WARPED),
				ModBlocks.DISPLAY_CASE, ModBlocks.ARMOR_DISPLAY, ModBlocks.CARD_DISPLAY.getItem(WoodType.OAK),
				ModBlocks.CARD_DISPLAY.getItem(WoodType.BIRCH), ModBlocks.CARD_DISPLAY.getItem(WoodType.SPRUCE),
				ModBlocks.CARD_DISPLAY.getItem(WoodType.JUNGLE), ModBlocks.CARD_DISPLAY.getItem(WoodType.ACACIA),
				ModBlocks.CARD_DISPLAY.getItem(WoodType.DARK_OAK), ModBlocks.CARD_DISPLAY.getItem(WoodType.CRIMSON),
				ModBlocks.CARD_DISPLAY.getItem(WoodType.WARPED), ModBlocks.VENDING_MACHINE1.getItem(Colors.WHITE),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.ORANGE), ModBlocks.VENDING_MACHINE1.getItem(Colors.MAGENTA),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.LIGHTBLUE), ModBlocks.VENDING_MACHINE1.getItem(Colors.YELLOW),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.LIME), ModBlocks.VENDING_MACHINE1.getItem(Colors.PINK), 
				ModBlocks.VENDING_MACHINE1.getItem(Colors.GRAY), ModBlocks.VENDING_MACHINE1.getItem(Colors.LIGHTGRAY),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.CYAN), ModBlocks.VENDING_MACHINE1.getItem(Colors.PURPLE),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.BLUE), ModBlocks.VENDING_MACHINE1.getItem(Colors.BROWN),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.GREEN), ModBlocks.VENDING_MACHINE1.getItem(Colors.RED),
				ModBlocks.VENDING_MACHINE1.getItem(Colors.BLACK), ModBlocks.FREEZER,
				ModBlocks.VENDING_MACHINE2.getItem(Colors.WHITE), ModBlocks.VENDING_MACHINE2.getItem(Colors.ORANGE),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.MAGENTA), ModBlocks.VENDING_MACHINE2.getItem(Colors.LIGHTBLUE),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.YELLOW), ModBlocks.VENDING_MACHINE2.getItem(Colors.LIME),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.PINK), ModBlocks.VENDING_MACHINE2.getItem(Colors.GRAY),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.LIGHTGRAY), ModBlocks.VENDING_MACHINE2.getItem(Colors.CYAN),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.PURPLE), ModBlocks.VENDING_MACHINE2.getItem(Colors.BLUE),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.BROWN), ModBlocks.VENDING_MACHINE2.getItem(Colors.GREEN),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.RED), ModBlocks.VENDING_MACHINE2.getItem(Colors.BLACK),
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
    		//Only need to sync the common config
    		Config.syncConfig();
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
    	//Sync config
    	LightmansCurrency.LogDebug("Player has logged in to the server. Sending config syncronization message.");
    	LightmansCurrencyPacketHandler.instance.send(target, new MessageSyncConfig(Config.getSyncData()));
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
