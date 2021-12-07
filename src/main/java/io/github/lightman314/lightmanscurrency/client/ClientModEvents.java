package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.WalletSlot;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientModEvents {

	@SubscribeEvent
	public void registerItemColors(ColorHandlerEvent.Item event)
	{
		//LightmansCurrency.LogInfo("Registering Item Colors for Ticket Items");
		event.getItemColors().register(new TicketColor(), ModItems.TICKET, ModItems.TICKET_MASTER);
	}
	
	@SubscribeEvent
	public void stitchTextures(TextureStitchEvent.Pre event) {
		if(event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
			//Add coin/wallet slot backgrounds
			LightmansCurrency.LogInfo("Adding empty coin/ticket/wallet slot sprites to the texture atlas.");
			event.addSprite(CoinSlot.EMPTY_COIN_SLOT);
			event.addSprite(TicketSlot.EMPTY_TICKET_SLOT);
			event.addSprite(WalletSlot.EMPTY_WALLET_SLOT);
			event.addSprite(ItemTradeButton.DEFAULT_BACKGROUND);
		}
	}
	
	@SubscribeEvent
	public void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(ModLayerDefinitions.WALLET, WalletLayer::createLayer);
	}
	
	@SubscribeEvent
	public void addLayers(EntityRenderersEvent.AddLayers event)
	{
		addWalletLayer(event,"default");
		addWalletLayer(event,"slim");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addWalletLayer(EntityRenderersEvent.AddLayers event, String skin)
	{
		EntityRenderer<? extends Player> renderer = event.getSkin(skin);
		if(renderer instanceof LivingEntityRenderer)
		{
			LivingEntityRenderer livingRenderer = (LivingEntityRenderer)renderer;
			livingRenderer.addLayer(new WalletLayer<>(livingRenderer));
		}
	}
	
}
