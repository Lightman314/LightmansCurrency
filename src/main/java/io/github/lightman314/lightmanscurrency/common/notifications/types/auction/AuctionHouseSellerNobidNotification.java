package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemWriteData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class AuctionHouseSellerNobidNotification extends AuctionHouseNotification{

	public static final NotificationType<AuctionHouseSellerNobidNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "auction_house_seller_nobid"),AuctionHouseSellerNobidNotification::new);
	
	List<ItemWriteData> items;

	private AuctionHouseSellerNobidNotification() {}

	public AuctionHouseSellerNobidNotification(AuctionTradeData trade) {
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemWriteData(trade.getAuctionItems().get(i)));
		
	}
	
	@Nonnull
    @Override
	protected NotificationType<AuctionHouseSellerNobidNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		
		Component itemText = getItemNames(this.items);
		
		//Create log from stored data
		return Component.translatable("notifications.message.auction.seller.nobid", itemText);
		
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		
		ListTag itemList = new ListTag();
		for(ItemWriteData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemWriteData(itemList.getCompound(i)));
		
	}
	
}
