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
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.client.builtin.ClientCoinType;
import io.github.lightman314.lightmanscurrency.api.money.client.builtin.ClientNullType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.renderer.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.MasterCoinListConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.EnchantedBookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.NormalBookRenderer;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.*;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.data.types.NotificationDataCache;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.MoneyBagItem;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.client.ClientAncientType;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.integration.curios.client.LCCuriosClient;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorClient;
import io.github.lightman314.lightmanscurrency.integration.patchouli.LCPatchouli;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ClientProxy extends CommonProxy{

	private long timeOffset = 0;

	private final Supplier<CoinChestBlockEntity> coinChestBE = Suppliers.memoize(() -> new CoinChestBlockEntity(BlockPos.ZERO, ModBlocks.COIN_CHEST.get().defaultBlockState()));

	@Override
	public boolean isClient() { return true; }

	@Override
	public void init(IEventBus eventBus, ModContainer modContainer) {
		modContainer.registerExtensionPoint(IConfigScreenFactory.class,ConfigSelectionScreen.mixedFactory(LCConfig.CLIENT,LCConfig.COMMON,LCConfig.SERVER,MasterCoinListConfigOption.INSTANCE));
		eventBus.addListener(this::registerVariantProperties);
	}

	@Override
	public void setupClient() {

		ConfigFile.loadClientFiles(ConfigFile.LoadPhase.SETUP);

    	//Register Screens
    	//Done in ClientModEvents#registerScreens
    	
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

        //Register Patchouli Functions
        IntegrationUtil.SafeRunIfLoaded("patchouli", LCPatchouli::init, "Error setting up Patchouli Compat!");

        //Register Item Position Rotation Handlers
        RotationHandler.setup();

        //Collect Trade Rule Tab Constructors
        TradeRulesClientTab.initialize();
        ATMIconRenderer.initialize();
        TradeRenderManager.initialize();

        //Register Client Money Types
        ClientMoneyAPI.getApi().RegisterClientType(ClientNullType.INSTANCE);
        ClientMoneyAPI.getApi().RegisterClientType(ClientCoinType.INSTANCE);
        ClientMoneyAPI.getApi().RegisterClientType(ClientAncientType.INSTANCE);
        IntegrationUtil.SafeRunIfLoaded("impactor", LCImpactorClient::setupClient, "Error setting up Impactor Compat!");

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
		if(NeoForge.EVENT_BUS.post(new NotificationEvent.NotificationReceivedOnClient(mc.player.getUUID(), NotificationDataCache.TYPE.get(true).getNotifications(mc.player), notification)).isCanceled())
			return;
		
		if(LCConfig.CLIENT.pushNotificationsToChat.get())
		{
			//Post the notification to chat
			for(Component line : notification.getChatMessage())
				mc.gui.getChat().addMessage(line);
		}

		
	}
	
	@Override
	public long getTimeDesync() { return this.timeOffset; }
	
	@Override
	public void setTimeDesync(long serverTime)
	{
		this.timeOffset = serverTime - System.currentTimeMillis();
		//Round the time offset to the nearest second
		this.timeOffset = (this.timeOffset / 1000) * 1000;
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

	@Nullable
	@Override
	public Player getLocalPlayer() { return Minecraft.getInstance().player; }

	@Override
	public RegistryAccess getClientRegistryHolder() {
		Level level = Minecraft.getInstance().level;
		if(level != null)
			return level.registryAccess();
		return null;
	}

}