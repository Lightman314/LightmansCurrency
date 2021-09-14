package io.github.lightman314.lightmanscurrency.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.BlockItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.ModLayerDefinitions;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.IUniversalDataDeserializer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.integration.Curios;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class ClientProxy extends CommonProxy{
	
	@Override
	public void setupClient() {
		
		//Set Render Layers
    	ItemBlockRenderTypes.setRenderLayer(ModBlocks.DISPLAY_CASE.block, RenderType.cutout());
    	
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE1, RenderType.cutout());
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE2, RenderType.cutout());
    	
    	ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARMOR_DISPLAY.block, RenderType.cutout());
    	
    	//Register Screens
    	MenuScreens.register(ModContainers.ATM, ATMScreen::new);
    	MenuScreens.register(ModContainers.MINT, MintScreen::new);
    	MenuScreens.register(ModContainers.ITEMTRADER, ItemTraderScreen::new);
    	MenuScreens.register(ModContainers.ITEMTRADERSTORAGE, ItemTraderStorageScreen::new);
    	MenuScreens.register(ModContainers.ITEMTRADERCR, ItemTraderScreenCR::new);
    	MenuScreens.register(ModContainers.ITEM_EDIT, ItemEditScreen::new);
    	MenuScreens.register(ModContainers.UNIVERSAL_ITEM_EDIT, ItemEditScreen::new);
    	MenuScreens.register(ModContainers.WALLET, WalletScreen::new);
    	MenuScreens.register(ModContainers.PAYGATE, PaygateScreen::new);
    	MenuScreens.register(ModContainers.TICKET_MACHINE, TicketMachineScreen::new);
    	MenuScreens.register(ModContainers.UNIVERSAL_ITEMTRADER, UniversalItemTraderScreen::new);
    	MenuScreens.register(ModContainers.UNIVERSAL_ITEMTRADERSTORAGE, UniversalItemTraderStorageScreen::new);
    	
    	//Register Tile Entity Renderers
    	BlockEntityRenderers.register(ModBlockEntities.ITEM_TRADER, ItemTraderBlockEntityRenderer::new);
    	BlockEntityRenderers.register(ModBlockEntities.FREEZER_TRADER, FreezerTraderBlockEntityRenderer::new);
    	
    	//Register ClientEvents
    	MinecraftForge.EVENT_BUS.register(new ClientEvents());
    	
    	//Register the key bind
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_WALLET);
    	
    	//Register Curios Renderers
    	if(LightmansCurrency.isCuriosLoaded())
    	{
    		Curios.RegisterCuriosRenderers();
    	}
    	else //Add wallet layer if curios is not loaded.
    	{
			Map<String, EntityRenderer<? extends Player>> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
	    	this.addWalletLayer((PlayerRenderer)skinMap.get("default"));
	    	this.addWalletLayer((PlayerRenderer)skinMap.get("slim"));
    	}
    	
	}
	
	@Override
	public void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(ModLayerDefinitions.WALLET, ModelWallet::createLayer);
	}
	
	private static void setRenderLayerForSet(BlockItemSet<?> blockItemSet, RenderType type)
	{
		blockItemSet.getAll().forEach(blockItemPair -> ItemBlockRenderTypes.setRenderLayer(blockItemPair.block, type));
	}
	
	private void addWalletLayer(PlayerRenderer renderer)
	{
		List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> layers = ObfuscationReflectionHelper.getPrivateValue(LivingEntityRenderer.class, renderer, "field_177097_h");
		if(layers != null)
		{
			layers.add(new WalletLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>(renderer, new ModelWallet<AbstractClientPlayer>(Minecraft.getInstance().getEntityModels().bakeLayer(ModLayerDefinitions.WALLET))));
		}
	}
	
	@Override
	public void updateTraders(CompoundTag compound)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.screen instanceof TerminalScreen)
		{
			if(compound.contains("Traders", Constants.NBT.TAG_LIST))
			{
				List<UniversalTraderData> traders = new ArrayList<>();
				ListTag traderList = compound.getList("Traders", Constants.NBT.TAG_COMPOUND);
				traderList.forEach(nbt -> traders.add(IUniversalDataDeserializer.Deserialize((CompoundTag)nbt)));
				((TerminalScreen)minecraft.screen).updateTraders(traders);
			}
		}
	}
	
	@Override
	public void openTerminalScreen(Player player)
	{
		Minecraft.getInstance().setScreen(new TerminalScreen(player));
	}
	
}
