package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class AuctionHouseBuyerNotification extends AuctionHouseNotification {

	public static final NotificationType<AuctionHouseBuyerNotification> TYPE = new Type();
	
	List<ItemData> items = ImmutableList.of();
	MoneyValue cost = MoneyValue.empty();

	private AuctionHouseBuyerNotification() {}
    private AuctionHouseBuyerNotification(List<ItemData> items, MoneyValue cost, CommonData data) {
        super(data);
        this.items = ImmutableList.copyOf(items);
        this.cost = cost;
    }
	public AuctionHouseBuyerNotification(AuctionTradeData trade) {
		
		this.cost = trade.getLastBidAmount();
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}

    @Override
	public NotificationType<AuctionHouseBuyerNotification> getType() { return TYPE; }

	@Override
	public Component getMessage() {
		
		Component itemText = ItemData.getItemNames(this.items);
		
		Component cost = this.cost.getText("0");
		
		//Create log from stored data
		return LCText.NOTIFICATION_AUCTION_BUYER.get(itemText, cost);
		
	}

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));

		this.cost = MoneyValue.safeLoad(compound, "Price");
		
	}

    private static class Type extends NotificationType<AuctionHouseBuyerNotification>
    {
        private static final MapCodec<AuctionHouseBuyerNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ItemData.LIST_CODEC.fieldOf("items").forGetter(n -> n.items),
                MoneyValue.CODEC.fieldOf("bid").forGetter(n -> n.cost),
                baseFields()
        ).apply(builder,AuctionHouseBuyerNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,AuctionHouseBuyerNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ItemData.STREAM_CODEC_LIST,n -> n.items,
                MoneyValue.STREAM_CODEC,n -> n.cost,
                AuctionHouseBuyerNotification::new);

        @Override
        protected AuctionHouseBuyerNotification createNew() { return new AuctionHouseBuyerNotification(); }
        @Override
        public MapCodec<AuctionHouseBuyerNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AuctionHouseBuyerNotification> streamCodec() { return STREAM_CODEC; }

    }
	
}
