package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.api.PaygateLogger;
import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.PaygateNotification;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageUpdateTradeRule;
import io.github.lightman314.lightmanscurrency.trader.ITradeSource;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PaygateBlockEntity extends TraderBlockEntity implements ITradeRuleHandler, ITradeRuleMessageHandler, ILoggerSupport<PaygateLogger>, ITradeSource<PaygateTradeData>{
	
	public static final int VERSION = 0;
	
	public static final int DURATION_MIN = 1;
	public static final int DURATION_MAX = 1200;
	
	private int timer;
	
	PaygateLogger logger = new PaygateLogger();
	
	protected int tradeCount = 1;
	
	protected List<PaygateTradeData> trades;
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public PaygateBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.PAYGATE.get(), pos, state);
	}
	
	public PaygateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.trades = PaygateTradeData.listOfSize(1);
	}
	
	public int getTradeCount()
	{
		return MathUtil.clamp(tradeCount, 1, ITrader.GLOBAL_TRADE_LIMIT);
	}
	
	@Override
	public boolean canEditTradeCount() { return true; }
	
	@Override
	public int getMaxTradeCount() { return 4; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.worldPosition, isAdd));
	}
	
	public void addTrade(Player requestor)
	{
		if(this.level.isClientSide)
			return;
		if(tradeCount >= ITrader.GLOBAL_TRADE_LIMIT)
			return;
		
		if(this.tradeCount >= this.getMaxTradeCount() && !TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "add creative trade slot", Permissions.ADMIN_MODE);
			return;
		}
		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Settings.PermissionWarning(requestor, "add trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.tradeCount + 1);
	}
	
	public void removeTrade(Player requestor)
	{
		if(this.level.isClientSide)
			return;
		if(this.tradeCount <= 1)
			return;
		
		if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.EDIT_TRADES);
			return;
		}
		this.overrideTradeCount(this.tradeCount - 1);
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, ITrader.GLOBAL_TRADE_LIMIT);
		List<PaygateTradeData> oldTrades = this.trades;
		this.trades = PaygateTradeData.listOfSize(this.tradeCount);
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
	
	public List<PaygateTradeData> getAllTrades() { return this.trades; }
	
	public List<? extends ITradeData> getTradeInfo() { return this.trades; }
	
	public void markTradesDirty()
	{
		this.setChanged();
		//Send an update to the client
		if(this.isServer())
		{
			//Send update packet
			BlockEntityUtil.sendUpdatePacket(this, this.writeTrades(new CompoundTag()));
		}
	}
	
	@Override
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(); }
	
	public PaygateLogger getLogger() { return this.logger; }
	
	public void clearLogger()
	{
		this.logger.clear();
		this.markLoggerDirty();
	}
	
	public void markLoggerDirty()
	{
		this.setChanged();
		//Send an update to the client
		if(this.isServer())
		{
			//Send update packet
			BlockEntityUtil.sendUpdatePacket(this, this.writeLogger(new CompoundTag()));
		}
	}
	
	public int getTradeStock(int tradeIndex) { return 1; }
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		
		super.saveAdditional(compound);
		
		this.writeTimer(compound);
		this.writeTrades(compound);
		this.writeLogger(compound);
		this.writeTradeRules(compound);
		
	}
	
	public final CompoundTag writeTimer(CompoundTag compound) {
		compound.putInt("Timer", Math.max(this.timer, 0));
		return compound;
	}
	
	public final CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeLimit", this.tradeCount);
		PaygateTradeData.saveAllData(compound, this.trades);
		return compound;
	}
	
	public final CompoundTag writeLogger(CompoundTag compound) {
		this.logger.write(compound);
		return compound;
	}
	
	public final CompoundTag writeTradeRules(CompoundTag compound) {
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound) {
		
		//Load the trade limit
		if(compound.contains("TradeLimit", Tag.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, ITrader.GLOBAL_TRADE_LIMIT);
		//Load trades
		if(compound.contains(TradeData.DEFAULT_KEY))
			this.trades = PaygateTradeData.loadAllData(compound, this.getTradeCount());
		//Generate trades from old data
		else if(compound.contains("Duration", Tag.TAG_INT) && this.trades.size() == 1 && !this.trades.get(0).isValid())
		{
			int duration = compound.getInt("Duration");
			List<PaygateTradeData> generatedTrades = new ArrayList<>();
			if(compound.contains("TicketID"))
			{
				UUID ticketID = compound.getUUID("TicketID");
				if(ticketID != null)
				{
					PaygateTradeData trade = new PaygateTradeData();
					trade.setDuration(duration);
					trade.setTicketID(ticketID);
					generatedTrades.add(trade);
				}
			}
			if(compound.contains("Price"))
			{
				CoinValue price = new CoinValue();
				price.readFromNBT(compound, "Price");
				if(price.getRawValue() > 0 || price.isFree())
				{
					PaygateTradeData trade = new PaygateTradeData();
					trade.setDuration(duration);
					trade.setCost(price);
					generatedTrades.add(trade);
				}
			}
			if(generatedTrades.size() > 0)
			{
				this.trades = generatedTrades;
				this.tradeCount = this.trades.size();
			}
		}
		
		//Load the shop logger
		this.logger.read(compound);
		
		//Load the timer
		if(compound.contains("Timer", Tag.TAG_INT))
			this.timer = Math.max(compound.getInt("Timer"), 0);
		
		//Load the trade rules
		if(compound.contains(TradeRule.DEFAULT_TAG, Tag.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		super.load(compound);
		
	}
	
	@Override
	public int GetCurrentVersion() { return VERSION; }
	
	@Override
	protected void onVersionUpdate(int oldVersion) {
		
	}
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.beforeTrade(event));
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		this.tradeRules.forEach(rule -> rule.tradeCost(event));
	}
	
	@Override
	public void afterTrade(PostTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.afterTrade(event));
	}
	
	public void addRule(TradeRule newRule)
	{
		if(newRule == null)
			return;
		//Confirm a lack of duplicate rules
		for(int i = 0; i < this.tradeRules.size(); i++)
		{
			if(newRule.type == this.tradeRules.get(i).type)
			{
				LightmansCurrency.LogInfo("Blocked rule addition due to rule of same type already present.");
				return;
			}
		}
		this.tradeRules.add(newRule);
	}
	
	public List<TradeRule> getRules() { return this.tradeRules; }
	
	public void setRules(List<TradeRule> rules) { this.tradeRules = rules; }
	
	public void removeRule(TradeRule rule)
	{
		if(this.tradeRules.contains(rule))
			this.tradeRules.remove(rule);
	}
	
	public void clearRules()
	{
		this.tradeRules.clear();
	}
	
	public void markRulesDirty()
	{
		this.setChanged();
		//Send an update to the client
		if(this.isServer())
		{
			//Send update packet
			BlockEntityUtil.sendUpdatePacket(this, this.writeTradeRules(new CompoundTag()));
		}
	}
	
	public void closeRuleScreen(Player player)
	{
		this.openStorageMenu(player);
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler(int tradeIndex) { return new TraderScreenHandler(this, tradeIndex); }
	
	private static class TraderScreenHandler implements ITradeRuleScreenHandler
	{
		
		private final PaygateBlockEntity tileEntity;
		private final int tradeIndex;
		
		public TraderScreenHandler(PaygateBlockEntity tileEntity, int tradeIndex)
		{
			this.tileEntity = tileEntity;
			this.tradeIndex = tradeIndex;
		}
		
		@Override
		public ITradeRuleHandler ruleHandler() { 
			if(this.tradeIndex < 0)
				return this.tileEntity;
			return this.tileEntity.getTrade(this.tradeIndex);
		}
		
		@Override
		public void reopenLastScreen()
		{
			this.tileEntity.sendOpenStorageMessage();
		}
		
		@Override
		public void updateServer(ResourceLocation type, CompoundTag updateInfo)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule(this.tileEntity.worldPosition, this.tradeIndex, type, updateInfo));
		}
		
		@Override
		public boolean stillValid() { return !this.tileEntity.isRemoved(); }
		
	}
	
	@Override
	public void receiveTradeRuleMessage(Player player, int index, ResourceLocation ruleType, CompoundTag updateInfo) {
		if(!this.hasPermission(player, Permissions.EDIT_TRADE_RULES))
		{
			Settings.PermissionWarning(player, "edit trade rule", Permissions.EDIT_TRADE_RULES);
			return;
		}
		if(index >= 0)
		{
			this.getTrade(index).updateRule(ruleType, updateInfo);
			this.markTradesDirty();
		}
		else
		{
			this.updateRule(ruleType, updateInfo);
			this.markRulesDirty();
		}
	}
	
	public boolean isActive() { return this.timer > 0; }
	
	public void activate(int duration) {
		this.timer = duration;
		this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, true));
		this.markTimerDirty();
	}
	
	@Override
	public void serverTick()
	{
		if(this.timer > 0)
		{
			this.timer--;
			this.markTimerDirty();
			if(timer <= 0)
			{
				this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(PaygateBlock.POWERED, false));
			}
		}
	}
	
	public void markTimerDirty() {
		this.setChanged();
		if(this.isServer())
			BlockEntityUtil.sendUpdatePacket(this, this.writeTimer(new CompoundTag()));
	}
	
	public int getValidTicketTrade(Player player, ItemStack heldItem) {
		if(heldItem.getItem() == ModItems.TICKET.get())
		{
			UUID ticketID = TicketItem.GetTicketID(heldItem);
			if(ticketID != null)
			{
				for(int i = 0; i < this.trades.size(); ++i)
				{
					PaygateTradeData trade = this.trades.get(i);
					if(trade.isTicketTrade() && trade.getTicketID().equals(ticketID))
					{
						//Confirm that the player is allowed to access the trade
						if(!this.runPreTradeEvent(PlayerReference.of(player), trade).isCanceled())
							return i;
					}
				}
			}
		}
		return -1;
	}
	
	@Override
	public void initStorageTabs(TraderStorageMenu menu) {
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new PaygateTradeEditTab(menu));
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
		
		//Process a ticket trade
		if(trade.isTicketTrade())
		{
			//Abort if we don't have a valid ticket to extract
			if(!trade.canAfford(context))
			{
				LightmansCurrency.LogDebug("Ticket ID " + trade.getTicketID() + " could not be found in the players inventory to pay for trade " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Abort if not enough room to put the ticket stub
			if(!context.canFitItem(new ItemStack(ModItems.TICKET_STUB.get())))
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
			
			//Give the ticket stub
			context.putItem(new ItemStack(ModItems.TICKET_STUB.get()));
			
			//Activate the paygate
			this.activate(trade.getDuration());
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();
			
			//Push Notification
			this.getCoreSettings().pushNotification(() -> new PaygateNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//We would normally change the internal inventory here, but for ticket trades that's not needed.
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
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
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();
			
			//Push Notification
			this.getCoreSettings().pushNotification(() -> new PaygateNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));
			
			//Don't store money if the trader is creative
			if(!this.isCreative())
			{
				//Give the paid cost to storage
				this.addStoredMoney(price);
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
		}
	}
	
}
