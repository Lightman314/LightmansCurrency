package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigSelectionScreen;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterVariantPropertiesEvent;
import io.github.lightman314.lightmanscurrency.client.gui.screen.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.MasterCoinListConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.EnchantedBookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.NormalBookRenderer;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.capability.event_unlocks.CapabilityEventUnlocks;
import io.github.lightman314.lightmanscurrency.common.capability.event_unlocks.IEventUnlocks;
import io.github.lightman314.lightmanscurrency.common.core.*;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.data.types.NotificationDataCache;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.MoneyBagItem;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.integration.curios.client.LCCuriosClient;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ClientProxy extends CommonProxy{

	private long timeOffset = 0;

	private final Supplier<CoinChestBlockEntity> coinChestBE = Suppliers.memoize(() -> new CoinChestBlockEntity(BlockPos.ZERO, ModBlocks.COIN_CHEST.get().defaultBlockState()));

	@Override
	public boolean isClient() { return true; }

	public void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerVariantProperties);
        FMLModContainer container = FMLJavaModLoadingContext.get().getContainer();
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,Suppliers.memoize(() -> ConfigSelectionScreen.mixedFactory(container,LCConfig.CLIENT,LCConfig.COMMON,LCConfig.SERVER,MasterCoinListConfigOption.INSTANCE)));
	}

	@Override
	public void setupClient() {

		ConfigFile.loadClientFiles(ConfigFile.LoadPhase.SETUP);

    	//Register Screens
    	MenuScreens.register(ModMenus.ATM.get(), ATMScreen::new);
    	MenuScreens.register(ModMenus.MINT.get(), MintScreen::new);

		MenuScreens.register(ModMenus.NETWORK_TERMINAL.get(), NetworkTerminalScreen::new);
    	MenuScreens.register(ModMenus.TRADER.get(), TraderScreen::new);
    	MenuScreens.register(ModMenus.TRADER_BLOCK.get(), TraderScreen::new);
    	MenuScreens.register(ModMenus.TRADER_NETWORK_ALL.get(), TraderScreen::new);

    	MenuScreens.register(ModMenus.TRADER_STORAGE.get(), TraderStorageScreen::new);

    	MenuScreens.register(ModMenus.SLOT_MACHINE.get(), SlotMachineScreen::new);
    	MenuScreens.register(ModMenus.GACHA_MACHINE.get(), GachaMachineScreen::new);

    	MenuScreens.register(ModMenus.WALLET.get(), WalletScreen::new);
    	MenuScreens.register(ModMenus.WALLET_BANK.get(), WalletBankScreen::new);
    	MenuScreens.register(ModMenus.TICKET_MACHINE.get(), TicketStationScreen::new);
    	
    	MenuScreens.register(ModMenus.TRADER_INTERFACE.get(), TraderInterfaceScreen::new);
    	
    	MenuScreens.register(ModMenus.EJECTION_RECOVERY.get(), EjectionRecoveryScreen::new);

		MenuScreens.register(ModMenus.PLAYER_TRADE.get(), PlayerTradeScreen::new);

		MenuScreens.register(ModMenus.COIN_CHEST.get(), CoinChestScreen::new);

		MenuScreens.register(ModMenus.TAX_COLLECTOR.get(), TaxCollectorScreen::new);

		MenuScreens.register(ModMenus.TEAM_MANAGEMENT.get(), TeamManagerScreen::new);

		MenuScreens.register(ModMenus.NOTIFICATIONS.get(), NotificationScreen::new);

		MenuScreens.register(ModMenus.ATM_CARD.get(), ATMCardScreen::new);

		MenuScreens.register(ModMenus.VARIANT_SELECT.get(), VariantSelectScreen::new);

		MenuScreens.register(ModMenus.ITEM_FILTER.get(), ItemFilterScreen::new);

    	//Register Tile Entity Renderers
    	BlockEntityRenderers.register(ModBlockEntities.ITEM_TRADER.get(), ItemTraderBlockEntityRenderer::create);
    	BlockEntityRenderers.register(ModBlockEntities.FREEZER_TRADER.get(), FreezerTraderBlockEntityRenderer::create);
		BlockEntityRenderers.register(ModBlockEntities.SLOT_MACHINE_TRADER.get(), SlotMachineBlockEntityRenderer::create);
		BlockEntityRenderers.register(ModBlockEntities.BOOK_TRADER.get(), BookTraderBlockEntityRenderer::create);
		BlockEntityRenderers.register(ModBlockEntities.AUCTION_STAND.get(), AuctionStandBlockEntityRenderer::create);
		BlockEntityRenderers.register(ModBlockEntities.COIN_CHEST.get(), CoinChestRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.GACHA_MACHINE.get(), GachaMachineBlockEntityRenderer::create);

		//Setup Item Edit blacklists
		ItemEditWidget.BlacklistCreativeTabs(CreativeModeTabs.HOTBAR, CreativeModeTabs.INVENTORY, CreativeModeTabs.SEARCH, CreativeModeTabs.OP_BLOCKS);
		ItemEditWidget.BlacklistItem(s -> s.getItem() instanceof TicketItem);
		//Add written book to Item Edit item list (for purchase/barter possibilities with NBT enforcement turned off)
		ItemEditWidget.AddExtraItemAfter(new ItemStack(Items.WRITTEN_BOOK), Items.WRITABLE_BOOK);

		//Setup Book Renderers
		BookRenderer.register(NormalBookRenderer.GENERATOR);
		BookRenderer.register(EnchantedBookRenderer.GENERATOR);

		//Setup custom item renderers
		LCItemRenderer.registerBlockEntitySource(this::checkForCoinChest);

		//Setup custom item property
		ItemProperties.register(ModItems.COIN_ANCIENT.get(), AncientCoinItem.PROPERTY,
				(stack,level,player,seed) -> {
					AncientCoinType type = AncientCoinItem.getAncientCoinType(stack);
					return type == null ? 0f : type.ordinal() + 1f;
				});
		ItemProperties.register(ModBlocks.MONEY_BAG.get().asItem(), MoneyBagItem.PROPERTY,
				(stack,level,player,seed) -> (float)MoneyBagItem.getSize(stack));

		//Register Curios Render Layers
		if(LCCurios.isLoaded())
			LCCuriosClient.registerRenderLayers();

        //Register Item Position Rotation Handlers
        RotationHandler.setup();

	}

	private void registerVariantProperties(RegisterVariantPropertiesEvent event) {
		//Register Variant Properties
		event.register(VersionUtil.lcResource("item_position_data"),VariantProperties.ITEM_POSITION_DATA);
		event.register(VersionUtil.lcResource("freezer_door"),VariantProperties.FREEZER_DOOR_DATA);
		event.register(VersionUtil.lcResource("input_display_offset"),VariantProperties.INPUT_DISPLAY_OFFSET);
		event.register(VersionUtil.lcResource("tooltip_info"),VariantProperties.TOOLTIP_INFO);
		event.register(VersionUtil.lcResource("show_in_creative"),VariantProperties.SHOW_IN_CREATIVE);
		event.register(VersionUtil.lcResource("hidden"),VariantProperties.HIDDEN);
	}

	private BlockEntity checkForCoinChest(Block block)
	{
		if(block == ModBlocks.COIN_CHEST.get())
			return coinChestBE.get();
		return null;
	}

	@Override
	public void receiveNotification(Notification notification)
	{
		
		Minecraft mc = Minecraft.getInstance();
		assert mc.player != null;
		if(MinecraftForge.EVENT_BUS.post(new NotificationEvent.NotificationReceivedOnClient(mc.player.getUUID(), NotificationDataCache.TYPE.get(true).getNotifications(mc.player), notification)))
			return;
		
		if(LCConfig.CLIENT.pushNotificationsToChat.get()) //Post the notification to chat
		{
			for(Component line : notification.getChatMessage())
				mc.gui.getChat().addMessage(line);
		}

	}

	@Override
	public long getTimeDesync()
	{
		return timeOffset;
	}
	
	@Override
	public void setTimeDesync(long serverTime)
	{
		this.timeOffset = serverTime - System.currentTimeMillis();
		//Round the time offset to the nearest second
		this.timeOffset = (timeOffset / 1000) * 1000;
		if(this.timeOffset < 10000) //Ignore offset if less than 10s, as it's likely due to ping
			this.timeOffset = 0;
	}
	
	@Override
	public void loadAdminPlayers(List<UUID> serverAdminList) { LCAdminMode.loadAdminPlayers(serverAdminList); }
	
	@Override
	public void playCoinSound() {
		if(LCConfig.CLIENT.moneyMendingClink.get())
		{
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.COINS_CLINKING.get(), 1f, 0.4f));
		}
	}

	@Nullable
	@Override
	public Level getDimension(boolean isClient, ResourceKey<Level> type) {
		if(isClient)
		{
			Minecraft mc = Minecraft.getInstance();
			if(mc.level != null && mc.level.dimension().location().equals(type.location()))
				return mc.level;
			return null;
		}
		return super.getDimension(isClient,type);
	}

	@Override
	public Level safeGetDummyLevel() {
		Level level = this.getDummyLevelFromServer();
		if(level == null)
			level = Minecraft.getInstance().level;
		if(level != null)
			return level;
		LightmansCurrency.LogWarning("Could not get dummy level from client, as there is no active level!");
		return null;
	}

    @Override
    public boolean getHasPermissionsSetting() {
        Player player = this.getLocalPlayer();
        if(player == null)
            return false;
        return Minecraft.getInstance().options.operatorItemsTab().get() && player.canUseGameMasterBlocks();
    }

	@Override
	public void loadPlayerTrade(ClientPlayerTrade trade) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player.containerMenu instanceof PlayerTradeMenu menu)
			menu.reloadTrade(trade);
	}

	@Override
	public void syncEventUnlocks(List<String> unlocksList) {
		Minecraft mc = Minecraft.getInstance();
		IEventUnlocks unlocks = CapabilityEventUnlocks.getCapability(mc.player);
		if(unlocks != null)
			unlocks.sync(unlocksList);
	}

	@Override
	public void sendClientMessage(Component message)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
			player.sendSystemMessage(message);
	}

	@Override
	public List<GameProfile> getPlayerList(boolean logicalClient) {
		if(!logicalClient)
			return super.getPlayerList(logicalClient);
		return Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map(PlayerInfo::getProfile).toList();
	}

	@Override
	public boolean isSelf(Player player) { return player == Minecraft.getInstance().player; }

	@Override
	public Player getLocalPlayer() { return Minecraft.getInstance().player; }

}