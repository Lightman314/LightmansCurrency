package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigSelectionScreen;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterVariantPropertiesEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.IconRenderer;
import io.github.lightman314.lightmanscurrency.api.misc.icons.client.builtin.*;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.*;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.client.builtin.ClientCoinType;
import io.github.lightman314.lightmanscurrency.api.money.client.builtin.ClientNullType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.ATMItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.client.ATMIconRenderer;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.client.builtin.BuiltInIconRenderer;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.builtin.*;
import io.github.lightman314.lightmanscurrency.api.traders.rules.builtin.*;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeRenderManager;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.MasterCoinListConfigOption;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.rules.client.builtin.*;
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
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.common.traders.commands.client.ClientCommandTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.commands.nodes.CommandTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.client.ClientItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.client.ClientPaygateTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.client.ClientTicketStubNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.PaygateTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.TicketStubNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.client.ClientSlotMachineNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.integration.IntegrationUtil;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.integration.curios.client.LCCuriosClient;
import io.github.lightman314.lightmanscurrency.integration.impactor.LCImpactorClient;
import io.github.lightman314.lightmanscurrency.integration.patchouli.LCPatchouli;
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
        TradeRenderManager.initialize();

        //Register Client Money Types
        ClientMoneyAPI.getApi().RegisterClientType(ClientNullType.INSTANCE);
        ClientMoneyAPI.getApi().RegisterClientType(ClientCoinType.INSTANCE);
        ClientMoneyAPI.getApi().RegisterClientType(ClientAncientType.INSTANCE);
        IntegrationUtil.SafeRunIfLoaded("impactor", LCImpactorClient::setupClient, "Error setting up Impactor Compat!");

        //Register Client ATM Icons
        ATMIconRenderer.REGISTRY.registerBatch(BuiltInIconRenderer.INSTANCE, ATMItemIcon.TYPE,SimpleArrowIcon.TYPE,SpriteIcon.TYPE);

        //Register Icon Renderers
        IconRenderer.register(IconIcon.TYPE,new IconIconRenderer());
        IconRenderer.register(ImageIcon.TYPE,new ImageIconRenderer());
        IconRenderer.register(ItemIcon.TYPE,new ItemIconRenderer());
        IconRenderer.register(MultiIcon.TYPE,new MultiIconRenderer());
        IconRenderer.register(NumberIcon.TYPE,new NumberIconRenderer());
        IconRenderer.register(TextIcon.TYPE,new TextIconRenderer());

        //Register Client Trader Nodes
        ClientTraderNode.registerClientNode(AdminNode.TYPE,ClientAdminNode::new);
        ClientTraderNode.registerClientNode(AlliesNode.TYPE,ClientAlliesNode::new);
        ClientTraderNode.registerClientNode(BankNode.TYPE,ClientBankNode::new);
        ClientTraderNode.registerClientNode(DisplayNode.TYPE,ClientDisplayNode::new);
        ClientTraderNode.registerClientNode(InputNode.TYPE,ClientInputNode::new);
        ClientTraderNode.registerClientNode(InterfaceSupportNode.TYPE,ClientInterfaceSupportNode::new);
        ClientTraderNode.registerClientNode(LoggerNode.TYPE,ClientLoggerNode::new);
        ClientTraderNode.registerClientNode(MachineAccessNode.TYPE,ClientMachineAccessNode::new);
        ClientTraderNode.registerClientNode(MoneyStorageNode.TYPE,ClientMoneyStorageNode::new);
        ClientTraderNode.registerClientNode(NormalTraderNode.TYPE,ClientNormalNode::new);
        ClientTraderNode.registerClientNode(OwnerNode.TYPE,ClientOwnerNode::new);
        ClientTraderNode.registerClientNode(PersistentDataNode.TYPE,ClientPersistentDataNode::new);
        ClientTraderNode.registerClientNode(TaxesNode.TYPE,ClientTaxesNode::new);
        ClientTraderNode.registerClientNode(TraderRulesNode.TYPE,ClientTraderRulesNode::new);
        ClientTraderNode.registerClientNode(WorldStateNode.TYPE,ClientWorldStateNode::new);

        //Item Client Nodes
        ClientTraderNode.registerClientNode(ItemTradeNode.TYPE,ClientItemTradeNode::new);
        //Paygate Client Nodes
        ClientTraderNode.registerClientNode(TicketStubNode.TYPE,ClientTicketStubNode::new);
        ClientTraderNode.registerClientNode(PaygateTradeNode.TYPE, ClientPaygateTradeNode::new);
        //Command Client Nodes
        ClientTraderNode.registerClientNode(CommandTradeNode.TYPE,ClientCommandTradeNode::new);
        //Slot Machine Client Nodes
        ClientTraderNode.registerClientNode(SlotMachineNode.TYPE,ClientSlotMachineNode::new);

        //Trade Rule Builder
        TradeRulesClientTab.REGISTRY.register(DailyTrades.TYPE, DailyTradesTab::new);
        TradeRulesClientTab.REGISTRY.register(DemandPricing.TYPE, DemandPricingTab::new);
        TradeRulesClientTab.REGISTRY.register(DiscountCodes.TYPE, DiscountCodesTab::new);
        TradeRulesClientTab.REGISTRY.register(FreeSample.TYPE, FreeSampleTab::new);
        TradeRulesClientTab.REGISTRY.register(PlayerDiscounts.TYPE, PlayerDiscountTab::new);
        TradeRulesClientTab.REGISTRY.register(PlayerListing.TYPE,PlayerListingTab::new);
        TradeRulesClientTab.REGISTRY.register(PlayerTradeLimit.TYPE,PlayerTradeLimitTab::new);
        TradeRulesClientTab.REGISTRY.register(PriceFluctuation.TYPE,PriceFluctuationTab::new);
        TradeRulesClientTab.REGISTRY.register(TimedSale.TYPE,TimedSaleTab::new);
        TradeRulesClientTab.REGISTRY.register(TradeLimit.TYPE,TradeLimitTab::new);


	}

	private void registerVariantProperties(RegisterVariantPropertiesEvent event) {
		//Register Variant Properties
		event.register(LightmansCurrency.id("item_position_data"),VariantProperties.ITEM_POSITION_DATA);
		event.register(LightmansCurrency.id("freezer_door"),VariantProperties.FREEZER_DOOR_DATA);
		event.register(LightmansCurrency.id("input_display_offset"),VariantProperties.INPUT_DISPLAY_OFFSET);
		event.register(LightmansCurrency.id("tooltip_info"),VariantProperties.TOOLTIP_INFO);
		event.register(LightmansCurrency.id("show_in_creative"),VariantProperties.SHOW_IN_CREATIVE);
		event.register(LightmansCurrency.id("hidden"),VariantProperties.HIDDEN);
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