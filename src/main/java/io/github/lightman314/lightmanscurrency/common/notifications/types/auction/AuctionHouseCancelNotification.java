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
import net.minecraft.network.codec.StreamCodec;

public class AuctionHouseCancelNotification extends AuctionHouseNotification{

	public static final NotificationType<AuctionHouseCancelNotification> TYPE = new Type();
	
	List<ItemData> items = ImmutableList.of();

	private AuctionHouseCancelNotification() { }
	private AuctionHouseCancelNotification(List<ItemData> items, CommonData data) {
        super(data);
        this.items = ImmutableList.copyOf(items);
    }

	public AuctionHouseCancelNotification(AuctionTradeData trade) {
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
	}

    @Override
	public NotificationType<AuctionHouseCancelNotification> getType() { return TYPE; }

	@Override
	public Component getMessage() {
		
		Component itemText = ItemData.getItemNames(this.items);
		
		//Create log from stored data
		return LCText.NOTIFICATION_AUCTION_CANCEL.get(itemText);
		
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));
		
	}

    private static class Type extends NotificationType<AuctionHouseCancelNotification>
    {
        private static final MapCodec<AuctionHouseCancelNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ItemData.LIST_CODEC.fieldOf("items").forGetter(n -> n.items),
                baseFields()
        ).apply(builder,AuctionHouseCancelNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,AuctionHouseCancelNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ItemData.STREAM_CODEC_LIST,n -> n.items,
                AuctionHouseCancelNotification::new);

        @Override
        protected AuctionHouseCancelNotification createNew() { return new AuctionHouseCancelNotification(); }
        @Override
        public MapCodec<AuctionHouseCancelNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,AuctionHouseCancelNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
