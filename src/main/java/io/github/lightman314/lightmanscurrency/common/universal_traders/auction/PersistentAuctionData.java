package io.github.lightman314.lightmanscurrency.common.universal_traders.auction;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.world.item.ItemStack;

public class PersistentAuctionData {
	
	public final String id;
	public final long duration;
	private final List<ItemStack> items;
	public final List<ItemStack> getAuctionItems() {
		List<ItemStack> copy = new ArrayList<>();
		for(ItemStack stack : this.items)
			copy.add(stack.copy());
		return copy;
	}
	private final CoinValue startBid;
	public final CoinValue getStartingBid() { return this.startBid.copy(); }
	private final CoinValue minBid;
	public final CoinValue getMinimumBidDifference() { return this.minBid; }
	
	private PersistentAuctionData(String id, long duration, List<ItemStack> items, CoinValue startBid, CoinValue minBid) {
		this.id = id;
		this.duration = duration;
		this.items = items;
		this.startBid = startBid;
		this.minBid = minBid;
	}
	
	public AuctionTradeData createAuction() { return new AuctionTradeData(this); }
	
	public static PersistentAuctionData load(JsonObject json) throws Exception {
	
		String id;
		if(json.has("id"))
			id = json.get("id").getAsString();
		else
			throw new Exception("Auction was not given a valid 'id' entry!");
		
		List<ItemStack> items = new ArrayList<>();
		if(json.has("Item1"))
			items.add(FileUtil.parseItemStack(json.getAsJsonObject("Item1")));
		if(json.has("Item2"))
			items.add(FileUtil.parseItemStack(json.getAsJsonObject("Item2")));
		
		if(items.size() <= 0)
			throw new Exception("Auction has no 'Item1' or 'Item2' entry!");
		
		long duration = 0;
		if(json.has("Duration"))
			duration = Math.max(json.get("Duration").getAsLong(), AuctionTradeData.MINIMUM_DURATION);
		else
			duration = AuctionTradeData.DEFAULT_DURATION;
		
		CoinValue startingBid;
		if(json.has("StartingBid"))
			startingBid = CoinValue.Parse(json.get("StartingBid"));
		else
			throw new Exception("Auction has no 'StartingBid' entry!");
		
		CoinValue minimumBid = new CoinValue(1);
		if(json.has("MinimumBid"))
			minimumBid = CoinValue.Parse(json.get("MinimumBid"));
		
		return new PersistentAuctionData(id, duration, items, startingBid, minimumBid);
	}
	
}
