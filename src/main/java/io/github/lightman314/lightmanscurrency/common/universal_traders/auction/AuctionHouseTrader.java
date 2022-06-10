package io.github.lightman314.lightmanscurrency.common.universal_traders.auction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.auction.*;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AuctionHouseTrader extends UniversalTraderData {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_house");
	
	public static final IconData ICON = IconData.of(new ResourceLocation(LightmansCurrency.MODID, "textures/gui/icons.png"), 96, 16);
	
	List<AuctionTradeData> trades = new ArrayList<>();
	
	Map<UUID,AuctionPlayerStorage> storage = new HashMap<>();
	
	public AuctionHouseTrader() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onRemoved() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@Override
	public MutableComponent getName() { return Component.translatable("gui.lightmanscurrency.universaltrader.auction"); }
	
	@Override
	public int getTradeCount() { return this.trades.size(); }
	
	public AuctionTradeData getTrade(int tradeIndex) {
		try {
			return this.trades.get(tradeIndex);
		} catch(Exception e) { return null; }
	}
	
	public int getTradeIndex(AuctionTradeData trade) {
		return this.trades.indexOf(trade);
	}
	
	@Override
	public void markTradesDirty() {
		this.markDirty(this::saveTrades);
	}
	
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
	
	@SubscribeEvent
	public void onWorldTick(ServerTickEvent event) {
		if(event.phase == Phase.END)
		{
			//Check if any trades have expired
			long currentTime = System.currentTimeMillis();
			boolean changed = false;
			//Can only delete trades if no player is currently using the trader, as we don't want to delete trades and mess up a trade index.
			boolean canDelete = !this.hasUser();
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
				this.markDirty(this.saveTrades(this.saveStorage(new CompoundTag())));
		}
	}
	
	@Override
	public int getPermissionLevel(PlayerReference player, String permission) {
		if(permission == Permissions.OPEN_STORAGE)
			return 1;
		return 0;
	}
	
	@Override
	public int getPermissionLevel(Player player, String permission) {
		if(permission == Permissions.OPEN_STORAGE)
			return 1;
		return 0;
	}
	
	@Override
	public CompoundTag write(CompoundTag compound) {
		super.write(compound);
		
		this.saveTrades(compound);
		this.saveStorage(compound);
		
		return compound;
	}
	
	protected final CompoundTag saveTrades(CompoundTag compound) {
		ListTag list = new ListTag();
		for(int i = 0; i < this.trades.size(); ++i)
		{
			list.add(this.trades.get(i).getAsNBT());
		}
		compound.put("Trades", list);
		return compound;
	}
	
	protected final CompoundTag saveStorage(CompoundTag compound) {
		ListTag list = new ListTag();
		this.storage.forEach((player,storage) -> list.add(storage.save(new CompoundTag())));
		compound.put("StorageData", list);
		return compound;
	}
	
	@Override
	public void read(CompoundTag compound) {
		
		//Load trades
		if(compound.contains("Trades"))
		{
			this.trades.clear();
			ListTag tradeList = compound.getList("Trades", Tag.TAG_COMPOUND);
			for(int i = 0; i < tradeList.size(); ++i)
				this.trades.add(new AuctionTradeData(tradeList.getCompound(i)));
		}
		
		//Load storage
		if(compound.contains("StorageData"))
		{
			this.storage.clear();
			ListTag storageList = compound.getList("StorageData", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); ++i)
			{
				AuctionPlayerStorage storageEntry = new AuctionPlayerStorage(storageList.getCompound(i));
				if(storageEntry.getOwner() != null)
					this.storage.put(storageEntry.getOwner().id, storageEntry);
			}
		}
		
		super.read(compound);
		
	}

	@Override
	public int getTradeStock(int index) { return 0; }

	@Override
	public List<Settings> getAdditionalSettings() { return new ArrayList<>(); }

	@Override
	public void requestAddOrRemoveTrade(boolean isAdd) {}

	@Override
	public void addTrade(Player requestor) { }

	@Override
	public void removeTrade(Player requestor) {}
	
	public void addTrade(AuctionTradeData trade) {
		trade.startTimer();
		if(trade.isValid())
		{
			this.trades.add(trade);
			this.markTradesDirty();
		}
		else
			LightmansCurrency.LogError("Auction Trade is not fully valid. Unable to add it to the list.");
	}

	@Override
	public ITradeRuleScreenHandler getRuleScreenHandler(int tradeIndex) {
		return null;
	}

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		//Interaction should simply open the bid menu, so...
		if(!context.hasPlayer())
			return TradeResult.FAIL_NOT_SUPPORTED;
		else
		{
			//Open bid menu for the given trade index
			return TradeResult.SUCCESS;
		}
	}
	
	public boolean makeBid(Player player, int tradeIndex, CoinValue bidAmount) {
		
		AuctionTradeData trade = this.getTrade(tradeIndex);
		if(trade == null)
			return false;
		if(trade.hasExpired(System.currentTimeMillis()))
			return false;
		
		AuctionPlayerStorage bidderStorage = this.getStorage(player);
		
		ItemStack wallet = LightmansCurrency.getWalletStack(player);
		long inventoryValue = bidderStorage.getStoredCoins().getRawValue();
		if(!wallet.isEmpty())
			inventoryValue += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
    	if(inventoryValue < bidAmount.getRawValue())
    		return false;
		if(trade.tryMakeBid(this, player, bidAmount))
		{
			//Take the money from the players storage first
			CoinValue pendingPayment = bidderStorage.takeMoney(bidAmount);
			//Take money from the players wallet second
			if(pendingPayment.getRawValue() > 0)
				MoneyUtil.ProcessPayment(null, player, bidAmount);
			//Mark storage & trades dirty
			this.markDirty(this.saveTrades(this.saveStorage(new CompoundTag())));
			return true;
		}
		return false;
		
	}

	@Override
	public List<? extends ITradeData> getTradeInfo() { return this.trades; }

	@Override
	protected ItemLike getCategoryItem() { return ModItems.TRADING_CORE.get(); }

	@Override
	protected void onVersionUpdate(int oldVersion) { }

	@Override
	public ResourceLocation getTraderType() { return TYPE; }

	@Override
	public IconData getIcon() { return ICON; }

	@Override
	public CompoundTag getPersistentData() { return new CompoundTag(); }

	@Override
	public void loadPersistentData(CompoundTag data) { }

	@Override
	public Function<ITradeData,Boolean> getStorageDisplayFilter(TraderStorageMenu menu) {
		return trade -> {
			if(trade instanceof AuctionTradeData)
			{
				AuctionTradeData at = (AuctionTradeData)trade;
				//Only display if the trade owner is owned by the player.
				return at.getTradeOwner().is(menu.player) && at.isActive();
			}
			return false;
		};
	}
	
	@Override
	public void initStorageTabs(TraderStorageMenu menu) {
		//Storage Tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new AuctionStorageTab(menu));
		//Cancel Trade tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new AuctionTradeCancelTab(menu));
		//Create Trade tab
		menu.setTab(10, new AuctionCreateTab(menu));
	}
	
	@Override
	public boolean shouldRemove(MinecraftServer server) { return false; }
	
	@Override
	public boolean hasNoValidTrades() { return false; }
	
}
