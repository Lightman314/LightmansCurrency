package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class PersistentAuctionData {
	
	public final String id;
	public final long duration;
	private final List<ItemStack> items;
	public List<ItemStack> getAuctionItems() { return ItemHandlerUtil.copyList(this.items); }
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

	public static PersistentAuctionData load(JsonObject json,DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {

		String id;
		if(json.has("ID"))
			id = GsonHelper.getAsString(json, "ID");
		else
			id = GsonHelper.getAsString(json, "id");
		
		List<ItemStack> items = new ArrayList<>();
        items.add(context.readOrThrow(GsonHelper.getNonNull(json,"Item1"),ItemStack.STRICT_CODEC));
		if(json.has("Item2"))
			items.add(context.readOrThrow(json.get("Item2"),ItemStack.STRICT_CODEC));
		
		if(items.isEmpty())
			throw new JsonSyntaxException("Auction has no 'Item1' or 'Item2' entry!");
		
		long duration = Math.max(GsonHelper.getAsLong(json, "Duration",AuctionTradeData.GetDefaultDuration()), AuctionTradeData.GetMinimumDuration());

		MoneyValue startingBid = context.readOrThrow(GsonHelper.getNonNull(json,"StartingBid"),MoneyValue.LENIENT_NON_EMPTY_OR_FREE_CODEC);

		MoneyValue minimumBid = startingBid.getSmallestValue();
		if(json.has("MinimumBid"))
			minimumBid = context.readOrThrow(json.get("MinimumBid"),MoneyValue.LENIENT_NON_EMPTY_OR_FREE_CODEC);

		if(!startingBid.getUniqueName().equals(minimumBid.getUniqueName()))
			throw new JsonSyntaxException("StartingBid and MinimumBid are not compatible money values!");

		boolean overtime = GsonHelper.getAsBoolean(json,"Overtime",true);
		
		return new PersistentAuctionData(id, duration, items, startingBid, minimumBid,overtime);
	}
	
}
