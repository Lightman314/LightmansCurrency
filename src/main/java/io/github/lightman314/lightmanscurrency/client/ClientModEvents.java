package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.colors.ATMCardColor;
import io.github.lightman314.lightmanscurrency.client.colors.GoldenTicketColor;
import io.github.lightman314.lightmanscurrency.client.colors.SusBlockColor;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.gui.screen.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.*;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event)
	{
		event.register(new TicketColor(), ModItems.TICKET.get(), ModItems.TICKET_PASS.get(), ModItems.TICKET_MASTER.get());
		event.register(new GoldenTicketColor(), ModItems.GOLDEN_TICKET_PASS.get(), ModItems.GOLDEN_TICKET_MASTER.get());
		event.register(new ATMCardColor(),ModItems.ATM_CARD.get(),ModItems.PREPAID_CARD.get());
		event.register(SusBlockColor.INSTANCE, ModBlocks.SUS_JAR.get());
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.Block event)
	{
		event.register(SusBlockColor.INSTANCE, ModBlocks.SUS_JAR.get());
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		for(FreezerBlock block : ModBlocks.FREEZER.getAll())
			registerModel(event,block.getDoorModel());
		registerModel(event,SlotMachineBlock.LIGHT_MODEL_LOCATION);
		event.register(NormalBookRenderer.MODEL_LOCATION);
		event.register(EnchantedBookRenderer.MODEL_LOCATION);
	}

	private static void registerModel(@Nonnull ModelEvent.RegisterAdditional event, @Nonnull ResourceLocation model)
	{
		event.register(new ModelResourceLocation(model,ModelResourceLocation.STANDALONE_VARIANT));
	}

	@SubscribeEvent
	public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(ModLayerDefinitions.WALLET, WalletLayer::createLayer);
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
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"wallet_hud"), WalletDisplayOverlay.INSTANCE);
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

		event.register(ModMenus.WALLET.get(), WalletScreen::new);
		event.register(ModMenus.WALLET_BANK.get(), WalletBankScreen::new);
		event.register(ModMenus.TICKET_MACHINE.get(), TicketStationScreen::new);

		event.register(ModMenus.TRADER_INTERFACE.get(), TraderInterfaceScreen::new);

		event.register(ModMenus.EJECTION_RECOVERY.get(), EjectionRecoveryScreen::new);

		event.register(ModMenus.PLAYER_TRADE.get(), PlayerTradeScreen::new);

		event.register(ModMenus.COIN_CHEST.get(), CoinChestScreen::new);

		event.register(ModMenus.TAX_COLLECTOR.get(), TaxCollectorScreen::new);

		event.register(ModMenus.COIN_MANAGEMENT.get(), CoinManagementScreen::new);

		event.register(ModMenus.TEAM_MANAGEMENT.get(), TeamManagerScreen::new);

		event.register(ModMenus.NOTIFICATIONS.get(), NotificationScreen::new);

		event.register(ModMenus.ATM_CARD.get(), ATMCardScreen::new);
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event)
	{
		event.registerItem(LCItemRenderer.USE_LC_RENDERER,ModBlocks.COIN_CHEST.get().asItem());
	}
	
}
