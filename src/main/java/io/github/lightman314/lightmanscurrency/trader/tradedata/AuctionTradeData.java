package io.github.lightman314.lightmanscurrency.trader.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.AlertData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseBidNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseBuyerNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseCancelNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseSellerNobidNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseSellerNotification;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class AuctionTradeData extends TradeData {

	private static final long MINIMUM_DURATION = TimeUtil.DURATION_HOUR;
	private static final long DEFAULT_DURATION = TimeUtil.DURATION_DAY;
	
	public boolean hasBid() { return this.lastBidPlayer != null; }
	
	private boolean cancelled;
	
	CoinValue lastBidAmount = new CoinValue();
	public CoinValue getLastBidAmount() { return this.lastBidAmount; }
	PlayerReference lastBidPlayer = null;
	public PlayerReference getLastBidPlayer() { return this.lastBidPlayer; }
	
	public void setStartingBid(CoinValue amount) {
		if(this.isActive())
			return;
		this.lastBidAmount = amount.copy();
	}
	
	CoinValue minBidDifference = new CoinValue(1);
	public CoinValue getMinBidDifference() { return this.minBidDifference; }
	public void setMinBidDifferent(CoinValue amount) {
		if(this.isActive())
			return;
		this.minBidDifference = amount.copy();
		if(this.minBidDifference.getRawValue() <= 0)
			this.minBidDifference = new CoinValue(1);
	}
	PlayerReference tradeOwner;
	public PlayerReference getTradeOwner() { return this.tradeOwner; }
	public boolean isOwner(Player player) {
		return tradeOwner == null || this.tradeOwner.is(player) || TradingOffice.isAdminPlayer(player);
	}
	
	long startTime = 0;
	long duration = 0;
	public void setDuration(long duration) {
		if(this.isActive())
			return;
		this.duration = Math.max(MINIMUM_DURATION, duration);
	}
	
	List<ItemStack> auctionItems = new ArrayList<>();
	public List<ItemStack> getAuctionItems() { return this.auctionItems; }
	public void setAuctionItems(Container auctionItems) {
		if(this.isActive())
			return;
		this.auctionItems.clear();
		for(int i = 0; i < auctionItems.getContainerSize(); ++i)
		{
			ItemStack stack = auctionItems.getItem(i);
			if(!stack.isEmpty())
				this.auctionItems.add(stack.copy());
		}
	}
	
	public AuctionTradeData(Player owner) { this.tradeOwner = PlayerReference.of(owner); this.setDuration(DEFAULT_DURATION); }
	
	public AuctionTradeData(CompoundTag compound) { this.loadFromNBT(compound); }

	public boolean isActive() { return this.startTime != 0 && !this.cancelled; }
	
	@Override
	public boolean isValid() {
		if(this.cancelled)
			return false;
		if(this.auctionItems.size() <= 0)
			return false;
		if(this.isActive() && this.hasExpired(TimeUtil.getCurrentTime()))
			return false;
		if(this.tradeOwner == null)
			return false;
		if(this.minBidDifference.getRawValue() <= 0)
			return false;
		if(this.lastBidAmount.getRawValue() <= 0)
			return false;
		return true;
	}
	
	public void startTimer() {
		if(!this.isActive())
			this.startTime = TimeUtil.getCurrentTime();
	}
	
	public long getRemainingTime(long currentTime) {
		if(!this.isActive())
			return this.duration;
		return Math.max(0, this.startTime + this.duration - currentTime);
	}
	
	public boolean hasExpired(long time) {
		if(this.isActive())
			return time >= this.startTime + this.duration;
		return false;
	}
	
	public boolean tryMakeBid(AuctionHouseTrader trader, Player player, CoinValue amount) {
		if(!validateBidAmount(amount))
			return false;
		
		PlayerReference oldBidder = this.lastBidPlayer;
		
		if(this.lastBidPlayer != null)
		{
			//Refund the money to the previous bidder
			AuctionPlayerStorage storage = trader.getStorage(this.lastBidPlayer);
			storage.giveMoney(this.lastBidAmount);
			trader.markStorageDirty();
		}
		
		this.lastBidPlayer = PlayerReference.of(player);
		this.lastBidAmount = amount.copy();
		
		//Send notification to the previous bidder letting them know they've been out-bid.
		if(oldBidder != null)
			TradingOffice.pushNotification(oldBidder.id, new AuctionHouseBidNotification(this));
		
		return true;
	}
	
	public boolean validateBidAmount(CoinValue amount) {
		CoinValue minAmount = this.getMinNextBid();
		return amount.getRawValue() >= minAmount.getRawValue();
	}
	
	public CoinValue getMinNextBid() {
		return new CoinValue(this.lastBidPlayer == null ? this.lastBidAmount.getRawValue() : this.lastBidAmount.getRawValue() + this.minBidDifference.getRawValue());
	}
	
	public void ExecuteTrade(AuctionHouseTrader trader) {
		if(this.cancelled)
			return;
		if(this.lastBidPlayer != null)
		{
			AuctionPlayerStorage buyerStorage = trader.getStorage(this.lastBidPlayer);
			//Reward the items to the last bidder
			for(int i = 0; i < this.auctionItems.size(); ++i)
				buyerStorage.giveItem(this.auctionItems.get(i));
			//Give the bid money to the trades owner
			AuctionPlayerStorage sellerStorage = trader.getStorage(this.tradeOwner);
			sellerStorage.giveMoney(this.lastBidAmount);
			
			//Post notification to the auction winner
			TradingOffice.pushNotification(this.lastBidPlayer.id, new AuctionHouseBuyerNotification(this));
			
			//Post notification to the auction owner
			TradingOffice.pushNotification(this.tradeOwner.id, new AuctionHouseSellerNotification(this));
		}
		else
		{
			//Nobody bid on the item(s), return the items to the auction owner
			AuctionPlayerStorage sellerStorage = trader.getStorage(this.tradeOwner);
			for(int i = 0; i < this.auctionItems.size(); ++i)
				sellerStorage.giveItem(this.auctionItems.get(i));
			
			//Post notification to the auction owner
			TradingOffice.pushNotification(this.tradeOwner.id, new AuctionHouseSellerNobidNotification(this));
			
		}
	}
	
	public void CancelTrade(AuctionHouseTrader trader, boolean giveToPlayer, Player player)
	{
		if(this.cancelled)
			return;
		this.cancelled = true;
		if(this.lastBidPlayer != null)
		{
			//Give a refund to the last bidder
			AuctionPlayerStorage buyerStorage = trader.getStorage(this.lastBidPlayer);
			buyerStorage.giveMoney(this.lastBidAmount);
			
			//Send cancel notification
			TradingOffice.pushNotification(this.lastBidPlayer.id, new AuctionHouseCancelNotification(this));
			
		}
		//Return the items being sold to their owner
		if(giveToPlayer)
		{
			//Return items to the player who cancelled the trade
			for(ItemStack stack : this.auctionItems) ItemHandlerHelper.giveItemToPlayer(player, stack);
		}
		else
		{
			//Return items to the trader owners storage. Ignore the player
			AuctionPlayerStorage sellerStorage = trader.getStorage(this.tradeOwner != null ? this.tradeOwner : PlayerReference.of(player));
			for(ItemStack stack : this.auctionItems) sellerStorage.giveItem(stack);
		}
			
	}
	
	@Override
	public CompoundTag getAsNBT() {
		//Do not run super.getAsNBT() as we don't need to save the price or trade rules.
		CompoundTag compound = new CompoundTag();
		ListTag itemList = new ListTag();
		for(int i = 0; i < this.auctionItems.size(); ++i)
		{
			itemList.add(this.auctionItems.get(i).save(new CompoundTag()));
		}
		compound.put("SellItems", itemList);
		this.lastBidAmount.writeToNBT(compound, "LastBid");
		if(this.lastBidPlayer != null)
			compound.put("LastBidPlayer", this.lastBidPlayer.save());
		
		this.minBidDifference.writeToNBT(compound, "MinBid");
		
		compound.putLong("StartTime", this.startTime);
		compound.putLong("Duration", this.duration);
		
		compound.put("TradeOwner", this.tradeOwner.save());
		
		compound.putBoolean("Cancelled", this.cancelled);
		
		return compound;
	}
	
	@Override
	public void loadFromNBT(CompoundTag compound) {
		//Do not run super.loadFromNBT() as we didn't save the default data in the first place
		ListTag itemList = compound.getList("SellItems", Tag.TAG_COMPOUND);
		this.auctionItems.clear();
		for(int i = 0; i < itemList.size(); ++i)
		{
			ItemStack stack = ItemStack.of(itemList.getCompound(i));
			if(!stack.isEmpty())
				this.auctionItems.add(stack);
		}
		this.lastBidAmount.readFromNBT(compound, "LastBid");
		if(compound.contains("LastBidPlayer"))
			this.lastBidPlayer = PlayerReference.load(compound.getCompound("LastBidPlayer"));
		else
			this.lastBidPlayer = null;
		
		this.minBidDifference.readFromNBT(compound, "MinBid");
		
		this.startTime = compound.getLong("StartTime");
		this.duration = compound.getLong("Duration");
		
		this.tradeOwner = PlayerReference.load(compound.getCompound("TradeOwner"));
		
		this.cancelled = compound.getBoolean("Cancelled");
		
	}
	
	@Override
	public int tradeButtonWidth(TradeContext context) { return 94; }

	@Override
	public int tradeButtonHeight(TradeContext context) { return 28; }

	@Override
	public DisplayData inputDisplayArea(TradeContext context) {
		return new DisplayData(1, 1, 34, 16);
	}

	@Override
	public DisplayData outputDisplayArea(TradeContext context) {
		return new DisplayData(58, 1, 34, 16);
	}
	
	@Override
	public Pair<Integer,Integer> arrowPosition(TradeContext context) {
		return Pair.of(36, 1);
	}

	@Override
	public Pair<Integer,Integer> alertPosition(TradeContext context) {
		return Pair.of(36, 1);
	}

	@Override
	public List<DisplayEntry> getInputDisplays(TradeContext context) {
		return Lists.newArrayList(DisplayEntry.of(this.lastBidAmount, this.getBidInfo(), true));
	}

	private List<Component> getBidInfo() {
		List<Component> bidInfo = new ArrayList<>();
		if(this.lastBidPlayer == null)
		{
			//First bid info
			bidInfo.add(Component.translatable("tooltip.lightmanscurrency.auction.nobidder"));
			bidInfo.add(Component.translatable("tooltip.lightmanscurrency.auction.minbid", this.lastBidAmount.getString()));
		}
		else
		{
			//Last bid info
			bidInfo.add(Component.translatable("tooltip.lightmanscurrency.auction.lastbidder", this.lastBidPlayer.lastKnownName()));
			bidInfo.add(Component.translatable("tooltip.lightmanscurrency.auction.currentbid", this.lastBidAmount.getString()));
			//Next bid info
			bidInfo.add(Component.translatable("tooltip.lightmanscurrency.auction.minbid", this.getMinNextBid().getString()));
		}
		return bidInfo;
	}
	
	@Override
	public List<DisplayEntry> getOutputDisplays(TradeContext context) {
		List<DisplayEntry> entries = new ArrayList<>();
		for(int i = 0; i < this.auctionItems.size(); ++i)
		{
			ItemStack item = this.auctionItems.get(i);
			if(!item.isEmpty())
				entries.add(DisplayEntry.of(item, item.getCount(), ItemRenderUtil.getTooltipFromItem(item)));
		}
		return entries;
	}

	@Override
	public List<AlertData> getAlertData(TradeContext context) {
		return new ArrayList<>();
	}

	@Override
	public void onInputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) { this.openCancelAuctionTab(tab, clientHandler); }

	@Override
	public void onOutputDisplayInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int index, int button, ItemStack heldItem) { this.openCancelAuctionTab(tab, clientHandler); }

	@Override
	public void onInteraction(BasicTradeEditTab tab, IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) { this.openCancelAuctionTab(tab, clientHandler); }
	
	private void openCancelAuctionTab(BasicTradeEditTab tab, IClientMessage clientHandler) {
		
		ITrader t = tab.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			int tradeIndex = trader.getTradeIndex(this);
			if(tradeIndex < 0)
				return;
			
			CompoundTag extraData = new CompoundTag();
			extraData.putInt("TradeIndex", tradeIndex);
			tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
			
		}
		
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderAdditional(AbstractWidget button, PoseStack pose, int mouseX, int mouseY, TradeContext context) {
		//Draw remaining time
		TimeData time = TimeUtil.separateDuration(this.getRemainingTime(TimeUtil.getCurrentTime()));
		TextRenderUtil.drawCenteredText(pose, time.toString(), button.x + button.getWidth() / 2, button.y + button.getHeight() - 10, this.getTextColor(time));
	}
	
	@Override
	public List<Component> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
		//If the mouse is over the time remaining text give time remaining info
		if(mouseY >= this.tradeButtonHeight(context) - 10)
		{
			List<Component> durationData = new ArrayList<>();
			
			return durationData;
		}
		return null;
	}
	
	private int getTextColor(TimeData remainingTime) {
		
		if(remainingTime.hours < 1)
		{
			if(remainingTime.minutes < 5) //Red if less than 5 minutes
				return 0xFF0000;
			//Yellow if less than 1 hour
			return 0xFFFF00;
		}
		//Green if more than 1 hour
		return 0x00FF00;
	}

	@Override
	public TradeDirection getTradeDirection() {
		return TradeDirection.NONE;
	}

	@Override
	public TradeComparisonResult compare(TradeData otherTrade) { return new TradeComparisonResult(); }

	@Override
	public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

	@Override
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return new ArrayList<>(); }
	
}
