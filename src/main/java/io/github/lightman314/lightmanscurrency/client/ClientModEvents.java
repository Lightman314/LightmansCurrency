package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterATMIconRenderersEvent;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterTradeRenderManagersEvent;
import io.github.lightman314.lightmanscurrency.api.events.client.RegisterTradeRuleTabsEvent;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.renderer.builtin.BuiltInIconRenderer;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.client.colors.*;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.gui.screen.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.variant.*;
import io.github.lightman314.lightmanscurrency.client.model.VariantBlockModel;
import io.github.lightman314.lightmanscurrency.client.model.VariantItemModel;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.*;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.client.renderer.item.GachaBallRenderer;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.GachaMachineBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.*;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.client.AuctionTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.CommandTrade;
import io.github.lightman314.lightmanscurrency.common.traders.commands.tradedata.client.CommandTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tradedata.GachaTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tradedata.client.GachaTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.item.ticket.TicketItemTrade;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.client.ItemTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.client.PaygateTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.*;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.client.SlotMachineTradeButtonRenderer;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event)
	{
		event.register(new TicketColor(),ModItems.TICKET.get(),ModItems.TICKET_PASS.get(),ModItems.TICKET_MASTER.get());
		event.register(new CouponColor(),ModItems.COUPON.get());
		event.register(new GoldenTicketColor(),ModItems.GOLDEN_TICKET_PASS.get(),ModItems.GOLDEN_TICKET_MASTER.get());
		event.register(new ATMCardColor(),ModItems.ATM_CARD.get(),ModItems.PREPAID_CARD.get());
		event.register(SusBlockColor.INSTANCE,ModBlocks.SUS_JAR.get());
		event.register(new GachaBallColor(),ModItems.GACHA_BALL.get());
		//Default Leather Colors for the leather wallet
		event.register(new VanillaColor(),ModItems.WALLET_LEATHER.get(),ModItems.TRANSACTION_REGISTER.get());
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.Block event)
	{
		event.register(SusBlockColor.INSTANCE, ModBlocks.SUS_JAR.get());
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		//Freezer Doors
		for(FreezerBlock block : ModBlocks.FREEZER.getAll())
			event.register(ModelResourceLocation.standalone(block.getDoorModel()));
		//Slot Machine Lights
		event.register(ModelResourceLocation.standalone(SlotMachineBlock.LIGHT_MODEL_LOCATION));
		//Bookshelf Traders
		event.register(NormalBookRenderer.MODEL_LOCATION);
		event.register(EnchantedBookRenderer.MODEL_LOCATION);
		//Wallets
		BuiltInRegistries.ITEM.forEach(item -> {
			if(item instanceof WalletItem wallet)
			{
				ResourceLocation model = wallet.components().get(ModDataComponents.WALLET_MODEL.get());
				if(model != null)
					event.register(ModelResourceLocation.standalone(model));
			}
		});
		//Gacha Ball
		event.register(GachaBallRenderer.MODEL);
		//Gacha Machine Basic Graphics Models
		for(ResourceLocation model : GachaMachineBlock.BASIC_MODELS)
			event.register(ModelResourceLocation.standalone(model));
		BuiltInRegistries.BLOCK.forEach(block -> {
			if(block instanceof GachaMachineBlock b)
			{
				for(ResourceLocation model : b.getBasicModels())
					event.register(ModelResourceLocation.standalone(model));
			}
		});
	}

	@SubscribeEvent
	public static void onModelsBaked(ModelEvent.ModifyBakingResult event)
	{
		//Don't do anything if Model Variants aren't supposed to load
		Map<ModelResourceLocation,BakedModel> modelRegistry = event.getModels();
		List<ModelResourceLocation> wrappedModels = new ArrayList<>();
		//Wrap each Variant Block item
		for(Block b : BuiltInRegistries.BLOCK)
		{
            IVariantBlock block = VariantProvider.getVariantBlock(b);
			if(block != null)
			{
				for(BlockState state : b.getStateDefinition().getPossibleStates())
				{
					ModelResourceLocation modelID = BlockModelShaper.stateToModelLocation(state);
					BakedModel existingModel = modelRegistry.get(modelID);
					if(existingModel != null)
					{
						modelRegistry.put(modelID,new VariantBlockModel(block,existingModel));
						wrappedModels.add(modelID);
					}
					else
						LightmansCurrency.LogDebug("Missing block model:  " + modelID);
				}

			}
		}
        for(Item i : BuiltInRegistries.ITEM)
        {
            IVariantItem item = VariantProvider.getVariantItem(i);
            if(item != null)
            {
                //Wrap the item model
                ModelResourceLocation itemModel = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(i));
                BakedModel existingModel = modelRegistry.get(itemModel);
                if(existingModel != null)
                {
                    modelRegistry.put(itemModel,new VariantItemModel(item,existingModel));
                    wrappedModels.add(itemModel);
                }
                else
                    LightmansCurrency.LogWarning("Missing item model: " + itemModel);
            }
        }
		LightmansCurrency.LogDebug("Wrapped " + wrappedModels.size() + " models with a custom VariantBlockModel");
	}

	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event)
	{
		addWalletLayer(event,PlayerSkin.Model.WIDE);
		addWalletLayer(event,PlayerSkin.Model.SLIM);
	}

	@SuppressWarnings({ "rawtypes" })
	private static void addWalletLayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skin)
	{
		EntityRenderer<? extends Player> renderer = event.getSkin(skin);
		if(renderer instanceof LivingEntityRenderer livingRenderer)
			livingRenderer.addLayer(new WalletLayer<>(livingRenderer));
	}
	
	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(ClientEvents.KEY_WALLET);
		if(LCCurios.isLoaded())
		{
			event.register(ClientEvents.KEY_PORTABLE_ATM);
			event.register(ClientEvents.KEY_PORTABLE_TERMINAL);
		}
	}

	@SubscribeEvent
	public static void registerWalletGuiOverlay(RegisterGuiLayersEvent event) {
		event.registerAboveAll(VersionUtil.lcResource("wallet_hud"), WalletDisplayOverlay.INSTANCE);
	}

	@SubscribeEvent
	public static void registerScreens(@Nonnull RegisterMenuScreensEvent event)
	{
		event.register(ModMenus.ATM.get(), ATMScreen::new);
		event.register(ModMenus.MINT.get(), MintScreen::new);

		event.register(ModMenus.NETWORK_TERMINAL.get(), NetworkTerminalScreen::new);
		event.register(ModMenus.TRADER.get(), TraderScreen::new);
		event.register(ModMenus.TRADER_BLOCK.get(), TraderScreen::new);
		event.register(ModMenus.TRADER_NETWORK_ALL.get(), TraderScreen::new);

		event.register(ModMenus.TRADER_STORAGE.get(), TraderStorageScreen::new);

		event.register(ModMenus.SLOT_MACHINE.get(), SlotMachineScreen::new);
		event.register(ModMenus.GACHA_MACHINE.get(), GachaMachineScreen::new);

		event.register(ModMenus.WALLET.get(), WalletScreen::new);
		event.register(ModMenus.WALLET_BANK.get(), WalletBankScreen::new);
		event.register(ModMenus.TICKET_MACHINE.get(), TicketStationScreen::new);

		event.register(ModMenus.TRADER_INTERFACE.get(), TraderInterfaceScreen::new);

		event.register(ModMenus.EJECTION_RECOVERY.get(), EjectionRecoveryScreen::new);

		event.register(ModMenus.PLAYER_TRADE.get(), PlayerTradeScreen::new);

		event.register(ModMenus.COIN_CHEST.get(), CoinChestScreen::new);

		event.register(ModMenus.TAX_COLLECTOR.get(), TaxCollectorScreen::new);

		event.register(ModMenus.TEAM_MANAGEMENT.get(), TeamManagerScreen::new);

		event.register(ModMenus.NOTIFICATIONS.get(), NotificationScreen::new);

		event.register(ModMenus.ATM_CARD.get(), ATMCardScreen::new);

		event.register(ModMenus.VARIANT_SELECT_BLOCK.get(), BlockVariantSelectScreen::new);
		event.register(ModMenus.VARIANT_SELECT_ITEM.get(), ItemVariantSelectScreen::new);

        event.register(ModMenus.ITEM_FILTER.get(), ItemFilterScreen::new);
        event.register(ModMenus.TRANSACTION_REGISTER.get(), TransactionRegisterScreen::new);
	}

    @SubscribeEvent
    public static void registerTradeRuleTabBuilders(RegisterTradeRuleTabsEvent event)
    {
        event.register(DailyTrades.TYPE,DailyTradesTab::new);
        event.register(DemandPricing.TYPE,DemandPricingTab::new);
        event.register(DiscountCodes.TYPE,DiscountCodesTab::new);
        event.register(FreeSample.TYPE,FreeSampleTab::new);
        event.register(PlayerDiscounts.TYPE,PlayerDiscountTab::new);
        event.register(PlayerListing.TYPE,PlayerListingTab::new);
        event.register(PlayerTradeLimit.TYPE,PlayerTradeLimitTab::new);
        event.register(PriceFluctuation.TYPE,PriceFluctuationTab::new);
        event.register(TimedSale.TYPE,TimedSaleTab::new);
        event.register(TradeLimit.TYPE,TradeLimitTab::new);
    }

    @SubscribeEvent
    public static void regsterATMIconRenderers(RegisterATMIconRenderersEvent event)
    {
        event.registerSet(BuiltInIconRenderer.INSTANCE,ItemIcon.TYPE,SimpleArrowIcon.TYPE,SpriteIcon.TYPE);
    }

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event)
	{
		event.registerItem(LCItemRenderer.USE_LC_RENDERER,ModBlocks.COIN_CHEST.get().asItem(),ModItems.GACHA_BALL.get());
	}

	@SubscribeEvent
	public static void registerResourceListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(ItemPositionManager.INSTANCE);
		event.registerReloadListener(ItemPositionBlockManager.INSTANCE);
		event.registerReloadListener(CustomModelDataManager.INSTANCE);
		event.registerReloadListener(ModelVariantDataManager.INSTANCE);
	}

    @SubscribeEvent
    public static void registerTradeButtonRenderers(RegisterTradeRenderManagersEvent event)
    {
        event.register(AuctionTradeButtonRenderer::new,AuctionTradeData.class);
        event.register(CommandTradeButtonRenderer::new,CommandTrade.class);
        event.register(GachaTradeButtonRenderer::new,GachaTradeData.class);
        event.register(ItemTradeButtonRenderer::new,ItemTradeData.class,TicketItemTrade.class);
        event.register(PaygateTradeButtonRenderer::new,PaygateTradeData.class);
        event.register(SlotMachineTradeButtonRenderer::new,SlotMachineTrade.class);
    }
	
}
