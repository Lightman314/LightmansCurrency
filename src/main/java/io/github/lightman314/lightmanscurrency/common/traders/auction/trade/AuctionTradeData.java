package io.github.lightman314.lightmanscurrency.common.traders.auction.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.taxes.ITaxCollector;
import io.github.lightman314.lightmanscurrency.api.taxes.TaxAPI;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.client.TradeInteractionData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModStats;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tabs.AuctionTradeCancelTab;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseBidNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseBuyerNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseCancelNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseSellerNobidNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.AuctionHouseSellerNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.common.traders.auction.PersistentAuctionData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.traders.trade.comparison.TradeComparisonResult;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.AuctionCompletedEvent;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.CancelAuctionEvent;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionTradesNode;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class AuctionTradeData extends TradeData {

    public static final int ITEM_SLOTS = 2;

    public static final Codec<AuctionTradeData> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.CODEC.listOf(1,ITEM_SLOTS).fieldOf("items").forGetter(AuctionTradeData::getAuctionItems),
            MoneyValue.CODEC.fieldOf("lastBid").forGetter(AuctionTradeData::getLastBidAmount),
            PlayerReference.CODEC.optionalFieldOf("lastBidPlayer").forGetter(t -> t.lastBidPlayer),
            MoneyValue.CODEC.fieldOf("minBid").forGetter(AuctionTradeData::getMinBidDifference),
            Codec.LONG.fieldOf("start").forGetter(t -> t.startTime),
            Codec.LONG.fieldOf("duration").forGetter(t -> t.duration),
            PlayerReference.CODEC.optionalFieldOf("owner").forGetter(t -> t.tradeOwner),
            Codec.BOOL.fieldOf("overtime").forGetter(t -> t.overtimeAllowed),
            Codec.BOOL.fieldOf("cancelled").forGetter(t -> t.cancelled),
            Codec.STRING.optionalFieldOf("persistent").forGetter(t -> t.persistentID)
    ).apply(builder,AuctionTradeData::new)),
            CodecHelper.oldValueLoader(AuctionTradeData::loadOldData,"Auction Trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf,AuctionTradeData> STREAM_CODEC = StreamHelper.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(ITEM_SLOTS)),AuctionTradeData::getAuctionItems,
            MoneyValue.STREAM_CODEC,AuctionTradeData::getLastBidAmount,
            ByteBufCodecs.optional(PlayerReference.STREAM_CODEC),t -> t.lastBidPlayer,
            MoneyValue.STREAM_CODEC,AuctionTradeData::getMinBidDifference,
            ByteBufCodecs.VAR_LONG,t -> t.startTime,
            ByteBufCodecs.VAR_LONG,t -> t.duration,
            ByteBufCodecs.optional(PlayerReference.STREAM_CODEC),t -> t.tradeOwner,
            ByteBufCodecs.BOOL,t -> t.overtimeAllowed,
            ByteBufCodecs.BOOL,t -> t.cancelled,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),t -> t.persistentID,
            AuctionTradeData::new);

	public static long GetMinimumDuration() {
		if(LCConfig.SERVER.auctionHouseDurationMin.get() > 0)
			return TimeUtil.DURATION_DAY * LCConfig.SERVER.auctionHouseDurationMin.get();
		return TimeUtil.DURATION_HOUR;
	}
	public static long GetDefaultDuration() {
		if(LCConfig.SERVER.auctionHouseDurationMin.get() > 0)
			return TimeUtil.DURATION_DAY * LCConfig.SERVER.auctionHouseDurationMin.get();
		return TimeUtil.DURATION_DAY;
	}
	public static final long OVERTIME_DURATION = 5 * TimeUtil.DURATION_MINUTE;
	
	public boolean hasBid() { return this.lastBidPlayer.isPresent(); }

	private boolean overtimeAllowed = true;
	public boolean isOvertimeAllowed() { return this.overtimeAllowed; }
	public void setOvertimeAllowed(boolean value)
	{
		if(this.isActive())
			return;
		this.overtimeAllowed = value;
	}

	private boolean cancelled;
	
	private Optional<String> persistentID = Optional.empty();
	public boolean isPersistentID(String id) { return this.persistentID.isPresent() && this.persistentID.get().equals(id); }

	MoneyValue lastBidAmount = MoneyValue.empty();
	public MoneyValue getLastBidAmount() { return this.lastBidAmount; }
	Optional<PlayerReference> lastBidPlayer = Optional.empty();
	@Nullable
	public PlayerReference getLastBidPlayer() { return this.lastBidPlayer.orElse(null); }
	
	public void setStartingBid(MoneyValue amount) {
		if(this.isActive())
			return;
		this.lastBidAmount = amount;
		//Validate the min bid difference
		if(this.minBidDifference.isEmpty())
			this.minBidDifference = this.lastBidAmount.getSmallestValue();
		else if(!this.minBidDifference.sameType(this.lastBidAmount))
			this.minBidDifference = this.lastBidAmount.getSmallestValue();
	}

	MoneyValue minBidDifference = MoneyValue.empty();
	public MoneyValue getMinBidDifference() {
		if(this.minBidDifference == null || !this.lastBidAmount.sameType(this.minBidDifference))
			return this.minBidDifference = this.lastBidAmount.getSmallestValue();
		return this.minBidDifference;
	}
	public void setMinBidDifference(MoneyValue amount) {
		if(this.isActive())
			return;
		if(!this.lastBidAmount.sameType(amount))
			return;
		this.minBidDifference = amount;
		if(this.minBidDifference.isEmpty())
			this.minBidDifference = this.lastBidAmount.getSmallestValue();
	}
	Optional<PlayerReference> tradeOwner = Optional.empty();
	public PlayerReference getOwner() { return this.tradeOwner.orElse(null); }
	public boolean isOwner(Player player) {
		return (this.tradeOwner.isPresent() && this.tradeOwner.get().is(player)) || LCAdminMode.isAdminPlayer(player);
	}

	long startTime = 0;
	long duration = 0;
	public void setDuration(long duration) {
		if(this.isActive())
			return;
		this.duration = Math.max(GetMinimumDuration(), duration);
	}

	@Override
	public int getStock(TradeContext context) { return this.isValid() ? 1 : 0; }

	List<ItemStack> auctionItems = new ArrayList<>();
	public List<ItemStack> getAuctionItems() { return this.auctionItems; }
	public void setAuctionItems(IItemHandler auctionItems) {
		if(this.isActive())
			return;
		this.auctionItems.clear();
        this.auctionItems.addAll(ItemHandlerUtil.toNonEmptyList(auctionItems));
	}
	
	public AuctionTradeData(Player owner) {
        this.tradeOwner = Optional.of(PlayerReference.of(owner));
        this.setDuration(GetDefaultDuration());
    }
	
	/**
	 * Used to create an auction trade from persistent auction data
	 */
	public AuctionTradeData(PersistentAuctionData data) {
		this.persistentID = Optional.of(data.id);
		this.setDuration(data.duration);
		this.auctionItems = data.getAuctionItems();
		this.setStartingBid(data.getStartingBid());
		this.setMinBidDifference(data.getMinimumBidDifference());
		this.overtimeAllowed = data.overtimeAllowed();
	}

    private AuctionTradeData(List<ItemStack> items,MoneyValue lastBid,Optional<PlayerReference> lastBidPlayer,MoneyValue minBid,long startTime,long duration,Optional<PlayerReference> owner,boolean overtimee,boolean cancelled,Optional<String> persistent)
    {
        this.auctionItems = new ArrayList<>(items);
        this.lastBidAmount = lastBid;
        this.lastBidPlayer = lastBidPlayer;
        this.minBidDifference = minBid;
        this.startTime = startTime;
        this.duration = duration;
        this.tradeOwner = owner;
        this.overtimeAllowed = overtimee;
        this.cancelled = cancelled;
        this.persistentID = persistent;
    }

	public boolean isActive() { return this.startTime != 0 && !this.cancelled; }
	
	@Override
	public boolean isValid() {
		if(this.cancelled)
			return false;
		if(this.auctionItems.isEmpty())
			return false;
		if(this.isActive() && this.hasExpired(TimeUtil.getCurrentTime()))
			return false;
		if(this.getMinBidDifference().isEmpty())
			return false;
		return !this.lastBidAmount.isEmpty();
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

	public boolean allowedToBid(Player player)
	{
		return this.allowed(this.tradeOwner.orElse(null),player,LCConfig.SERVER.auctionHouseAllowOwnerBidding) && this.allowed(this.lastBidPlayer.orElse(null),player,LCConfig.SERVER.auctionHouseAllowDoubleBidding);
	}

	private boolean allowed(@Nullable PlayerReference test, Player player, BooleanOption config)
	{
		return test == null || !test.is(player) || config.get();
	}
	
	public boolean tryMakeBid(TraderData trader, Player player, MoneyValue amount) {
		if(this.cancelled)
			return false;
		if(!validateBidAmount(amount))
			return false;

		if(!this.allowedToBid(player))
			return false;

        AuctionTradesNode tradesNode = trader.getNode(AuctionTradesNode.TYPE);
        AuctionStorageNode storageNode = trader.getNode(AuctionStorageNode.TYPE);
        if(tradesNode == null || storageNode == null)
            return false;

		PlayerReference oldBidder = this.lastBidPlayer.orElse(null);
		if(oldBidder != null)
		{
			//Refund the money to the previous bidder
			AuctionPlayerStorage storage = storageNode.getStorage(oldBidder);
			storage.giveMoney(this.lastBidAmount);
		}
		
		this.lastBidPlayer = Optional.of(PlayerReference.of(player));
		this.lastBidAmount = amount;
		
		//Send notification to the previous bidder letting them know they've been out-bid.
		if(oldBidder != null)
			NotificationAPI.getApi().PushPlayerNotification(oldBidder.id, new AuctionHouseBidNotification(this));

		long currentTime = TimeUtil.getCurrentTime();
		if(this.overtimeAllowed && this.getRemainingTime(currentTime) < OVERTIME_DURATION)
		{
			//Reset start time to now, and set the auctions duration to 5 minutes
			this.startTime = currentTime;
			this.duration = OVERTIME_DURATION;
		}

        //Reward the player with a bid stat
        player.awardStat(ModStats.STAT_AUCTION_BIDS);
        //Flat the trade as changed
        this.setChanged();
		
		return true;
	}
	
	public boolean validateBidAmount(MoneyValue amount) {
		MoneyValue minAmount = this.getMinNextBid();
		return amount.containsValue(minAmount);
	}
	
	public MoneyValue getMinNextBid() {
		if(this.lastBidPlayer.isEmpty())
			return this.lastBidAmount;
		else
			return this.lastBidAmount.addValue(this.minBidDifference);
	}
	
	public void ExecuteTrade(AuctionStorageNode storageNode,TraderData trader) {
		if(this.cancelled)
			return;
		this.cancelled = true;
		
		//Throw auction completed event
		AuctionCompletedEvent event = new AuctionCompletedEvent(trader, this);
		NeoForge.EVENT_BUS.post(event);

        PlayerReference lastBidder = this.lastBidPlayer.orElse(null);
        PlayerReference owner = this.tradeOwner.orElse(null);
		if(lastBidder != null)
		{
			AuctionPlayerStorage buyerStorage = storageNode.getStorage(lastBidder);
			List<ItemStack> rewards = event.getItems();
			//Reward the items to the last bidder
			for (ItemStack reward : rewards) buyerStorage.giveItem(reward);
			//Give the bid money to the trades owner
			if(owner != null)
			{
				AuctionPlayerStorage sellerStorage = storageNode.getStorage(owner);
				sellerStorage.giveMoney(event.getPaymentAmount());
			}

			//Pay the fee
			MoneyValue fee = event.getFeePayment();
			if(!fee.isEmpty() && LCConfig.SERVER.auctionHouseStoreFeeInServerTax.get())
			{
				ITaxCollector serverTax = TaxAPI.getApi().GetServerTaxCollector(trader);
				serverTax.PayTaxesDirectly(trader,fee);
			}
			
			//Post notification to the auction winner
			NotificationAPI.getApi().PushPlayerNotification(lastBidder.id, new AuctionHouseBuyerNotification(this));

			//Post notification to the auction owner
			if(owner != null)
				NotificationAPI.getApi().PushPlayerNotification(owner.id, new AuctionHouseSellerNotification(this,event.getPaymentAmount(),event.getFeePayment()));

            //Award victory stat
            storageNode.awardAuctionWinStat(lastBidder);

		}
		else
		{
			//Nobody bid on the item(s), return the items to the auction owner
			if(owner != null)
			{
				AuctionPlayerStorage sellerStorage = storageNode.getStorage(owner);
				List<ItemStack> items = event.getItems();
				for (ItemStack item : items) sellerStorage.giveItem(item);
				
				//Post notification to the auction owner
				NotificationAPI.getApi().PushPlayerNotification(owner.id, new AuctionHouseSellerNobidNotification(this));
			}
		}
        //Flag the trade as changed
        this.setChanged();
	}
	
	public void CancelTrade(TraderData trader, boolean giveToPlayer, Player player)
	{
		if(this.cancelled)
			return;
		this.cancelled = true;

        AuctionStorageNode storageNode = trader.getNode(AuctionStorageNode.TYPE);
        if(storageNode == null)
            return;

        PlayerReference lastBidder = this.lastBidPlayer.orElse(null);
		if(lastBidder != null)
		{
			//Give a refund to the last bidder
			AuctionPlayerStorage buyerStorage = storageNode.getStorage(lastBidder);
			buyerStorage.giveMoney(this.lastBidAmount);
			
			//Send cancel notification
			NotificationAPI.getApi().PushPlayerNotification(lastBidder.id, new AuctionHouseCancelNotification(this));
			
		}
		//Return the items being sold to their owner
		if(giveToPlayer)
		{
			//Return items to the player who cancelled the trade
			for(ItemStack stack : this.auctionItems)
				ItemHandlerHelper.giveItemToPlayer(player, stack);
		}
		else
		{
			//Return items to the trader owners storage. Ignore the player
			if(this.tradeOwner.isPresent())
			{
				AuctionPlayerStorage sellerStorage = storageNode.getStorage(this.tradeOwner.get());
				for(ItemStack stack : this.auctionItems) sellerStorage.giveItem(stack);
			}
		}
		
		CancelAuctionEvent event = new CancelAuctionEvent(trader, this, player);
		NeoForge.EVENT_BUS.post(event);
			
	}

	public JsonObject saveToJson(JsonObject json, DataContext<JsonElement> context) {
		
		for(int i = 0; i < this.auctionItems.size(); ++i)
			json.add("Item" + (i + 1), context.write(this.auctionItems.get(i),ItemStack.CODEC));
		json.addProperty("Duration", this.duration);
		json.add("StartingBid", this.lastBidAmount.toJson());
		json.add("MinimumBid", this.minBidDifference.toJson());
		json.addProperty("Overtime", this.overtimeAllowed);
		return json;
	}

    @Deprecated
	public static AuctionTradeData loadOldData(CompoundTag compound, HolderLookup.Provider lookup) {
		//Do not run super.loadFromNBT() as we didn't saveItem the default data in the first place
		ListTag itemList = compound.getList("SellItems", Tag.TAG_COMPOUND);
        List<ItemStack> auctionItems = new ArrayList<>();
        DataContext<Tag> context = DataContext.createNBT(lookup);
		for(Tag tag : itemList)
		{
			ItemStack stack = context.read(tag,CodecHelper.UNLIMITED_ITEM_OPTIONAL);
			if(!stack.isEmpty())
				auctionItems.add(stack);
		}
		MoneyValue lastBidAmount = MoneyValue.safeLoad(compound, "LastBid");
        Optional<PlayerReference> lastBidPlayer = Optional.empty();
		if(compound.contains("LastBidPlayer"))
			lastBidPlayer = Optional.of(PlayerReference.load(compound.getCompound("LastBidPlayer")));

		MoneyValue minBidDifference = MoneyValue.safeLoad(compound, "MinBid");
		
		long startTime = compound.getLong("StartTime");
		long duration = compound.getLong("Duration");

        Optional<PlayerReference> tradeOwner = Optional.empty();
		if(compound.contains("TradeOwner", Tag.TAG_COMPOUND))
			tradeOwner = Optional.of(PlayerReference.load(compound.getCompound("TradeOwner")));
		
		boolean cancelled = compound.getBoolean("Cancelled");

        boolean overtimeAllowed = compound.getBoolean("Overtime");

        Optional<String> persistentID = Optional.empty();
		if(compound.contains("PersistentID", Tag.TAG_STRING))
			persistentID = Optional.of(compound.getString("PersistentID"));
        return new AuctionTradeData(auctionItems,lastBidAmount,lastBidPlayer,minBidDifference,startTime,duration,tradeOwner,overtimeAllowed,cancelled,persistentID);
	}

	@Override
	public void OnInputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { this.openCancelAuctionTab(tab); }

	@Override
	public void OnOutputDisplayInteraction(BasicTradeEditTab tab, int index, TradeInteractionData data, ItemStack heldItem) { this.openCancelAuctionTab(tab); }

	@Override
	public void OnInteraction(BasicTradeEditTab tab, TradeInteractionData data, ItemStack heldItem) { this.openCancelAuctionTab(tab); }
	
	private void openCancelAuctionTab(BasicTradeEditTab tab) {
        AuctionTradesNode node = tab.menu.getTraderNode(AuctionTradesNode.TYPE);
		if(node != null)
		{
			int tradeIndex = node.getTradeIndex(this);
			if(tradeIndex < 0)
				return;
			tab.sendOpenTabMessage(AuctionTradeCancelTab.KEY, tab.builder().setInt("TradeIndex", tradeIndex));
		}
	}

	@Override
	public TradeDirection getTradeDirection() { return TradeDirection.OTHER; }

	@Override
	public TradeComparisonResult compare(TradeData otherTrade) { return new TradeComparisonResult(); }

	@Override
	public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

	@Override
	public List<Component> GetDifferenceWarnings(TradeComparisonResult differences) { return new ArrayList<>(); }
	
}
