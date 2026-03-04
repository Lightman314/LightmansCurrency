package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ItemTradeNotification extends SingleLineTaxableNotification {

	public static final NotificationType<ItemTradeNotification> TYPE = new Type();
	
	TraderCategory traderData = TraderCategory.NULL;
	
	TradeDirection tradeType = TradeDirection.OTHER;
	List<ItemData> items = new ArrayList<>();
	MoneyValue cost = MoneyValue.empty();
	
	String customer = "";

	private ItemTradeNotification(){}
    private ItemTradeNotification(TraderCategory trader, TradeDirection type, List<ItemData> items, MoneyValue cost, String customer, MoneyValue taxes, CommonData data) {
        super(taxes,data);
        this.traderData = trader;
        this.tradeType = type;
        this.items = items;
        this.cost = cost;
        this.customer = customer;
    }
	public ItemTradeNotification(ItemTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) {

		super(taxesPaid);

		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.items = new ArrayList<>();
		this.items.add(new ItemData(trade.getSellItem(0), trade.isPurchase() ? "" : trade.getCustomName(0)));
		this.items.add(new ItemData(trade.getSellItem(1), trade.isPurchase() ? "" : trade.getCustomName(1)));
		
		if(trade.isBarter())
		{
			this.items.add(new ItemData(trade.getBarterItem(0),""));
			this.items.add(new ItemData(trade.getBarterItem(1),""));
		}
		else
			this.cost = cost;
		
		this.customer = customer.getName(false);
		
	}

	public static Supplier<Notification> create(ItemTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory trader, MoneyValue taxesPaid) { return () -> new ItemTradeNotification(trade, cost, customer, trader, taxesPaid); }

	@Override
	public NotificationType<ItemTradeNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public Component getNormalMessage() {
		
		Component action = this.tradeType.getActionPhrase();

		Component itemText = ItemData.format(this.items.get(0), this.items.get(1));

		Component cost;
		if(this.tradeType == TradeDirection.BARTER)
		{
			//Flip the cost and item text, as for barters the text is backwards "bartered *barter items* for *sold items*"
			cost = itemText;
			itemText = ItemData.format(this.items.get(2), this.items.get(3));
		}
		else
			cost = this.cost.getText("NULL");

		//Create log from stored data
		return LCText.NOTIFICATION_TRADE_ITEM.get(this.customer, action, itemText, cost);
		
	}

	@Override
	protected void loadNormal(CompoundTag compound, HolderLookup.Provider lookup) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"),lookup);
		this.tradeType = TradeDirection.fromIndex(compound.getInt("TradeType"));
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i),lookup));
		if(this.tradeType != TradeDirection.BARTER)
			this.cost = MoneyValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ItemTradeNotification itn)
		{
			if(!itn.traderData.matches(this.traderData))
				return false;
			if(itn.tradeType != this.tradeType)
				return false;
			if(itn.items.size() != this.items.size())
				return false;
			for(int i = 0; i < this.items.size(); ++i)
			{
				if(!this.items.get(i).matches(itn.items.get(i)))
					return false;
			}
			if(!itn.cost.equals(this.cost))
				return false;
			if(!itn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return this.TaxesMatch(itn);
		}
		return false;
	}

    private static class Type extends NotificationType<ItemTradeNotification>
    {
        private static final MapCodec<ItemTradeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                TraderCategory.TYPE.codec().codec().fieldOf("trader").forGetter(n -> n.traderData),
                TradeDirection.CODEC.fieldOf("tradeType").forGetter(n -> n.tradeType),
                ItemData.LIST_CODEC.fieldOf("items").forGetter(n -> n.items),
                MoneyValue.CODEC.fieldOf("cost").forGetter(n -> n.cost),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer)
        ).and(taxableFields(builder)).apply(builder,ItemTradeNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,ItemTradeNotification> STREAM_CODEC = StreamHelper.combine(taxableStreamFields(),
                TraderCategory.TYPE.streamCodec(),n -> n.traderData,
                TradeDirection.STREAM_CODEC,n -> n.tradeType,
                ItemData.STREAM_CODEC_LIST,n -> n.items,
                MoneyValue.STREAM_CODEC,n -> n.cost,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                ItemTradeNotification::new);

        @Override
        protected ItemTradeNotification createNew() { return new ItemTradeNotification(); }
        @Override
        public MapCodec<ItemTradeNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemTradeNotification> streamCodec() { return STREAM_CODEC; }
    }
	
}
