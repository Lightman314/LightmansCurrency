package io.github.lightman314.lightmanscurrency;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.lightman314.lightmanscurrency.Reference.Colors;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.common.universal_traders.IUniversalDataDeserializer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;

import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.integration.Curios;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.config.MessageSyncConfig;
import io.github.lightman314.lightmanscurrency.network.message.extendedinventory.MessageUpdateWallet;
import io.github.lightman314.lightmanscurrency.proxy.*;
import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleDeserializer;
import io.github.lightman314.lightmanscurrency.tradedata.rules.PlayerBlacklist;
import io.github.lightman314.lightmanscurrency.tradedata.rules.PlayerTradeLimit;
import io.github.lightman314.lightmanscurrency.tradedata.rules.PlayerWhitelist;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod("lightmanscurrency")
public class LightmansCurrency {
	
	public static final String MODID = "lightmanscurrency";
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	private static boolean curiosLoaded = false;
	private static boolean backpackedLoaded = false;
	
	public static final ResourceLocation EMPTY_SLOTS = new ResourceLocation(MODID, "textures/gui/empty_slots.png");
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final CustomItemGroup COIN_GROUP = new CustomItemGroup(MODID + ".coins", () -> ModBlocks.COINPILE_GOLD);
    public static final CustomItemGroup MACHINE_GROUP = new CustomItemGroup(MODID + ".machines", () -> ModBlocks.MACHINE_ATM);
    public static final CustomItemGroup TRADING_GROUP = new CustomItemGroup(MODID + ".trading", () -> ModBlocks.DISPLAY_CASE);
    
    public LightmansCurrency() {
    	
    	//Common
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        //Client
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        //Inter-mod coms
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEnqueueIMC);
        //Config loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoad);
        
        //Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        curiosLoaded = ModList.get().isLoaded("curios");
        backpackedLoaded = ModList.get().isLoaded("backpacked");
        
    }
    
    public static boolean isBackpackedLoaded()
    {
    	return backpackedLoaded;
    }
    
    public static boolean isCuriosLoaded()
    {
    	return curiosLoaded;
    }
    
    private void doCommonStuff(final FMLCommonSetupEvent event)
    {
    	//LOGGER.info("PacketHandler init");
    	LightmansCurrencyPacketHandler.init();
    	
    	//Initialize coinList
    	MoneyUtil.init();
		
    	//Initialize the UniversalTraderData deserializers
    	IUniversalDataDeserializer.RegisterDeserializer(UniversalItemTraderData.TYPE, UniversalItemTraderData.DESERIALIZER);
    	
    	//Initialize the Trade Rule deserializers
    	ITradeRuleDeserializer.RegisterDeserializer(PlayerTradeLimit.TYPE, PlayerTradeLimit.DESERIALIZER);
    	ITradeRuleDeserializer.RegisterDeserializer(PlayerBlacklist.TYPE, PlayerBlacklist.DESERIALIZER);
    	ITradeRuleDeserializer.RegisterDeserializer(PlayerWhitelist.TYPE, PlayerWhitelist.DESERIALIZER);
    	
    	//Initialized the sorting lists
		COIN_GROUP.initSortingList(Arrays.asList(ModItems.COIN_COPPER, ModItems.COIN_IRON, ModItems.COIN_GOLD,
				ModItems.COIN_EMERALD, ModItems.COIN_DIAMOND, ModItems.COIN_NETHERITE, ModBlocks.COINPILE_COPPER.item,
				ModBlocks.COINPILE_IRON.item, ModBlocks.COINPILE_GOLD.item, ModBlocks.COINPILE_EMERALD.item,
				ModBlocks.COINPILE_DIAMOND.item, ModBlocks.COINPILE_NETHERITE.item, ModBlocks.COINBLOCK_COPPER.item,
				ModBlocks.COINBLOCK_IRON.item, ModBlocks.COINBLOCK_GOLD.item, ModBlocks.COINBLOCK_EMERALD.item,
				ModBlocks.COINBLOCK_DIAMOND.item, ModBlocks.COINBLOCK_NETHERITE.item, ModItems.TRADING_CORE, ModItems.TICKET,
				ModItems.WALLET_COPPER, ModItems.WALLET_IRON, ModItems.WALLET_GOLD, ModItems.WALLET_EMERALD, ModItems.WALLET_DIAMOND,
				ModItems.WALLET_NETHERITE
			));
		
		MACHINE_GROUP.initSortingList(Arrays.asList(ModBlocks.MACHINE_ATM.item, ModBlocks.MACHINE_MINT.item, ModBlocks.CASH_REGISTER.item,
				ModBlocks.TERMINAL.item, ModBlocks.PAYGATE.item, ModBlocks.TICKET_MACHINE.item
			));
		
		TRADING_GROUP.initSortingList(Arrays.asList(ModBlocks.SHELF.getItem(WoodType.OAK), ModBlocks.SHELF.getItem(WoodType.BIRCH),
				ModBlocks.SHELF.getItem(WoodType.SPRUCE), ModBlocks.SHELF.getItem(WoodType.JUNGLE),
				ModBlocks.SHELF.getItem(WoodType.ACACIA), ModBlocks.SHELF.getItem(WoodType.DARK_OAK),
				ModBlocks.SHELF.getItem(WoodType.CRIMSON), ModBlocks.SHELF.getItem(WoodType.WARPED),
				ModBlocks.DISPLAY_CASE.item, ModBlocks.ARMOR_DISPLAY.item, ModBlocks.CARD_DISPLAY.getItem(WoodType.OAK),
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
				ModBlocks.VENDING_MACHINE1.getItem(Colors.BLACK), ModBlocks.FREEZER.item,
				ModBlocks.VENDING_MACHINE2.getItem(Colors.WHITE), ModBlocks.VENDING_MACHINE2.getItem(Colors.ORANGE),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.MAGENTA), ModBlocks.VENDING_MACHINE2.getItem(Colors.LIGHTBLUE),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.YELLOW), ModBlocks.VENDING_MACHINE2.getItem(Colors.LIME),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.PINK), ModBlocks.VENDING_MACHINE2.getItem(Colors.GRAY),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.LIGHTGRAY), ModBlocks.VENDING_MACHINE2.getItem(Colors.CYAN),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.PURPLE), ModBlocks.VENDING_MACHINE2.getItem(Colors.BLUE),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.BROWN), ModBlocks.VENDING_MACHINE2.getItem(Colors.GREEN),
				ModBlocks.VENDING_MACHINE2.getItem(Colors.RED), ModBlocks.VENDING_MACHINE2.getItem(Colors.BLACK),
				ModBlocks.ITEM_TRADER_SERVER_SMALL.item, ModBlocks.ITEM_TRADER_SERVER_MEDIUM.item, ModBlocks.ITEM_TRADER_SERVER_LARGE.item,
				ModBlocks.ITEM_TRADER_SERVER_EXTRA_LARGE.item
			));
		
    }
    
    private void onEnqueueIMC(InterModEnqueueEvent event)
    {
    	
    	if(!curiosLoaded)
    		return;
    	
    	InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BELT.getMessageBuilder().build());
    	
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
    	
        PROXY.setupClient();
    	
    }
    
    //Ensures synchronization between the server and the clients for the "extra" item wallet slot on login
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	PlayerEntity player = event.getPlayer();
    	if(player.inventory instanceof IWalletInventory)
    	{
    		
    		ItemStack wallet = ItemStack.EMPTY;
    		//Get the wallet
    		if(player.inventory instanceof IWalletInventory)
    			wallet = ((IWalletInventory) player.inventory).getWalletItems().get(0);
    		
    		if(!wallet.isEmpty() && wallet.getItem() instanceof WalletItem)
    		{
    			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new MessageUpdateWallet(player.getEntityId(), wallet));
    		}
    		
    	}
    	
    }
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	if(event.phase != TickEvent.Phase.START)
    		return;
    	
    	PlayerEntity player = event.player;
    	if(!player.world.isRemote && player.inventory instanceof IWalletInventory)
    	{
    		IWalletInventory inventory = (IWalletInventory)player.inventory;
    		if(!inventory.getWalletArray().get(0).equals(inventory.getWalletItems().get(0)))
    		{
    			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new MessageUpdateWallet(player.getEntityId(), inventory.getWalletItems().get(0)));
    			inventory.getWalletArray().set(0, inventory.getWalletItems().get(0));
    		}
    	}
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlayerRenderScreen(GuiContainerEvent.DrawBackground event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	ContainerScreen<?> screen = event.getGuiContainer();
    	
        if(screen instanceof InventoryScreen)
        {
        	InventoryScreen inventoryScreen = (InventoryScreen) screen;
            int left = inventoryScreen.getGuiLeft();
            int top = inventoryScreen.getGuiTop();
            inventoryScreen.getMinecraft().getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
            Screen.blit(event.getMatrixStack(), left + 151, top + 61, 7, 7, 18, 18, 256, 256);
            
            WalletSlot.drawEmptyWalletSlots(screen, screen.getContainer(), event.getMatrixStack(), left, top);
        }
        else if(screen instanceof CreativeScreen)
        {
            CreativeScreen creativeScreen = (CreativeScreen) screen;
            if(creativeScreen.getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
            {
                int left = creativeScreen.getGuiLeft();
                int top = creativeScreen.getGuiTop();
                creativeScreen.getMinecraft().getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
                Screen.blit(event.getMatrixStack(), left + 152, top + 32, 7, 7, 18, 18, 256, 256);
                
                int walletSlotIndex = ExtendedPlayerInventory.WALLETINDEX + 5;
                if(walletSlotIndex < 0 && walletSlotIndex >= screen.getContainer().inventorySlots.size())
                {
                	LightmansCurrency.LogError("Calculated wallet slot index is out of bounds.");
                	return;
                }
                Slot walletSlot = screen.getContainer().inventorySlots.get(walletSlotIndex);
                if(!walletSlot.getHasStack())
                {
                	screen.getMinecraft().getTextureManager().bindTexture(LightmansCurrency.EMPTY_SLOTS);
                	screen.blit(event.getMatrixStack(), left + 153, top + 33, WalletSlot.EMPTY_SLOT_X, WalletSlot.EMPTY_SLOT_Y, 16, 16);
                }
                
                //WalletSlot.drawEmptyWalletSlots(screen, screen.getContainer(), event.getMatrixStack(), left, top);
                
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	PlayerEntity oldPlayer = event.getOriginal();
    	if(oldPlayer.inventory instanceof IWalletInventory && event.getPlayer().inventory instanceof IWalletInventory)
    	{
    		((IWalletInventory) event.getPlayer().inventory).copyWallet((IWalletInventory) oldPlayer.inventory);
    	}
    	
    }
    
    private void onConfigLoad(ModConfig.Loading event)
    {
    	if(event.getConfig().getModId().equals(MODID) && event.getConfig().getSpec() == Config.commonSpec)
    	{
    		//Only need to sync the common config
    		//LightmansCurrency.LOGGER.info("MONEY_CONFIG COMMON IS LOADED!!!!!");
    		Config.syncConfig();
    	}
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
    	LightmansCurrency.LogDebug("Player has logged in to the server. Sending config syncronization message.");
    	LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(event.getPlayer()), new MessageSyncConfig(Config.getSyncData()));
    }
    
    /**
     * Easy public access to the equipped wallet that functions regardless of which system (stand-alone, backpacked compatibility, curios) is being used to store the slot.
     */
    public static ItemStack getWalletStack(PlayerEntity player)
    {
    	AtomicReference<ItemStack> wallet = new AtomicReference<>(ItemStack.EMPTY);
    	
    	if(curiosLoaded)
    	{
    		wallet.set(Curios.getWalletStack(player));
    	}
    	else
    	{
    		if(player.inventory instanceof IWalletInventory)
        	{
    			IWalletInventory inventory = (IWalletInventory)player.inventory;
        		wallet.set(inventory.getWalletItems().get(0));
        	}
    	}
    	
    	return wallet.get();
    	
    }
    
    public static void LogDebug(String message)
    {
    	LOGGER.debug(message);
    }
    
    public static void LogInfo(String message)
    {
    	if(Config.COMMON.debugLevel.get() > 0)
    		LOGGER.debug("INFO: " + message);
    	else
    		LOGGER.info(message);
    }
    
    public static void LogWarning(String message)
    {
    	if(Config.COMMON.debugLevel.get() > 1)
    		LOGGER.debug("WARN: " + message);
    	else
    		LOGGER.warn(message);
    }
    
    public static void LogError(String message)
    {
    	if(Config.COMMON.debugLevel.get() > 2)
    		LOGGER.debug("ERROR: " + message);
    	else
    		LOGGER.error(message);
    }
    
}
