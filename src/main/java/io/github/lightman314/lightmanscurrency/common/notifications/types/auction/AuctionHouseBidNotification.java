package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.ItemTradeNotification.ItemData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class AuctionHouseBidNotification extends AuctionHouseNotification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_house_outbid");
	
	List<ItemData> items;
	CoinValue cost = new CoinValue();
	
	String customer;
	
	public AuctionHouseBidNotification(AuctionTradeData trade) {
		
		this.cost = trade.getLastBidAmount().copy();
		this.customer = trade.getLastBidPlayer().getName(false);
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}
	
	public AuctionHouseBidNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public IFormattableTextComponent getMessage() {
		
		ITextComponent itemText = getItemNames(this.items);

		ITextComponent cost = this.cost.getComponent("0");
		
		//Create log from stored data
		return EasyText.translatable("notifications.message.auction.outbid", this.customer, itemText, cost);
		
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		
		ListNBT itemList = new ListNBT();
		for(ItemData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		this.cost.save(compound, "Price");
		compound.putString("Customer", this.customer);
		
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
		ListNBT itemList = compound.getList("Items", Constants.NBT.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemData(itemList.getCompound(i)));
		this.cost.load(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}
	
}