package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.colors.*;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.model.VariantBlockModel;
import io.github.lightman314.lightmanscurrency.client.model.VariantItemModel;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.*;
import io.github.lightman314.lightmanscurrency.client.renderer.item.GachaBallRenderer;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.GachaMachineBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event)
	{
		event.register(new TicketColor(),ModItems.TICKET.get(),ModItems.TICKET_PASS.get(),ModItems.TICKET_MASTER.get());
		event.register(new GoldenTicketColor(),ModItems.GOLDEN_TICKET_PASS.get(),ModItems.GOLDEN_TICKET_MASTER.get());
		event.register(new ATMCardColor(),ModItems.ATM_CARD.get(),ModItems.PREPAID_CARD.get());
		event.register(SusBlockColor.INSTANCE,ModBlocks.SUS_JAR.get());
		event.register(new GachaBallColor(),ModItems.GACHA_BALL.get());
		//Default Leather Colors for the leather wallet
		event.register((stack,layer) -> {
			if(stack.getItem() instanceof DyeableLeatherItem item)
				return item.getColor(stack);
			return -1;
		},ModItems.WALLET_LEATHER.get());
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
			event.register(block.getDoorModel());
		//Slot Machine Lights
		event.register(SlotMachineBlock.LIGHT_MODEL_LOCATION);
		//Bookshelf Traders
		event.register(NormalBookRenderer.MODEL_LOCATION);
		event.register(EnchantedBookRenderer.MODEL_LOCATION);
		//Wallets
		ForgeRegistries.ITEMS.forEach(item -> {
			if(item instanceof WalletItem wallet)
				event.register(wallet.model);
		});
		//Gacha Ball
		event.register(GachaBallRenderer.MODEL);
		//Gacha Machine Basic Graphics Models
		for(ResourceLocation model : GachaMachineBlock.BASIC_MODELS)
			event.register(model);
		ForgeRegistries.BLOCKS.forEach(block -> {
			if(block instanceof GachaMachineBlock b)
			{
				for(ResourceLocation model : b.getBasicModels())
					event.register(model);
			}
		});
	}

	@SubscribeEvent
	public static void onModelsBaked(ModelEvent.ModifyBakingResult event)
	{
		Map<ResourceLocation,BakedModel> modelRegistry = event.getModels();
		List<ModelResourceLocation> wrappedModels = new ArrayList<>();
		//Wrap each Variant Block item
		for(Block b : ForgeRegistries.BLOCKS)
		{
			if(b instanceof IVariantBlock block)
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
				//Also wrap the item model
				ModelResourceLocation itemModel = new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(b.asItem()),"inventory");
				BakedModel existingModel = modelRegistry.get(itemModel);
				if(existingModel != null)
				{
					modelRegistry.put(itemModel,new VariantItemModel(block,existingModel));
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
		addWalletLayer(event,"default");
		addWalletLayer(event,"slim");
	}
	
	@SuppressWarnings({ "rawtypes"})
	private static void addWalletLayer(EntityRenderersEvent.AddLayers event, String skin)
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
			event.register(ClientEvents.KEY_PORTABLE_TERMINAL);
			event.register(ClientEvents.KEY_PORTABLE_ATM);
		}
	}

	@SubscribeEvent
	public static void registerWalletGuiOverlay(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("wallet_hud", WalletDisplayOverlay.INSTANCE);
	}

	@SubscribeEvent
	public static void registerResourceListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(ItemPositionManager.INSTANCE);
		event.registerReloadListener(ItemPositionBlockManager.INSTANCE);
		event.registerReloadListener(CustomModelDataManager.INSTANCE);
		event.registerReloadListener(ModelVariantDataManager.INSTANCE);
	}
	
}
