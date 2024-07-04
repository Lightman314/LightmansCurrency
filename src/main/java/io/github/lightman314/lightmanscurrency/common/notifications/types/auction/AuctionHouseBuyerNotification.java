package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class AuctionHouseBuyerNotification extends AuctionHouseNotification {

	public static final NotificationType<AuctionHouseBuyerNotification> TYPE = new NotificationType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "auction_house_buyer"),AuctionHouseBuyerNotification::new);
	
	List<ItemData> items;
	MoneyValue cost = MoneyValue.empty();

	private AuctionHouseBuyerNotification() {}

	public AuctionHouseBuyerNotification(AuctionTradeData trade) {
		
		this.cost = trade.getLastBidAmount();
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}
	
	@Nonnull
    @Override
	protected NotificationType<AuctionHouseBuyerNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public MutableComponent getMessage() {
		
		Component itemText = ItemData.getItemNames(this.items);
		
		Component cost = this.cost.getText("0");
		
		//Create log from stored data
		return LCText.NOTIFICATION_AUCTION_BUYER.get(itemText, cost);
		
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		ListTag itemList = new ListTag();
		for(ItemData item : this.items)
			itemList.add(item.save(lookup));
		compound.put("Items", itemList);
		compound.put("Price", this.cost.save());
		
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));

		this.cost = MoneyValue.safeLoad(compound, "Price");
		
	}
	
}
