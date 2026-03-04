package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;
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

public class AuctionHouseSellerNotification extends Notification {

	public static final NotificationType<AuctionHouseSellerNotification> TYPE = new Type();
	
	List<ItemData> items = ImmutableList.of();
	MoneyValue highestBid = MoneyValue.empty();
	Optional<MoneyValue> payment = Optional.empty();
    Optional<MoneyValue> fee = Optional.empty();
	
	String customer = "";

	private AuctionHouseSellerNotification() { }
	private AuctionHouseSellerNotification(List<ItemData> items, MoneyValue highestBid, Optional<MoneyValue> payment, Optional<MoneyValue> fee, String customer, CommonData data) {
        super(data);
        this.items = ImmutableList.copyOf(items);
        this.highestBid = highestBid;
        this.payment = payment;
        this.fee = fee;
        this.customer = customer;
    }

	public AuctionHouseSellerNotification(AuctionTradeData trade, MoneyValue payment, MoneyValue fee) {
		
		this.highestBid = trade.getLastBidAmount();
		this.payment = payment.isEmpty() ? Optional.empty() : Optional.of(payment);
		this.fee = fee.isEmpty() ? Optional.empty() : Optional.of(fee);

		this.customer = trade.getLastBidPlayer().getName(false);
		
		this.items = new ArrayList<>();
		for(int i = 0; i < trade.getAuctionItems().size(); ++i)
			this.items.add(new ItemData(trade.getAuctionItems().get(i)));
		
	}

    @Override
	public NotificationType<AuctionHouseSellerNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }

	@Override
	public List<Component> getMessageLines() {
		Component itemText = ItemData.getItemNames(this.items);

		Component cost = this.highestBid.getText("0");

		//Create log from stored data
        Component line1 = LCText.NOTIFICATION_AUCTION_SELLER.get(this.customer, itemText, cost);

		if(this.payment.isEmpty() || this.fee.isEmpty())
			return List.of(line1);

        Component line2 = LCText.NOTIFICATION_AUCTION_SELLER_FEE.get(this.payment.get().getText("0"),this.fee.get().getText("0"));
		return List.of(line1,line2);

	}

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));
		this.highestBid = MoneyValue.safeLoad(compound, "Price");
		if(compound.contains("Payment") && compound.contains("Fee"))
		{
			this.payment = Optional.of(MoneyValue.load(compound.getCompound("Payment")));
			this.fee = Optional.of(MoneyValue.load(compound.getCompound("Fee")));
		}
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(Notification other) { return false; }

    private static class Type extends NotificationType<AuctionHouseSellerNotification>
    {
        private static final MapCodec<AuctionHouseSellerNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ItemData.LIST_CODEC.fieldOf("items").forGetter(n -> n.items),
                MoneyValue.CODEC.fieldOf("bid").forGetter(n -> n.highestBid),
                MoneyValue.CODEC.optionalFieldOf("payment").forGetter(n -> n.payment),
                MoneyValue.CODEC.optionalFieldOf("fee").forGetter(n -> n.fee),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer),
                baseFields()
        ).apply(builder,AuctionHouseSellerNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,AuctionHouseSellerNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ItemData.STREAM_CODEC_LIST,n -> n.items,
                MoneyValue.STREAM_CODEC,n -> n.highestBid,
                ByteBufCodecs.optional(MoneyValue.STREAM_CODEC),n -> n.payment,
                ByteBufCodecs.optional(MoneyValue.STREAM_CODEC),n -> n.fee,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                AuctionHouseSellerNotification::new);

        @Override
        protected AuctionHouseSellerNotification createNew() { return new AuctionHouseSellerNotification(); }
        @Override
        public MapCodec<AuctionHouseSellerNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,AuctionHouseSellerNotification> streamCodec() { return STREAM_CODEC; }
    }

}
