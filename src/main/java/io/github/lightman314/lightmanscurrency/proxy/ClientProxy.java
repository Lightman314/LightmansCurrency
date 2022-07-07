package io.github.lightman314.lightmanscurrency.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.client.util.PlayerSuggestionsUtil;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.items.CoinItem;
import io.github.lightman314.lightmanscurrency.money.CoinData;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientProxy extends CommonProxy{
	
	boolean openTerminal = false;
	boolean openTeamManager = false;
	boolean openNotifications = false;
	
	private long timeOffset = 0;
	
	@Override
	public void setupClient() {
		
		//Set Render Layers
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.DISPLAY_CASE, RenderType.cutout());
		
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_ORANGE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_MAGENTA, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LIGHTBLUE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_YELLOW, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LIME, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_PINK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_GRAY, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LIGHTGRAY, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_CYAN, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_PURPLE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_BLUE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_BROWN, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_GREEN, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_RED, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_BLACK, RenderType.cutout());
		
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_ORANGE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_MAGENTA, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_LIGHTBLUE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_YELLOW, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_LIME, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_PINK, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_GRAY, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_LIGHTGRAY, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_CYAN, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_PURPLE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_BLUE, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_BROWN, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_GREEN, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_RED, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE_BLACK, RenderType.cutout());
    	
    	ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARMOR_DISPLAY, RenderType.cutout());
    	
    	//Register Screens
    	MenuScreens.register(ModMenus.ATM, ATMScreen::new);
    	MenuScreens.register(ModMenus.MINT, MintScreen::new);
    	
    	MenuScreens.register(ModMenus.TRADER, TraderScreen::new);
    	MenuScreens.register(ModMenus.TRADER_UNIVERSAL, TraderScreen::new);
    	
    	MenuScreens.register(ModMenus.TRADER_STORAGE, TraderStorageScreen::new);
    	MenuScreens.register(ModMenus.TRADER_STORAGE_UNIVERSAL, TraderStorageScreen::new);
    	
    	MenuScreens.register(ModMenus.WALLET, WalletScreen::new);
    	MenuScreens.register(ModMenus.WALLET_BANK, WalletBankScreen::new);
    	MenuScreens.register(ModMenus.TICKET_MACHINE, TicketMachineScreen::new);
    	
    	MenuScreens.register(ModMenus.TRADER_INTERFACE, TraderInterfaceScreen::new);
    	
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
    	TradeRuleScreen.RegisterTradeRule(() -> new FreeSample());
    	
    	//Register the key bind
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_WALLET);
    	
	}
	
	@Override
	public void clearClientTraders()
	{
		ClientTradingOffice.clearData();
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
	public void updateNotifications(NotificationData data)
	{
		ClientTradingOffice.updateNotifications(data);
	}
	
	@Override
	public void receiveNotification(Notification notification)
	{
		
		Minecraft mc = Minecraft.getInstance();
		if(MinecraftForge.EVENT_BUS.post(new NotificationEvent.NotificationReceivedOnClient(mc.player.getUUID(), ClientTradingOffice.getNotifications(), notification)))
			return;
		
		if(Config.CLIENT.pushNotificationsToChat.get()) //Post the notification to chat
			mc.gui.getChat().addMessage(notification.getChatMessage());
		
	}
	
	@Override
	public void receiveSelectedBankAccount(AccountReference selectedAccount) { ClientTradingOffice.updateLastSelectedAccount(selectedAccount); }
	
	@Override
	public void openTerminalScreen() { this.openTerminal = true; }
	
	@Override
	public void openNotificationScreen() { this.openNotifications = true; }
	
	@Override
	public void openTeamManager() { this.openTeamManager = true; }
	
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
			else if(this.openNotifications)
			{
				this.openNotifications = false;
				Minecraft.getInstance().setScreen(new NotificationScreen());
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
	
	@Override
	public void playCoinSound() {
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft != null)
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
	}
	
	@Override
	public void processPlayerList(CompoundTag data) { PlayerSuggestionsUtil.loadFromServerData(data); }
	
}
