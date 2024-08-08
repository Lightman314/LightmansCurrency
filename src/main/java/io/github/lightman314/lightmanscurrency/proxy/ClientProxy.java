package io.github.lightman314.lightmanscurrency.proxy;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.client.data.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.*;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.EnchantedBookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers.NormalBookRenderer;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.attachments.EventUnlocks;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinChestBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.*;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.enchantments.MoneyMendingEnchantment;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.playertrading.ClientPlayerTrade;
import io.github.lightman314.lightmanscurrency.api.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import javax.annotation.Nonnull;

public class ClientProxy extends CommonProxy{
	boolean openNotifications = false;
	private long timeOffset = 0;

	private final Supplier<CoinChestBlockEntity> coinChestBE = Suppliers.memoize(() -> new CoinChestBlockEntity(BlockPos.ZERO, ModBlocks.COIN_CHEST.get().defaultBlockState()));

	@Override
	public boolean isClient() { return true; }

	@Override
	public void init(@Nonnull ModContainer modContainer) {
		//modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	@Override
	public void setupClient() {

		ConfigFile.loadClientFiles(ConfigFile.LoadPhase.SETUP);

    	//Register Screens
    	//Done in ClientModEvents#registerScreens
    	
    	//Register Tile Entity Renderers
    	BlockEntityRenderers.register(ModBlockEntities.ITEM_TRADER.get(), ItemTraderBlockEntityRenderer::new);
    	BlockEntityRenderers.register(ModBlockEntities.FREEZER_TRADER.get(), FreezerTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.SLOT_MACHINE_TRADER.get(), SlotMachineBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.BOOK_TRADER.get(), BookTraderBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.AUCTION_STAND.get(), AuctionStandBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.COIN_CHEST.get(), CoinChestRenderer::new);
		//BlockEntityRenderers.register(ModBlockEntities.TAX_BLOCK.get(), TaxBlockRenderer::new);

		//Setup Item Edit blacklists
		ItemEditWidget.BlacklistCreativeTabs(CreativeModeTabs.HOTBAR, CreativeModeTabs.INVENTORY, CreativeModeTabs.SEARCH, CreativeModeTabs.OP_BLOCKS);
		ItemEditWidget.BlacklistItem(s -> s.getItem() instanceof TicketItem);
		//Add written book to Item Edit item list (for purchase/barter possibilities with NBT enforcement turned off)
		ItemEditWidget.AddExtraItemAfter(new ItemStack(Items.WRITTEN_BOOK), Items.WRITABLE_BOOK);

		//Setup Book Renderers
		BookRenderer.register(NormalBookRenderer.GENERATOR);
		BookRenderer.register(EnchantedBookRenderer.GENERATOR);

		//Setup custom item renderers
		LCItemRenderer.registerBlockEntitySource(this::checkForCoinChest);

		//Register Curios Render Layers
		//if(LightmansCurrency.isCuriosLoaded())
		//	LCCuriosClient.registerRenderLayers();

	}

	private BlockEntity checkForCoinChest(Block block)
	{
		if(block == ModBlocks.COIN_CHEST.get())
			return coinChestBE.get();
		return null;
	}

	@Override
	public void clearClientTraders() { ClientTraderData.ClearTraders(); }
	
	@Override
	public void updateTrader(CompoundTag compound) { ClientTraderData.UpdateTrader(compound); }
	
	@Override
	public void removeTrader(long traderID) { ClientTraderData.RemoveTrader(traderID); }
	
	public void clearTeams() { ClientTeamData.ClearTeams(); }
	
	public void updateTeam(CompoundTag compound) { ClientTeamData.UpdateTeam(compound); }
	
	@Override
	public void removeTeam(long teamID) { ClientTeamData.RemoveTeam(teamID); }
	
	@Override
	public void clearBankAccounts() { ClientBankData.ClearBankAccounts(); }
	
	@Override
	public void updateBankAccount(UUID player, CompoundTag compound) { ClientBankData.UpdateBankAccount(player, compound); }
	
	@Override
	public void receiveEmergencyEjectionData(CompoundTag compound)
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
		assert mc.player != null;
		if(NeoForge.EVENT_BUS.post(new NotificationEvent.NotificationReceivedOnClient(mc.player.getUUID(), ClientNotificationData.GetNotifications(), notification)).isCanceled())
			return;
		
		if(LCConfig.CLIENT.pushNotificationsToChat.get()) //Post the notification to chat
			mc.gui.getChat().addMessage(notification.getChatMessage());
		
	}
	
	@Override
	public void receiveSelectedBankAccount(BankReference selectedAccount) { ClientBankData.UpdateLastSelectedAccount(selectedAccount); }

	@Override
	public void updateTaxEntries(CompoundTag compound) { ClientTaxData.UpdateEntry(compound); }

	@Override
	public void removeTaxEntry(long id) { ClientTaxData.RemoveEntry(id); }
	
	@Override
	public void openNotificationScreen() { this.openNotifications = true; }
	
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
		this.timeOffset = (this.timeOffset / 1000) * 1000;
		if(this.timeOffset < 10000) //Ignore offset if less than 10s, as it's likely due to ping
			this.timeOffset = 0;
	}
	
	@Override
	public void loadAdminPlayers(List<UUID> serverAdminList) { LCAdminMode.loadAdminPlayers(serverAdminList); }
	
	@SubscribeEvent
	public void openScreenOnRenderTick(RenderFrameEvent.Pre event)
	{
		if(this.openNotifications)
		{
			this.openNotifications = false;
			//Open easy notification screen
			Minecraft.getInstance().setScreen(new NotificationScreen());
		}
	}
	
	@SubscribeEvent
	//Add coin value tooltips to non CoinItem coins.
	public void onItemTooltip(ItemTooltipEvent event) {
		if(event.getEntity() == null || CoinAPI.API.NoDataAvailable() || event.getContext().registries() == null)
			return;
		ItemStack stack = event.getItemStack();
		if(CoinAPI.API.IsCoin(stack, true))
			ChainData.addCoinTooltips(event.getItemStack(), event.getToolTip(), event.getFlags(), event.getEntity());

		//If item has money mending, display money mending tooltip
		MoneyMendingEnchantment.addEnchantmentTooltips(stack,event.getToolTip(),event.getContext());

		if(LCConfig.SERVER.isLoaded() && LCConfig.SERVER.anarchyMode.get() && stack.getItem() instanceof BlockItem bi)
		{
			Block b = bi.getBlock();
			if(b instanceof IOwnableBlock)
				event.getToolTip().add(LCText.TOOLTIP_ANARCHY_WARNING.get().withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED));
		}


	}
	
	@Override
	public void playCoinSound() {
		if(LCConfig.CLIENT.moneyMendingClink.get())
		{
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.COINS_CLINKING.get(), 1f, 0.4f));
		}
	}

	@Override
	@Nonnull
	public Level safeGetDummyLevel() throws Exception {
		Level level = this.getDummyLevelFromServer();
		if(level == null)
			level = Minecraft.getInstance().level;
		if(level != null)
			return level;
		throw new Exception("Could not get dummy level from client, as there is no active level!");
	}

	@Override
	public void loadPlayerTrade(ClientPlayerTrade trade) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player.containerMenu instanceof PlayerTradeMenu menu)
			menu.reloadTrade(trade);
	}

	@Override
	public void syncEventUnlocks(@Nonnull List<String> unlocksList) {
		Minecraft mc = Minecraft.getInstance();
		EventUnlocks unlocks = mc.player.getData(ModAttachmentTypes.EVENT_UNLOCKS);
		if(unlocks != null)
			unlocks.sync(unlocksList);
	}

	@Override
	public void sendClientMessage(@Nonnull Component message)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
			player.sendSystemMessage(message);
	}

	@Override
	public List<GameProfile> getPlayerList(boolean logicalClient) {
		if(!logicalClient)
			return super.getPlayerList(logicalClient);
		return Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map(PlayerInfo::getProfile).toList();
	}

	@Override
	public RegistryAccess getClientRegistryHolder() {
		Level level = Minecraft.getInstance().level;
		if(level != null)
			return level.registryAccess();
		return null;
	}

}