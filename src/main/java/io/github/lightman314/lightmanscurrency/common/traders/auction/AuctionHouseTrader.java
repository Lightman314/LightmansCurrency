package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.*;
import java.util.function.Predicate;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.FakeOwner;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionCreateTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction.AuctionTradeCancelTab;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.AuctionBidEvent;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.CreateAuctionEvent;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.util.IconUtil;
import io.github.lightman314.lightmanscurrency.network.message.auction.SPacketStartBid;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AuctionHouseTrader extends TraderData implements IEasyTickable {

	public static final TraderType<AuctionHouseTrader> TYPE = new TraderType<>(VersionUtil.lcResource( "auction_house"),AuctionHouseTrader::new);
	
	public static final IconData ICON = IconData.of(IconUtil.ICON_TEXTURE, 96, 16);
	
	private final List<AuctionTradeData> trades = new ArrayList<>();
	
	Map<UUID,AuctionPlayerStorage> storage = new HashMap<>();

	public static boolean isEnabled() { return LCConfig.SERVER.auctionHouseEnabled.get(); }
	public static boolean shouldShowOnTerminal() { return isEnabled() && LCConfig.SERVER.auctionHouseOnTerminal.get(); }

	@Override
	public boolean showOnTerminal() { return shouldShowOnTerminal(); }
	@Override
	public boolean isCreative() { return true; }
	
	private AuctionHouseTrader() {
		super(TYPE);
		this.getOwner().SetOwner(FakeOwner.of(LCText.GUI_TRADER_AUCTION_HOUSE_OWNER.get()));
	}

    @Override
	public MutableComponent getName() { return LCText.GUI_TRADER_AUCTION_HOUSE.get(); }

	@Override
	public boolean readyForCustomers() { return true; }

	@Override
	public int getTradeCount() { return (int)this.trades.stream().filter(AuctionTradeData::isValid).count(); }
	
	public AuctionTradeData getTrade(int tradeIndex) {
		try {
			return this.trades.get(tradeIndex);
		} catch(Exception e) { return null; }
	}
	
	public boolean hasPersistentAuction(String id) {
		for(AuctionTradeData trade : this.trades)
		{
			if(trade.isPersistentID(id) && trade.isValid())
				return true;
		}
		return false;
	}
	
	public int getTradeIndex(AuctionTradeData trade) {
		return this.trades.indexOf(trade);
	}
	
	@Override
	public void markTradesDirty() { this.markDirty(this::saveTrades); }

	@Override
	public boolean showSearchBox() { return this.getTradeCount() > 10; }

	public AuctionPlayerStorage getStorage(Player player) { return getStorage(PlayerReference.of(player)); }
	
	public AuctionPlayerStorage getStorage(PlayerReference player) {
		if(player == null)
			return null;
		if(!this.storage.containsKey(player.id))
		{
			//Create new storage entry for the player
			this.storage.put(player.id, new AuctionPlayerStorage(player));
			this.markStorageDirty();
		}
		return this.storage.get(player.id);
	}
	
	public void markStorageDirty() {
		this.markDirty(this::saveStorage);
	}

	@Override
	public void tick() {
		//Check if any trades have expired
		long currentTime = System.currentTimeMillis();
		boolean changed = false;
		//Can only delete trades if no player is currently using the trader, as we don't want to delete trades and mess up a trade index.
		boolean canDelete = this.getUserCount() <= 0;
		for(int i = 0; i < this.trades.size(); ++i)
		{
			AuctionTradeData trade = this.trades.get(i);
			//Check if the auction has timed out and should be executed
			if(trade.hasExpired(currentTime))
			{
				//Execute the trade if the time has run out
				//Includes sending notifications and payment to the relevant players storage
				trade.ExecuteTrade(this);
				changed = true;
			}
			//Check if the trade should be deleted
			if(canDelete && !trade.isValid())
			{
				//Delete the trade if it's no longer valid
				this.trades.remove(i);
				i--;
			}
		}
		if(changed) //Mark both trades and storage dirty
		{
			this.markDirty(this::saveTrades);
			this.markDirty(this::saveStorage);
		}
	}
	
	@Override
	public int getPermissionLevel(PlayerReference player, String permission) {
		if(Objects.equals(permission, Permissions.OPEN_STORAGE) || Objects.equals(permission, Permissions.EDIT_TRADES))
			return 1;
		return 0;
	}
	
	@Override
	public int getPermissionLevel(Player player, String permission) {
		if(Objects.equals(permission, Permissions.OPEN_STORAGE) || Objects.equals(permission, Permissions.EDIT_TRADES))
			return 1;
		return 0;
	}
	
	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		this.saveTrades(compound,lookup);
		this.saveStorage(compound,lookup);
		
	}
	
	protected final void saveTrades(CompoundTag compound,HolderLookup.Provider lookup) {
		ListTag list = new ListTag();
		for (AuctionTradeData trade : this.trades) {
			list.add(trade.getAsNBT(lookup));
		}
		compound.put("Trades", list);
	}
	
	protected final void saveStorage(CompoundTag compound,HolderLookup.Provider lookup) {
		ListTag list = new ListTag();
		this.storage.forEach((player,storage) -> list.add(storage.save(new CompoundTag(),lookup)));
		compound.put("StorageData", list);
	}
	
	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		//Load trades
		if(compound.contains("Trades"))
		{
			this.trades.clear();
			ListTag tradeList = compound.getList("Trades", Tag.TAG_COMPOUND);
			for(int i = 0; i < tradeList.size(); ++i)
				this.trades.add(new AuctionTradeData(tradeList.getCompound(i),lookup));
		}
		
		//Load storage
		if(compound.contains("StorageData"))
		{
			this.storage.clear();
			ListTag storageList = compound.getList("StorageData", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); ++i)
			{
				AuctionPlayerStorage storageEntry = new AuctionPlayerStorage(storageList.getCompound(i),lookup);
				if(storageEntry.getOwner() != null)
					this.storage.put(storageEntry.getOwner().id, storageEntry);
			}
		}

		//Reset fake owner name
		this.getOwner().SetOwner(FakeOwner.of(LCText.GUI_TRADER_AUCTION_HOUSE_OWNER.get()));
		
	}

	@Override
	public void addTrade(Player requestor) { }

	@Override
	public void removeTrade(Player requestor) {}
	
	public void addTrade(AuctionTradeData trade, boolean persistent) {
		
		CreateAuctionEvent.Pre e1 = new CreateAuctionEvent.Pre(this, trade, persistent);
		if(NeoForge.EVENT_BUS.post(e1).isCanceled())
			return;
		trade = e1.getAuction();
		
		trade.startTimer();
		if(trade.isValid())
		{
			this.trades.add(trade);
			this.markTradesDirty();
			
			CreateAuctionEvent.Post e2 = new CreateAuctionEvent.Post(this, trade, persistent);
			NeoForge.EVENT_BUS.post(e2);
		}
		else
			LightmansCurrency.LogError("Auction Trade is not fully valid. Unable to add it to the list.");
	}

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		//Interaction should simply open the bid menu, so...
		if(!context.hasPlayer())
			return TradeResult.FAIL_NOT_SUPPORTED;
		else
		{
			//Open bid menu for the given trade index
			AuctionTradeData trade = this.getTrade(tradeIndex);
			if(trade != null && trade.allowedToBid(context.getPlayer()))
			{
				new SPacketStartBid(this.getID(), tradeIndex).sendTo(context.getPlayer());
				return TradeResult.SUCCESS;
			}
			else
				return TradeResult.FAIL_TRADE_RULE_DENIAL;
		}
	}
	
	public void makeBid(Player player, TraderMenu menu, int tradeIndex, MoneyValue bidAmount) {
		
		AuctionTradeData trade = this.getTrade(tradeIndex);
		if(trade == null)
			return;
		if(trade.hasExpired(TimeUtil.getCurrentTime()))
			return;
		
		AuctionBidEvent.Pre e1 = new AuctionBidEvent.Pre(this, trade, player, bidAmount);
		if(NeoForge.EVENT_BUS.post(e1).isCanceled())
			return;
		
		bidAmount = e1.getBidAmount();

		TradeContext tradeContext = menu.getContext(this);

		if(tradeContext.hasFunds(bidAmount) && trade.tryMakeBid(this, player, bidAmount))
		{
			//Take money from the coin slots & players wallet second
			tradeContext.getPayment(bidAmount);
			//Mark storage & trades dirty
			this.markDirty((c,l) -> { this.saveTrades(c,l); this.saveStorage(c,l);} );
			
			AuctionBidEvent.Post e2 = new AuctionBidEvent.Post(this, trade, player, bidAmount);
			NeoForge.EVENT_BUS.post(e2);
		}

	}

	
    @Override
	public List<? extends TradeData> getTradeData() { return this.trades == null ? new ArrayList<>() : this.trades; }

	@Override
	public IconData getIcon() { return ICON; }

	@Override
	public boolean canMakePersistent() { return false; }
	
	@Override
	public void saveAdditionalPersistentData(CompoundTag data, HolderLookup.Provider lookup) { }

	@Override
	public void loadAdditionalPersistentData(CompoundTag data, HolderLookup.Provider lookup) { }

	
	@Override
	public Predicate<TradeData> getStorageTradeFilter(ITraderStorageMenu menu) { return trade -> trade instanceof AuctionTradeData at && at.isOwner(menu.getPlayer()) && at.isValid(); }

	@Override
	public void initStorageTabs(ITraderStorageMenu menu) {
		//Storage Tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new AuctionStorageTab(menu));
		//Cancel Trade tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new AuctionTradeCancelTab(menu));
		//Create Trade tab
		menu.setTab(TraderStorageTab.TAB_TRADE_MISC, new AuctionCreateTab(menu));
		//Clear unwanted tabs
		menu.clearTab(TraderStorageTab.TAB_TRADER_INFO);
		menu.clearTab(TraderStorageTab.TAB_TRADER_SETTINGS);
		menu.clearTab(TraderStorageTab.TAB_SETTINGS_CLIPBOARD);
		menu.clearTab(TraderStorageTab.TAB_RULES_TRADER);
		menu.clearTab(TraderStorageTab.TAB_RULES_TRADE);
	}
	
	@Override
	public boolean shouldRemove(MinecraftServer server) { return false; }

	@Override
	public void getAdditionalContents(List<ItemStack> contents) { }

	@Override
	protected MutableComponent getDefaultName() { return this.getName(); }

	@Override
	public boolean hasValidTrade() { return true; }

	@Override
	protected void saveAdditionalToJson(JsonObject json, HolderLookup.Provider lookup) { }

	@Override
	protected void loadAdditionalFromJson(JsonObject json, HolderLookup.Provider lookup) {}

	@Override
	protected boolean allowAdditionalUpgradeType(UpgradeType type) { return false; }

	@Override
	public int getTradeStock(int tradeIndex) { return 0; }

	@Override
	public boolean supportsMultiPriceEditing() { return false; }

	@Override
	protected void addPermissionOptions(List<PermissionOption> options) { }
	
	@Override
	protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) { defaultValues.clear(); }

	@Override
	protected void appendTerminalInfo(List<Component> list, @Nullable Player player) {
		int auctionCount = 0;
		for(AuctionTradeData auction : this.trades)
		{
			if(auction.isValid() && auction.isActive())
				auctionCount++;
		}
		list.add(LCText.TOOLTIP_NETWORK_TERMINAL_AUCTION_HOUSE.get(auctionCount));
	}

	@Override
	public int getTerminalTextColor() {
		int auctionCount = 0;
		for(AuctionTradeData auction : this.trades)
		{
			if(auction.isValid() && auction.isActive())
				auctionCount++;
		}
		//Green if there's an auction available, normal color if not.
		return auctionCount > 0 ? 0x00FF00 : 0x404040;
	}

}
