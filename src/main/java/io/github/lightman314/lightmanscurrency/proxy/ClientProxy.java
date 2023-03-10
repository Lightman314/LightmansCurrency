package io.github.lightman314.lightmanscurrency.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.client.data.ClientEjectionData;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.data.ClientTeamData;
import io.github.lightman314.lightmanscurrency.client.data.ClientTraderData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.core.*;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.common.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.common.items.CoinItem;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;

public class ClientProxy extends CommonProxy{
	
	boolean openTerminal = false;
	boolean openTeamManager = false;
	boolean openNotifications = false;
	
	private long timeOffset = 0;
	
	@Override
	public void setupClient() {
		
		//Set Render Layers
		RenderTypeLookup.setRenderLayer(ModBlocks.DISPLAY_CASE.get(), RenderType.cutout());
		
		this.setRenderLayer(ModBlocks.VENDING_MACHINE.getAll(), RenderType.cutout());
		
		this.setRenderLayer(ModBlocks.VENDING_MACHINE_LARGE.getAll(), RenderType.cutout());

		RenderTypeLookup.setRenderLayer(ModBlocks.ARMOR_DISPLAY.get(), RenderType.cutout());
		this.setRenderLayer(ModBlocks.AUCTION_STAND.getAll(), RenderType.cutout());

		RenderTypeLookup.setRenderLayer(ModBlocks.GEM_TERMINAL.get(), RenderType.translucent());
    	
    	//Register Screens
    	ScreenManager.register(ModMenus.ATM.get(), ATMScreen::new);
		ScreenManager.register(ModMenus.MINT.get(), MintScreen::new);

		ScreenManager.register(ModMenus.TRADER.get(), TraderScreen::new);
		ScreenManager.register(ModMenus.TRADER_BLOCK.get(), TraderScreen::new);
		ScreenManager.register(ModMenus.TRADER_NETWORK_ALL.get(), TraderScreen::new);

		ScreenManager.register(ModMenus.TRADER_STORAGE.get(), TraderStorageScreen::new);

		ScreenManager.register(ModMenus.WALLET.get(), WalletScreen::new);
		ScreenManager.register(ModMenus.WALLET_BANK.get(), WalletBankScreen::new);
		ScreenManager.register(ModMenus.TICKET_MACHINE.get(), TicketMachineScreen::new);

		ScreenManager.register(ModMenus.TRADER_INTERFACE.get(), TraderInterfaceScreen::new);

		ScreenManager.register(ModMenus.TRADER_RECOVERY.get(), TraderRecoveryScreen::new);

		ScreenManager.register(ModMenus.PLAYER_TRADE.get(), PlayerTradeScreen::new);
    	
    	//Register Tile Entity Renderers
    	ClientRegistry.bindTileEntityRenderer(ModBlockEntities.ITEM_TRADER.get(), ItemTraderBlockEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ModBlockEntities.FREEZER_TRADER.get(), FreezerTraderBlockEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ModBlockEntities.AUCTION_STAND.get(), AuctionStandBlockEntityRenderer::new);


    	//Register the key bind
    	ClientRegistry.registerKeyBinding(ClientEvents.KEY_WALLET);
		ClientRegistry.registerKeyBinding(ClientEvents.KEY_PORTABLE_TERMINAL);

		//Setup Item Edit blacklists
		ItemEditWidget.BlacklistCreativeTabs(ItemGroup.TAB_HOTBAR, ItemGroup.TAB_INVENTORY, ItemGroup.TAB_SEARCH);
		ItemEditWidget.BlacklistItem(ModItems.TICKET);
		ItemEditWidget.BlacklistItem(ModItems.TICKET_MASTER);

		//Register the Wallet Overlay
		//Not needed in 1.16, as I have to manually render in the hud render post event
		//Overlay.registerOverlayTop("wallet_hud", WalletDisplayOverlay.INSTANCE);

		//Add wallet layer the old fashioned way :P
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
		this.addWalletLayer(skinMap.get("default"));
		this.addWalletLayer(skinMap.get("slim"));
    	
	}

	private void addWalletLayer(PlayerRenderer renderer)
	{
		List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> layers = ObfuscationReflectionHelper.getPrivateValue(LivingRenderer.class, renderer, "field_177097_h");
		if(layers != null)
		{
			layers.add(new WalletLayer<>(renderer, new ModelWallet<>()));
		}
	}
	
	private void setRenderLayer(List<Block> blocks, RenderType type) {
		for(Block b : blocks) RenderTypeLookup.setRenderLayer(b, type);
	}
	
	@Override
	public void clearClientTraders() { ClientTraderData.ClearTraders(); }
	
	@Override
	public void updateTrader(CompoundNBT compound) { ClientTraderData.UpdateTrader(compound); }
	
	@Override
	public void removeTrader(long traderID) { ClientTraderData.RemoveTrader(traderID); }
	
	public void initializeTeams(CompoundNBT compound)
	{
		if(compound.contains("Teams", Constants.NBT.TAG_LIST))
		{
			List<Team> teams = Lists.newArrayList();
			ListNBT teamList = compound.getList("Teams", Constants.NBT.TAG_COMPOUND);
			teamList.forEach(nbt -> teams.add(Team.load((CompoundNBT)nbt)));
			ClientTeamData.InitTeams(teams);
		}
	}
	
	public void updateTeam(CompoundNBT compound) { ClientTeamData.UpdateTeam(compound); }
	
	@Override
	public void removeTeam(long teamID) { ClientTeamData.RemoveTeam(teamID); }
	
	@Override
	public void initializeBankAccounts(CompoundNBT compound)
	{
		if(compound.contains("BankAccounts", Constants.NBT.TAG_LIST))
		{
			Map<UUID,BankAccount> bank = new HashMap<>();
			ListNBT bankList = compound.getList("BankAccounts", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < bankList.size(); ++i)
			{
				CompoundNBT tag = bankList.getCompound(i);
				UUID id = tag.getUUID("Player");
				BankAccount bankAccount = new BankAccount(tag);
				bank.put(id,bankAccount);
			}
			ClientBankData.InitBankAccounts(bank);
		}
	}
	
	@Override
	public void updateBankAccount(CompoundNBT compound) { ClientBankData.UpdateBankAccount(compound); }
	
	@Override
	public void receiveEmergencyEjectionData(CompoundNBT compound)
	{
		ClientEjectionData.UpdateEjectionData(compound);
	}
	
	@Override
	public void updateNotifications(NotificationData data)
	{
		ClientNotificationData.UpdateNotifications(data);
	}
	
	@Override
	public void receiveNotification(Notification notification)
	{
		
		Minecraft mc = Minecraft.getInstance();
		if(MinecraftForge.EVENT_BUS.post(new NotificationEvent.NotificationReceivedOnClient(mc.player.getUUID(), ClientNotificationData.GetNotifications(), notification)))
			return;
		
		if(Config.CLIENT.pushNotificationsToChat.get()) //Post the notification to chat
			mc.gui.getChat().addMessage(notification.getChatMessage());
		
	}
	
	@Override
	public void receiveSelectedBankAccount(AccountReference selectedAccount) { ClientBankData.UpdateLastSelectedAccount(selectedAccount); }
	
	@Override
	public void openTerminalScreen() { this.openTerminal = true; }
	
	@Override
	public void openNotificationScreen() { this.openNotifications = true; }
	
	@Override
	public void openTeamManager() { this.openTeamManager = true; }
	
	@Override
	public void createTeamResponse(long teamID)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.screen instanceof TeamManagerScreen)
		{
			TeamManagerScreen screen = (TeamManagerScreen)minecraft.screen;
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
		CommandLCAdmin.loadAdminPlayers(serverAdminList);
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
		minecraft.getSoundManager().play(SimpleSound.forUI(ModSounds.COINS_CLINKING, 1f, 0.4f));
	}
	
	@SubscribeEvent
	public void onLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
		
		//Initialize the item edit widgets item list
    	ItemEditWidget.initItemList();
		
	}

	@Override
	@Nonnull
	public World safeGetDummyLevel() throws Exception{
		World level = this.getDummyLevelFromServer();
		if(level == null)
			level = Minecraft.getInstance().level;
		if(level != null)
			return level;
		throw new Exception("Could not get dummy level from client, as there is no active level!");
	}

	@Override
	public void loadPlayerTrade(ClientPlayerTrade trade) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player.containerMenu instanceof PlayerTradeMenu)
		{
			PlayerTradeMenu menu = (PlayerTradeMenu)mc.player.containerMenu;
			menu.reloadTrade(trade);
		}
	}
	
}
