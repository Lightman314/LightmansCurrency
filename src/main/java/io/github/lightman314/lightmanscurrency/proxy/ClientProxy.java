package io.github.lightman314.lightmanscurrency.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.BlockItemSet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.items.CoinItem;
import io.github.lightman314.lightmanscurrency.money.CoinData;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientProxy extends CommonProxy{
	
	boolean openTerminal = false;
	boolean openTeamManager = false;
	
	private long timeOffset = 0;
	
	@Override
	public void setupClient() {
		
		//Set Render Layers
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.DISPLAY_CASE.block, RenderType.cutout());
    	
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE1, RenderType.cutout());
    	setRenderLayerForSet(ModBlocks.VENDING_MACHINE2, RenderType.cutout());
    	
    	ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARMOR_DISPLAY.block, RenderType.cutout());
    	
    	//Register Screens
    	MenuScreens.register(ModMenus.ATM, ATMScreen::new);
    	MenuScreens.register(ModMenus.MINT, MintScreen::new);
    	MenuScreens.register(ModMenus.ITEM_TRADER, ItemTraderScreen::new);
    	MenuScreens.register(ModMenus.ITEM_TRADER_CR, ItemTraderScreen::new);
    	MenuScreens.register(ModMenus.ITEM_TRADER_UNIVERSAL, ItemTraderScreen::new);
    	
    	MenuScreens.register(ModMenus.ITEM_TRADER_STORAGE, ItemTraderStorageScreen::new);
    	MenuScreens.register(ModMenus.ITEM_TRADER_STORAGE_UNIVERSAL, ItemTraderStorageScreen::new);
    	
    	MenuScreens.register(ModMenus.ITEM_EDIT, ItemEditScreen::new);
    	MenuScreens.register(ModMenus.UNIVERSAL_ITEM_EDIT, ItemEditScreen::new);
    	MenuScreens.register(ModMenus.WALLET, WalletScreen::new);
    	MenuScreens.register(ModMenus.PAYGATE, PaygateScreen::new);
    	MenuScreens.register(ModMenus.TICKET_MACHINE, TicketMachineScreen::new);
    	
    	MenuScreens.register(ModMenus.ITEM_INTERFACE, ItemInterfaceScreen::new);
    	
    	//Register Tile Entity Renderers
    	BlockEntityRenderers.register(ModBlockEntities.ITEM_TRADER, ItemTraderBlockEntityRenderer::new);
    	BlockEntityRenderers.register(ModBlockEntities.FREEZER_TRADER, FreezerTraderBlockEntityRenderer::new);
    	
    	//Register Addable Trade Rules
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerWhitelist());
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerBlacklist());
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerTradeLimit());
    	TradeRuleScreen.RegisterTradeRule(() -> new PlayerDiscounts());
    	TradeRuleScreen.RegisterTradeRule(() -> new TimedSale());
    	TradeRuleScreen.RegisterTradeRule(() -> new TradeLimit());
    	
    	//Register the key bind
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_WALLET);
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_TEAM);
    	
    	//Wallet layer is now registered in ClientModEvents
    	
	}
	
	private static void setRenderLayerForSet(BlockItemSet<?> blockItemSet, RenderType type)
	{
		blockItemSet.getAll().forEach(blockItemPair -> ItemBlockRenderTypes.setRenderLayer(blockItemPair.block, type));
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
	
	public void initializeTeams(CompoundTag compound)
	{
		if(compound.contains("Teams", Tag.TAG_LIST))
		{
			List<Team> teams = Lists.newArrayList();
			ListTag teamList = compound.getList("Teams", Tag.TAG_COMPOUND);
			teamList.forEach(nbt -> teams.add(Team.load((CompoundTag)nbt)));
			ClientTradingOffice.initTeams(teams);
		}
	}
	
	public void updateTeam(CompoundTag compound)
	{
		ClientTradingOffice.updateTeam(compound);
	}
	
	public void removeTeam(UUID teamID)
	{
		ClientTradingOffice.removeTeam(teamID);
	}
	
	@Override
	public void initializeBankAccounts(CompoundTag compound)
	{
		if(compound.contains("BankAccounts", Tag.TAG_LIST))
		{
			Map<UUID,BankAccount> bank = new HashMap<>();
			ListTag bankList = compound.getList("BankAccounts", Tag.TAG_COMPOUND);
			for(int i = 0; i < bankList.size(); ++i)
			{
				CompoundTag tag = bankList.getCompound(i);
				UUID id = tag.getUUID("Player");
				BankAccount bankAccount = new BankAccount(tag);
				bank.put(id,bankAccount);
			}
			ClientTradingOffice.initBankAccounts(bank);
		}
	}
	
	@Override
	public void updateBankAccount(CompoundTag compound)
	{
		ClientTradingOffice.updateBankAccount(compound);
	}
	
	@Override
	public void openTerminalScreen()
	{
		this.openTerminal = true;
	}
	
	@Override
	public void openTeamManager()
	{
		this.openTeamManager = true;
	}
	
	@Override
	public void createTeamResponse(UUID teamID)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Screen openScreen = minecraft.screen;
		if(openScreen instanceof TeamManagerScreen)
		{
			TeamManagerScreen screen = (TeamManagerScreen)openScreen;
			screen.setActiveTeam(teamID);
		}
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
	
	@SubscribeEvent
	public void openScreenOnRenderTick(RenderTickEvent event)
	{
		if(event.phase == TickEvent.Phase.START)
		{
			if(this.openTerminal)
			{
				this.openTerminal = false;
				Minecraft.getInstance().setScreen(new TradingTerminalScreen());
			}
			else if(this.openTeamManager)
			{
				this.openTeamManager = false;
				Minecraft.getInstance().setScreen(new TeamManagerScreen());
			}
		}
	}
	
	@SubscribeEvent
	//Add coin value tooltips to non CoinItem coins.
	public void onItemTooltip(ItemTooltipEvent event) {
		Item item = event.getItemStack().getItem();
		CoinData coinData = MoneyUtil.getData(item);
		if(coinData != null && !(item instanceof CoinItem || item instanceof CoinBlockItem))
		{
			CoinItem.addCoinTooltips(event.getItemStack(), event.getToolTip());
		}
	}
	
}
