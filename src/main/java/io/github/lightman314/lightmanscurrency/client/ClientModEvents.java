package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.colors.SusBlockColor;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event)
	{
		event.register(new TicketColor(), ModItems.TICKET.get(), ModItems.TICKET_PASS.get(), ModItems.TICKET_MASTER.get());
		event.register(new SusBlockColor.Item(), ModBlocks.SUS_JAR.get());
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.Block event)
	{
		event.register(new SusBlockColor(), ModBlocks.SUS_JAR.get());
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		for(FreezerBlock block : ModBlocks.FREEZER.getAll())
			event.register(block.getDoorModel());
		event.register(SlotMachineBlock.LIGHT_MODEL_LOCATION);
		event.register(NormalBookRenderer.MODEL_LOCATION);
		event.register(EnchantedBookRenderer.MODEL_LOCATION);
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
		{
			event.register(ClientEvents.KEY_PORTABLE_TERMINAL);
			event.register(ClientEvents.KEY_PORTABLE_ATM);
		}
	}

	@SubscribeEvent
	public static void registerWalletGuiOverlay(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("wallet_hud", WalletDisplayOverlay.INSTANCE);
	}
	
}
