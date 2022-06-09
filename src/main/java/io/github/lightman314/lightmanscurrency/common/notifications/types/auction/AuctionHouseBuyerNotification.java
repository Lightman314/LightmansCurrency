package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ItemTradeNotification.ItemData;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class AuctionHouseBuyerNotification extends AuctionHouseNotification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_house_buyer");
	
	List<ItemData> items;
	CoinValue cost = new CoinValue();
	
	public AuctionHouseBuyerNotification(AuctionTradeData trade) {
		
		this.cost = trade.getLastBidAmount().copy();
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}
	
	public AuctionHouseBuyerNotification(CompoundTag compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public Component getMessage() {
		
		Component itemText = getItemNames(this.items);
		
		Component cost = new TextComponent(this.cost.getString("0"));
		
		//Create log from stored data
		return new TranslatableComponent("notifications.message.auction.buyer", itemText, cost);
		
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		
		ListTag itemList = new ListTag();
		for(ItemData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		this.cost.writeToNBT(compound, "Price");
		
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemData(itemList.getCompound(i)));
		this.cost.readFromNBT(compound, "Price");
		
	}
	
}
