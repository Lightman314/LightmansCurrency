package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AuctionHouseBidNotification extends AuctionHouseNotification{

	public static final NotificationType<AuctionHouseBidNotification> TYPE = new Type();
	
	List<ItemData> items = ImmutableList.of();
	MoneyValue cost = MoneyValue.empty();
	
	String customer = "";

	private AuctionHouseBidNotification() { }

    private AuctionHouseBidNotification(List<ItemData> items, MoneyValue cost, String customer, CommonData data) {
        super(data);
        this.items = ImmutableList.copyOf(items);
        this.cost = cost;
        this.customer = customer;
    }

	public AuctionHouseBidNotification(AuctionTradeData trade) {
		
		this.cost = trade.getLastBidAmount();
		this.customer = trade.getLastBidPlayer().getName(false);
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}



    @Override
	public NotificationType<AuctionHouseBidNotification> getType() { return TYPE; }

	@Override
	public Component getMessage() {

		Component itemText = ItemData.getItemNames(this.items);
		
		Component cost = this.cost.getText();
		
		//Create log from stored data
		return LCText.NOTIFICATION_AUCTION_BID.get(this.customer, itemText, cost);
		
	}

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));
		this.cost = MoneyValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}

    private static class Type extends NotificationType<AuctionHouseBidNotification>
    {

        private static final MapCodec<AuctionHouseBidNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ItemData.LIST_CODEC.fieldOf("items").forGetter(n -> n.items),
                MoneyValue.CODEC.fieldOf("price").forGetter(n -> n.cost),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer),
                baseFields()
        ).apply(builder,AuctionHouseBidNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,AuctionHouseBidNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ItemData.STREAM_CODEC_LIST,n -> n.items,
                MoneyValue.STREAM_CODEC,n -> n.cost,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                AuctionHouseBidNotification::new);

        @Override
        protected AuctionHouseBidNotification createNew() { return new AuctionHouseBidNotification(); }
        @Override
        public MapCodec<AuctionHouseBidNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AuctionHouseBidNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
