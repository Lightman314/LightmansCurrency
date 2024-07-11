package io.github.lightman314.lightmanscurrency.common.traders.paygate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.PaygateNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.network.message.paygate.CPacketCollectTicketStubs;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public class PaygateTraderData extends TraderData {

	public static final TraderType<PaygateTraderData> TYPE = new TraderType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "paygate"),PaygateTraderData::new);
	
	public static final int DURATION_MIN = 1;
	public static int getMaxDuration() {
		int val = LCConfig.SERVER.paygateMaxDuration.get();
		if(val <= 0)
			return Integer.MAX_VALUE;
		return val;
	}

	private final List<ItemStack> storedTicketStubs = new ArrayList<>();
	public int getStoredTicketStubs() {
		int count = 0;
		for(ItemStack stack : this.storedTicketStubs)
			count += stack.getCount();
		return count;
	}
	public void addTicketStub(ItemStack stub)
	{
		//Don't bother storing the ticket stubs if creative.
		if(this.isCreative())
			return;
		for(ItemStack s : this.storedTicketStubs)
		{
			if(stub.getItem() == s.getItem())
			{
				s.grow(stub.getCount());
				stub.setCount(0);
				break;
			}
		}
		if(!stub.isEmpty())
			this.storedTicketStubs.add(stub.copyAndClear());
		this.markTicketStubsDirty();
	}
	public void collectTicketStubs(Player player)
	{
		for(ItemStack stub : this.storedTicketStubs)
			ItemHandlerHelper.giveItemToPlayer(player, stub);
		this.storedTicketStubs.clear();
		this.markTicketStubsDirty();
	}

	@Override
	public boolean canShowOnTerminal() { return false; }
	
	protected List<PaygateTradeData> trades = PaygateTradeData.listOfSize(1);
	
	private PaygateTraderData() { super(TYPE); }
	public PaygateTraderData(@Nonnull Level level, @Nonnull BlockPos pos) { super(TYPE, level, pos); }

	public int getTradeCount() { return this.trades.size(); }
	
	@Override
	public IconData getIcon() { return IconData.of(Items.REDSTONE_BLOCK); }

	@Override
	protected boolean allowAdditionalUpgradeType(UpgradeType type) { return false; }

	@Override
	public boolean canEditTradeCount() { return true; }
	
	@Override
	public int getMaxTradeCount() { return 8; }
	
	@Override
	public void addTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
			return;
		
		if(this.getTradeCount() >= this.getMaxTradeCount() && !LCAdminMode.isAdminPlayer(requestor))
		{
			Permissions.PermissionWarning(requestor, "add creative trade slot", Permissions.ADMIN_MODE);
			return;
		}
		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Permissions.PermissionWarning(requestor, "add trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.getTradeCount() + 1);
	}
	
	@Override
	public void removeTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(this.getTradeCount() <= 1)
			return;
		
		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Permissions.PermissionWarning(requestor, "remove trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.getTradeCount() - 1);
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.getTradeCount() == newTradeCount)
			return;
		
		int tradeCount = MathUtil.clamp(newTradeCount, 1, TraderData.GLOBAL_TRADE_LIMIT);
		List<PaygateTradeData> oldTrades = this.trades;
		this.trades = PaygateTradeData.listOfSize(tradeCount);
		//Write the old trade data into the array
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); ++i)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		
		//Mark trades dirty
		this.markTradesDirty();
		
	}
	
	public PaygateTradeData getTrade(int tradeSlot) {
		if(tradeSlot < 0 || tradeSlot >= this.trades.size())
		{
			LightmansCurrency.LogError("Cannot get trade in index " + tradeSlot + " from a trader with only " + this.trades.size() + " trades.");
			return new PaygateTradeData();
		}
		return this.trades.get(tradeSlot);
	}
	
	@Nonnull
	@Override
	public List<PaygateTradeData> getTradeData() { return this.trades; }
	
	public int getTradeStock(int tradeIndex) { return 1; }
	
	private PaygateBlockEntity getBlockEntity() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel l = server.getLevel(this.getLevel());
			if(l != null && l.isLoaded(this.getPos()))
			{
				BlockEntity be = l.getBlockEntity(this.getPos());
				if(be instanceof PaygateBlockEntity)
					return (PaygateBlockEntity)be;
			}
		}
		return null;
	}
	
	public boolean isActive() {
		PaygateBlockEntity be = this.getBlockEntity();
		if(be != null)
			return be.isActive();
		return false;
	}
	
	private void activate(int duration) {
		PaygateBlockEntity be = this.getBlockEntity();
		if(be != null)
			be.activate(duration);
	}

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		
		PaygateTradeData trade = this.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			LightmansCurrency.LogError("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			LightmansCurrency.LogWarning("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the paygate is already activated
		if(this.isActive())
		{
			LightmansCurrency.LogWarning("Paygate is already activated. It cannot be activated until the previous timer is completed.");
			return TradeResult.FAIL_OUT_OF_STOCK;
		}
		
		//Abort if no player context is given
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(trade, context).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;

		MoneyValue price = MoneyValue.empty();
		MoneyValue taxesPaid = MoneyValue.empty();

		//Process a ticket trade
		if(trade.isTicketTrade())
		{
			//Abort if we don't have a valid ticket to extract
			if(!trade.canAfford(context))
			{
				LightmansCurrency.LogDebug("Ticket ID " + trade.getTicketID() + " could not be found in the players inventory to pay for trade " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}

			boolean hasPass = context.hasPass(trade.getTicketID());

			if(!hasPass)
			{

				ItemStack ticketStub = trade.getTicketStub();

				//Abort if not enough room to put the ticket stub
				if(!trade.shouldStoreTicketStubs() && !context.canFitItem(ticketStub))
				{
					LightmansCurrency.LogInfo("Not enough room for the ticket stub. Aborting trade!");
					return TradeResult.FAIL_NO_OUTPUT_SPACE;
				}

				//Trade is valid, collect the ticket
				if(!context.collectTicket(trade.getTicketID()))
				{
					LightmansCurrency.LogError("Unable to collect the ticket. Aborting Trade!");
					return TradeResult.FAIL_CANNOT_AFFORD;
				}

				//Store the ticket stub if flagged to do so
				if(trade.shouldStoreTicketStubs())
					this.addTicketStub(ticketStub);
				else //Give the ticket stub
					context.putItem(ticketStub);

			}
			
			//Activate the paygate
			this.activate(trade.getDuration());
			
			//Push Notification
			this.pushNotification(PaygateNotification.createTicket(trade, hasPass, context.getPlayerReference(), this.getNotificationCategory()));

		}
		//Process a coin trade
		else
		{
			//Get the cost of the trade
			price = trade.getCost(context);

			//Abort if we don't have enough money
			if(!context.getPayment(price))
			{
				LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//We have collected the payment, activate the paygate
			this.activate(trade.getDuration());

			//Don't store money if the trader is creative
			if(!this.isCreative())
			{
				//Give the paid cost to storage
				taxesPaid = this.addStoredMoney(price, true);
			}

			//Handle Stats
			this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

			//Push Notification
			this.pushNotification(PaygateNotification.createMoney(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

		}
		//Push the post-trade event
		this.runPostTradeEvent(trade, context, price, taxesPaid);
		return TradeResult.SUCCESS;
	}

	@Override
	protected void saveAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		this.saveTrades(compound,lookup);
		this.saveTicketStubs(compound,lookup);
	}

	protected final void saveTicketStubs(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		ListTag list = new ListTag();
		for(ItemStack stub : this.storedTicketStubs)
			list.add(InventoryUtil.saveItemNoLimits(stub,lookup));
		compound.put("Stubs", list);
	}

	protected final void saveTrades(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { PaygateTradeData.saveAllData(compound, this.trades,lookup); }

	public void markTicketStubsDirty() { this.markDirty(this::saveTicketStubs); }

	@Override
	protected void saveAdditionalToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) { }

	@Override
	protected void loadAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		//Load Trades
		if(compound.contains(PaygateTradeData.DEFAULT_KEY))
			this.trades = PaygateTradeData.loadAllData(compound,lookup);
		//Load Ticket Stubs
		if(compound.contains("TicketStubs"))
		{
			int count = compound.getInt("TicketStubs");
			this.storedTicketStubs.clear();
			if(count > 0)
				this.storedTicketStubs.add(new ItemStack(ModItems.TICKET_STUB.get(), count));
		}
		else if(compound.contains("Stubs"))
		{
			ListTag list = compound.getList("Stubs", Tag.TAG_COMPOUND);
			this.storedTicketStubs.clear();
			for(int i = 0; i < list.size(); ++i)
			{
				ItemStack stack = InventoryUtil.loadItemNoLimits(list.getCompound(i),lookup);
				if(!stack.isEmpty())
					this.storedTicketStubs.add(stack);
			}
		}
	}

	@Override
	protected void loadAdditionalFromJson(JsonObject json, @Nonnull HolderLookup.Provider lookup) { }

	@Override
	protected void saveAdditionalPersistentData(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { }

	@Override
	protected void loadAdditionalPersistentData(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { }

	@Override
	protected void getAdditionalContents(List<ItemStack> results) { }

	@Override
	public boolean canMakePersistent() { return false; }

	@Override
	public void initStorageTabs(@Nonnull ITraderStorageMenu menu) {
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new PaygateTradeEditTab(menu));
	}

	@Override
	protected void addPermissionOptions(List<PermissionOption> options) { }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onScreenInit(TraderScreen screen, Consumer<Object> addWidget) {
		super.onScreenInit(screen, addWidget);
		//Add Collect Ticket Stub button
		IconButton button = this.createTicketStubCollectionButton(() -> screen.getMenu().player);
		addWidget.accept(button);
		screen.leftEdgePositioner.addWidget(button);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onStorageScreenInit(TraderStorageScreen screen, Consumer<Object> addWidget) {
		super.onStorageScreenInit(screen, addWidget);
		//Add Collect Ticket Stub button
		IconButton button = this.createTicketStubCollectionButton(() -> screen.getMenu().player);
		addWidget.accept(button);
		screen.leftEdgePositioner.addWidget(button);
	}


	@OnlyIn(Dist.CLIENT)
	private IconButton createTicketStubCollectionButton(Supplier<Player> playerSource)
	{
		return new IconButton(0,0, b -> new CPacketCollectTicketStubs(this.getID()).send(), IconData.of(ModItems.TICKET_STUB))
				.withAddons(EasyAddonHelper.toggleTooltip(() -> this.getStoredTicketStubs() > 0, () -> LCText.TOOLTIP_TRADER_PAYGATE_COLLECT_TICKET_STUBS.get(this.getStoredTicketStubs()), EasyText::empty),
				EasyAddonHelper.visibleCheck(() -> this.areTicketStubsRelevant() && this.hasPermission(playerSource.get(), Permissions.OPEN_STORAGE)),
				EasyAddonHelper.activeCheck(() -> this.getStoredTicketStubs() > 0));
	}

	private boolean areTicketStubsRelevant() {
		return this.getStoredTicketStubs() > 0 || this.trades.stream().anyMatch(t -> t.isTicketTrade() && t.shouldStoreTicketStubs());
	}

}
