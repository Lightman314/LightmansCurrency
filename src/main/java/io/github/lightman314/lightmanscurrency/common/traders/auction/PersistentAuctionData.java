package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class PersistentAuctionData {
	
	public final String id;
	public final long duration;
	private final List<ItemStack> items;
	@Nonnull
	public List<ItemStack> getAuctionItems() { return InventoryUtil.copyList(this.items); }
	private final MoneyValue startBid;
	public MoneyValue getStartingBid() { return this.startBid; }
	private final MoneyValue minBid;
	public MoneyValue getMinimumBidDifference() { return this.minBid; }
	private final boolean overtime;
	public boolean overtimeAllowed() { return this.overtime; }
	
	private PersistentAuctionData(String id, long duration, List<ItemStack> items, MoneyValue startBid, MoneyValue minBid, boolean overtime) {
		this.id = id;
		this.duration = duration;
		this.items = items;
		this.startBid = startBid;
		this.minBid = minBid;
		this.overtime = overtime;
	}
	
	public AuctionTradeData createAuction() { return new AuctionTradeData(this); }
	
	public static PersistentAuctionData load(JsonObject json) throws JsonSyntaxException, ResourceLocationException {

		String id;
		if(json.has("id"))
			id = GsonHelper.getAsString(json, "id");
		else
			id = GsonHelper.getAsString(json, "ID");
		
		List<ItemStack> items = new ArrayList<>();
		if(json.has("Item1"))
			items.add(FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json, "Item1")));
		if(json.has("Item2"))
			items.add(FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json, "Item2")));
		
		if(items.isEmpty())
			throw new JsonSyntaxException("Auction has no 'Item1' or 'Item2' entry!");
		
		long duration = Math.max(GsonHelper.getAsLong(json, "Duration", AuctionTradeData.GetDefaultDuration()), AuctionTradeData.GetMinimumDuration());

		MoneyValue startingBid = MoneyValue.loadFromJson(GsonHelper.getNonNull(json, "StartingBid"));
		if(startingBid.isEmpty() || startingBid.isFree())
			throw new JsonSyntaxException("StartingBid cannot be empty and/or free!");

		MoneyValue minimumBid = startingBid.getSmallestValue();
		if(json.has("MinimumBid"))
			minimumBid = MoneyValue.loadFromJson(GsonHelper.getNonNull(json, "MinimumBid"));

		if(!startingBid.getUniqueName().equals(minimumBid.getUniqueName()))
			throw new JsonSyntaxException("StartingBid and MinimumBid are not compatible money values!");

		boolean overtime = GsonHelper.getAsBoolean(json,"Overtime",false);

		return new PersistentAuctionData(id, duration, items, startingBid, minimumBid, overtime);
	}
	
}
