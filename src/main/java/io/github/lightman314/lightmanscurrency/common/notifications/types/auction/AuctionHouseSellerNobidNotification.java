package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class AuctionHouseSellerNobidNotification extends AuctionHouseNotification{

	public static final NotificationType<AuctionHouseSellerNobidNotification> TYPE = new Type();
	List<ItemData> items = ImmutableList.of();

	private AuctionHouseSellerNobidNotification() {}
	private AuctionHouseSellerNobidNotification(List<ItemData> items, CommonData data) {
        super(data);
        this.items = ImmutableList.copyOf(items);
    }

	public AuctionHouseSellerNobidNotification(AuctionTradeData trade) {
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}

    @Override
	public NotificationType<AuctionHouseSellerNobidNotification> getType() { return TYPE; }

	@Override
	public MutableComponent getMessage() {
		
		Component itemText = ItemData.getItemNames(this.items);
		
		//Create log from stored data
		return LCText.NOTIFICATION_AUCTION_SELLER_NO_BID.get(itemText);
		
	}

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));
		
	}

    private static class Type extends NotificationType<AuctionHouseSellerNobidNotification>
    {
        private static final MapCodec<AuctionHouseSellerNobidNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ItemData.LIST_CODEC.fieldOf("items").forGetter(n -> n.items),
                baseFields()
        ).apply(builder,AuctionHouseSellerNobidNotification::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,AuctionHouseSellerNobidNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ItemData.STREAM_CODEC_LIST,n -> n.items,
                AuctionHouseSellerNobidNotification::new);

        @Override
        protected AuctionHouseSellerNobidNotification createNew() { return new AuctionHouseSellerNobidNotification(); }
        @Override
        public MapCodec<AuctionHouseSellerNobidNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,AuctionHouseSellerNobidNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
