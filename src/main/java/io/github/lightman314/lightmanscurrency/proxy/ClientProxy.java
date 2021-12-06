package io.github.lightman314.lightmanscurrency.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.BlockItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.ModLayerDefinitions;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.client.renderer.tileentity.*;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.integration.Curios;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ClientProxy extends CommonProxy{
	
	private long timeOffset = 0;
	
	@Override
	public void setupClient() {
		
		//Set Render Layers
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.DISPLAY_CASE.block, RenderType.cutout());
    	
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE1, RenderType.cutout());
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE2, RenderType.cutout());
    	
    	ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARMOR_DISPLAY.block, RenderType.cutout());
    	
    	//Register Screens
    	MenuScreens.register(ModContainers.INVENTORY_WALLET, PlayerInventoryWalletScreen::new);
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
    	BlockEntityRenderers.register(ModTileEntities.ITEM_TRADER, ItemTraderTileEntityRenderer::new);
    	BlockEntityRenderers.register(ModTileEntities.FREEZER_TRADER, FreezerTraderTileEntityRenderer::new);
    	
    	//Register Addable Trade Rules
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerWhitelist());
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerBlacklist());
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerTradeLimit());
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerDiscounts());
    	TradeRuleScreen.RegisterTradeRule(() -> new TimedSale());
    	
    	//Register ClientEvents
    	MinecraftForge.EVENT_BUS.register(new ClientEvents());
    	
    	//Register the key bind
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_WALLET);
    	
    	//Add wallet layer unless curios is loaded.
    	if(LightmansCurrency.isCuriosLoaded())
    	{
    		Curios.RegisterCuriosRenderers();
    	}
    	else
    	{
	    	Map<String, EntityRenderer<? extends Player>> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
	    	this.addWalletLayer((PlayerRenderer)skinMap.get("default"));
	    	this.addWalletLayer((PlayerRenderer)skinMap.get("slim"));
    	}
    	
	}
	
	private static void setRenderLayerForSet(BlockItemSet<?> blockItemSet, RenderType type)
	{
		blockItemSet.getAll().forEach(blockItemPair -> ItemBlockRenderTypes.setRenderLayer(blockItemPair.block, type));
	}
	
	private void addWalletLayer(PlayerRenderer renderer)
	{
		/*List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> layers = ObfuscationReflectionHelper.getPrivateValue(LivingEntityRenderer.class, renderer, "layers");
		if(layers != null)
		{
			layers.add(new WalletLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>(renderer, new ModelWallet<AbstractClientPlayer>(Minecraft.getInstance().getEntityModels().bakeLayer(ModLayerDefinitions.WALLET))));
		}*/
	}
	
	@Override
	public void initializeTraders(CompoundTag compound)
	{
		if(compound.contains("Traders", Tag.TAG_LIST))
		{
			List<UniversalTraderData> traders = new ArrayList<>();
			ListTag traderList = compound.getList("Traders", Tag.TAG_COMPOUND);
			traderList.forEach(nbt -> traders.add(TradingOffice.Deserialize((CompoundTag)nbt)));
			ClientTradingOffice.initData(traders);
		}
	}
	
	@Override
	public void updateTrader(CompoundTag compound)
	{
		ClientTradingOffice.updateTrader(compound);
	}
	
	@Override
	public void removeTrader(UUID traderID)
	{
		ClientTradingOffice.removeTrader(traderID);
	}
	
	@Override
	public void openTerminalScreen(Player player)
	{
		Minecraft.getInstance().setScreen(new TradingTerminalScreen(player));
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
	public void loadAdminPlayers(List<UUID> serverAdminList)
	{
		TradingOffice.loadAdminPlayers(serverAdminList);
	}
	
	
	public void registerItemColors(ColorHandlerEvent.Item event)
	{
		LightmansCurrency.LogInfo("Registering Item Colors for Ticket Items");
		event.getItemColors().register(new TicketColor(), ModItems.TICKET, ModItems.TICKET_MASTER);
	}
	
}
