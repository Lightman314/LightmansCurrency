package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.ItemTradeNotification.ItemData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class AuctionHouseCancelNotification extends AuctionHouseNotification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "auction_house_canceled");
	
	List<ItemData> items;
	
	public AuctionHouseCancelNotification(AuctionTradeData trade) {
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}
	
	public AuctionHouseCancelNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public IFormattableTextComponent getMessage() {
		
		ITextComponent itemText = getItemNames(this.items);
		
		//Create log from stored data
		return EasyText.translatable("notifications.message.auction.canceled", itemText);
		
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		
		ListNBT itemList = new ListNBT();
		for(ItemData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {

		ListNBT itemList = compound.getList("Items", Constants.NBT.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemData(itemList.getCompound(i)));
		
	}
	
}