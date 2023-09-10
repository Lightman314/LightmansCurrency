package io.github.lightman314.lightmanscurrency.common.traders.paygate;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.PaygateNotification;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.paygate.CMessageCollectTicketStubs;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public class PaygateTraderData extends TraderData {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "paygate");

	public static final int DURATION_MIN = 1;
	public static final int DURATION_MAX = 1200;

	private int storedTicketStubs = 0;
	public int getStoredTicketStubs() { return this.storedTicketStubs; }
	public void addTicketStub(int count)
	{
		//Don't bother storing the ticket stubs if creative.
		if(this.isCreative())
			return;
		this.storedTicketStubs += count;
		this.markTicketStubsDirty();
	}
	public void collectTicketStubs(Player player)
	{
		if(this.storedTicketStubs > 0)
		{
			do
			{
				ItemStack stub = new ItemStack(ModItems.TICKET_STUB.get());
				int addCount = Math.min(this.storedTicketStubs, stub.getMaxStackSize());
				stub.setCount(addCount);
				this.storedTicketStubs -= addCount;
				ItemHandlerHelper.giveItemToPlayer(player, stub);
			} while (this.storedTicketStubs > 0);
			this.storedTicketStubs = 0;
			this.markTicketStubsDirty();
		}
		else if(this.storedTicketStubs != 0)
		{
			this.storedTicketStubs = 0;
			this.markTicketStubsDirty();
		}
	}


	@Override
	public boolean canShowOnTerminal() { return false; }

	protected List<PaygateTradeData> trades = PaygateTradeData.listOfSize(1);

	public PaygateTraderData() { super(TYPE); }
	public PaygateTraderData(Level level, BlockPos pos) { super(TYPE, level, pos); }

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

		if(this.getTradeCount() >= this.getMaxTradeCount() && !CommandLCAdmin.isAdminPlayer(requestor))
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
		if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;

		//Get the cost of the trade
		CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();

		CoinValue taxesPaid = CoinValue.EMPTY;

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
				//Abort if not enough room to put the ticket stub
				if(!trade.shouldStoreTicketStubs() && !context.canFitItem(new ItemStack(ModItems.TICKET_STUB.get())))
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
					this.addTicketStub(1);
				else //Give the ticket stub
					context.putItem(new ItemStack(ModItems.TICKET_STUB.get()));

			}

			//Activate the paygate
			this.activate(trade.getDuration());

			//Push Notification
			this.pushNotification(PaygateNotification.createTicket(trade, hasPass, context.getPlayerReference(), this.getNotificationCategory()));

		}
		//Process a coin trade
		else
		{
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

			//Push Notification
			this.pushNotification(PaygateNotification.createMoney(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

		}
		//Push the post-trade event
		this.runPostTradeEvent(context.getPlayerReference(), trade, price, taxesPaid);
		return TradeResult.SUCCESS;
	}

	@Override
	public boolean hasValidTrade() {
		for(PaygateTradeData trade : this.trades)
		{
			if(trade.isValid())
				return true;
		}
		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		this.saveTrades(compound);
		this.saveTicketStubs(compound);
	}

	protected final void saveTicketStubs(CompoundTag compound) { compound.putInt("TicketStubs", this.storedTicketStubs); }

	protected final void saveTrades(CompoundTag compound) { PaygateTradeData.saveAllData(compound, this.trades); }

	public void markTicketStubsDirty() { this.markDirty(this::saveTicketStubs); }

	@Override
	protected void saveAdditionalToJson(JsonObject json) { }

	@Override
	protected void loadAdditional(CompoundTag compound) {
		//Load Trades
		if(compound.contains(PaygateTradeData.DEFAULT_KEY))
			this.trades = PaygateTradeData.loadAllData(compound);
		//Load Ticket Stubs
		if(compound.contains("TicketStubs"))
			this.storedTicketStubs = compound.getInt("TicketStubs");
	}

	@Override
	protected void loadAdditionalFromJson(JsonObject json) { }

	@Override
	protected void saveAdditionalPersistentData(CompoundTag compound) { }

	@Override
	protected void loadAdditionalPersistentData(CompoundTag compound) { }

	@Override
	protected void getAdditionalContents(List<ItemStack> results) { }

	@Override
	public void addInteractionSlots(List<InteractionSlotData> interactionSlots) { }

	@Override
	public boolean canMakePersistent() { return false; }

	@Override
	public void initStorageTabs(TraderStorageMenu menu) {
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
		return new IconButton(0,0, b -> LightmansCurrencyPacketHandler.instance.sendToServer(new CMessageCollectTicketStubs(this.getID())), IconData.of(ModItems.TICKET_STUB))
				.withAddons(EasyAddonHelper.toggleTooltip(() -> this.storedTicketStubs > 0, () -> EasyText.translatable("tooltip.lightmanscurrency.trader.collect_ticket_stubs", this.storedTicketStubs), EasyText::empty),
						EasyAddonHelper.visibleCheck(() -> this.areTicketStubsRelevant() && this.hasPermission(playerSource.get(), Permissions.OPEN_STORAGE)),
						EasyAddonHelper.activeCheck(() -> this.getStoredTicketStubs() > 0));
	}

	private boolean areTicketStubsRelevant() {
		return this.storedTicketStubs > 0 || this.trades.stream().anyMatch(t -> t.isTicketTrade() && t.shouldStoreTicketStubs());
	}

}