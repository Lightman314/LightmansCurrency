package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
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

public class AuctionHouseBuyerNotification extends AuctionHouseNotification{

	public static final NotificationType<AuctionHouseBuyerNotification> TYPE = new NotificationType<>(new ResourceLocation(LightmansCurrency.MODID, "auction_house_buyer"),AuctionHouseBuyerNotification::new);
	
	List<ItemWriteData> items;
	MoneyValue cost = MoneyValue.empty();

	private AuctionHouseBuyerNotification() {}

	public AuctionHouseBuyerNotification(AuctionTradeData trade) {
		
		this.cost = trade.getLastBidAmount();
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemWriteData(trade.getAuctionItems().get(i)));
		
	}
	
	@Nonnull
    @Override
	protected NotificationType<AuctionHouseBuyerNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		
		Component itemText = getItemNames(this.items);
		
		Component cost = this.cost.getText("0");
		
		//Create log from stored data
		return Component.translatable("notifications.message.auction.buyer", itemText, cost);
		
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		
		ListTag itemList = new ListTag();
		for(ItemWriteData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		compound.put("Price", this.cost.save());
		
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemWriteData(itemList.getCompound(i)));

		this.cost = MoneyValue.safeLoad(compound, "Price");
		
	}
	
}
