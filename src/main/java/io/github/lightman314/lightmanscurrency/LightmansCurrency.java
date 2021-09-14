package io.github.lightman314.lightmanscurrency;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.Reference.Colors;
import io.github.lightman314.lightmanscurrency.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.common.universal_traders.IUniversalDataDeserializer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;

import io.github.lightman314.lightmanscurrency.containers.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.extendedinventory.ExtendedPlayerInventory;
import io.github.lightman314.lightmanscurrency.extendedinventory.IWalletInventory;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.config.MessageSyncConfig;
import io.github.lightman314.lightmanscurrency.network.message.extendedinventory.MessageUpdateWallet;
import io.github.lightman314.lightmanscurrency.proxy.*;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

@Mod("lightmanscurrency")
public class LightmansCurrency {
	
	public static final String MODID = "lightmanscurrency";
	
	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	private static boolean curiosLoaded = false;
	private static boolean backpackedLoaded = false;
	
	public static final ResourceLocation EMPTY_SLOTS = new ResourceLocation(MODID, "textures/gui/empty_slots.png");
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final CustomCreativeTab COIN_GROUP = new CustomCreativeTab(MODID + ".coins", () -> ModBlocks.COINPILE_GOLD);
    public static final CustomCreativeTab MACHINE_GROUP = new CustomCreativeTab(MODID + ".machines", () -> ModBlocks.MACHINE_ATM);
    public static final CustomCreativeTab TRADING_GROUP = new CustomCreativeTab(MODID + ".trading", () -> ModBlocks.DISPLAY_CASE);
    
    public LightmansCurrency() {
    	
    	//Common
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doCommonStuff);
        //Client
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        //Layer Registration
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerLayers);
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
        //backpackedLoaded = ModList.get().isLoaded("backpacked");
        
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
    
    private void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event)
    {
    	PROXY.registerLayers(event);
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
    //@SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	Player player = event.getPlayer();
    	if(player.getInventory() instanceof IWalletInventory)
    	{
    		
    		ItemStack wallet = ItemStack.EMPTY;
    		//Get the wallet
    		if(player.getInventory() instanceof IWalletInventory)
    			wallet = ((IWalletInventory) player.getInventory()).getWalletItems().get(0);
    		
    		if(!wallet.isEmpty() && wallet.getItem() instanceof WalletItem)
    		{
    			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new MessageUpdateWallet(player.getId(), wallet));
    		}
    		
    	}
    	
    }
    
    //@SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	if(event.phase != TickEvent.Phase.START)
    		return;
    	
    	Player player = event.player;
    	if(!player.level.isClientSide && player.getInventory() instanceof IWalletInventory)
    	{
    		IWalletInventory inventory = (IWalletInventory)player.getInventory();
    		if(!inventory.getWalletArray().get(0).equals(inventory.getWalletItems().get(0)))
    		{
    			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new MessageUpdateWallet(player.getId(), inventory.getWalletItems().get(0)));
    			inventory.getWalletArray().set(0, inventory.getWalletItems().get(0));
    		}
    	}
    }
    
    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlayerRenderScreen(GuiContainerEvent.DrawBackground event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	AbstractContainerScreen<?> screen = event.getGuiContainer();
    	
        if(screen instanceof InventoryScreen)
        {
        	InventoryScreen inventoryScreen = (InventoryScreen) screen;
            int left = inventoryScreen.getGuiLeft();
            int top = inventoryScreen.getGuiTop();
            
            RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
            //inventoryScreen.getMinecraft().getTextureManager().bindTexture(AbstractContainerScreen.INVENTORY_LOCATION);
            Screen.blit(event.getMatrixStack(), left + 151, top + 61, 7, 7, 18, 18, 256, 256);
            
            WalletSlot.drawEmptyWalletSlots(screen, screen.getMenu(), event.getMatrixStack(), left, top);
        }
        else if(screen instanceof CreativeModeInventoryScreen)
        {
        	CreativeModeInventoryScreen creativeScreen = (CreativeModeInventoryScreen) screen;
            if(creativeScreen.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId())
            {
                int left = creativeScreen.getGuiLeft();
                int top = creativeScreen.getGuiTop();
                RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
                //creativeScreen.getMinecraft().getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
                Screen.blit(event.getMatrixStack(), left + 152, top + 32, 7, 7, 18, 18, 256, 256);
                
                int walletSlotIndex = ExtendedPlayerInventory.WALLET_INDEXES.get(0) + 5;
                if(walletSlotIndex < 0 && walletSlotIndex >= screen.getMenu().slots.size())
                {
                	LightmansCurrency.LogError("Calculated wallet slot index is out of bounds.");
                	return;
                }
                Slot walletSlot = screen.getMenu().slots.get(walletSlotIndex);
                if(!walletSlot.hasItem())
                {
                	RenderSystem.setShaderTexture(0, LightmansCurrency.EMPTY_SLOTS);
                	//screen.getMinecraft().getTextureManager().bindTexture(LightmansCurrency.EMPTY_SLOTS);
                	screen.blit(event.getMatrixStack(), left + 153, top + 33, WalletSlot.EMPTY_SLOT_X, WalletSlot.EMPTY_SLOT_Y, 16, 16);
                }
                
                //WalletSlot.drawEmptyWalletSlots(screen, screen.getMenu(), event.getMatrixStack(), left, top);
                
            }
        }
    }
    
    //@SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
    	
    	if(curiosLoaded)
    		return;
    	
    	Player oldPlayer = event.getOriginal();
    	if(oldPlayer.getInventory() instanceof IWalletInventory && event.getPlayer().getInventory() instanceof IWalletInventory)
    	{
    		((IWalletInventory) event.getPlayer().getInventory()).copyWallet((IWalletInventory) oldPlayer.getInventory());
    	}
    	
    }
    
    private void onConfigLoad(ModConfigEvent.Loading event)
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
