package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketModifierSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.TicketSlot;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.client.ItemTradeButtonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event)
	{
		//LightmansCurrency.LogInfo("Registering Item Colors for Ticket Items");
		event.register(new TicketColor(), ModItems.TICKET.get(), ModItems.TICKET_MASTER.get());
	}

	@SubscribeEvent
	public static void stitchTextures(TextureStitchEvent.Pre event) {
		if(event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
			//Add coin/wallet slot backgrounds
			//LightmansCurrency.LogInfo("Adding empty slot sprites to the texture atlas.");
			event.addSprite(CoinSlot.EMPTY_COIN_SLOT);
			event.addSprite(TicketSlot.EMPTY_TICKET_SLOT);
			event.addSprite(WalletSlot.EMPTY_WALLET_SLOT);
			event.addSprite(EasySlot.EMPTY_SLOT_BG);
			event.addSprite(UpgradeInputSlot.EMPTY_UPGRADE_SLOT);
			event.addSprite(TicketModifierSlot.EMPTY_DYE_SLOT);
			event.addSprite(ItemTradeButtonRenderer.NBT_SLOT);
		}
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		for(FreezerBlock block : ModBlocks.FREEZER.getAll())
			event.register(block.getDoorModel());
	}
	
	@SubscribeEvent
	public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(ModLayerDefinitions.WALLET, WalletLayer::createLayer);
	}
	
	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event)
	{
		addWalletLayer(event,"default");
		addWalletLayer(event,"slim");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addWalletLayer(EntityRenderersEvent.AddLayers event, String skin)
	{
		EntityRenderer<? extends Player> renderer = event.getSkin(skin);
		if(renderer instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new WalletLayer<>(livingRenderer));
		}
	}
	
	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(ClientEvents.KEY_WALLET);
		if(LightmansCurrency.isCuriosLoaded())
			event.register(ClientEvents.KEY_PORTABLE_TERMINAL);
	}

	@SubscribeEvent
	public static void registerWalletGuiOverlay(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("wallet_hud", WalletDisplayOverlay.INSTANCE);
	}
	
}
