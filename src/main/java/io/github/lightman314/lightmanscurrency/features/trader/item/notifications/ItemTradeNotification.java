package io.github.lightman314.lightmanscurrency.features.trader.item.notifications;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.TraderNotification;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class ItemTradeNotification extends TraderNotification.TaxableSingleLine {

    private static final MapCodec<ItemTradeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TradeDirection.CODEC.fieldOf("type").forGetter(n -> n.type),
            TradePriceReceipt.CODEC.fieldOf("cost").forGetter(n -> n.cost),
            CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("items").forGetter(n -> n.items),
            ComponentSerialization.CODEC.fieldOf("customer").forGetter(n -> n.customer),
            TraderCategory.TRADER_CATEGORY_CODEC.fieldOf("trader").forGetter(ItemTradeNotification::getCategory),
            MoneyValue.CODEC.fieldOf("taxesPaid").forGetter(ItemTradeNotification::getTaxesPaid)
    ).apply(builder,ItemTradeNotification::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,ItemTradeNotification> STREAM_CODEC = StreamCodec.composite(
            TradeDirection.STREAM_CODEC,n -> n.type,
            TradePriceReceipt.STREAM_CODEC,n -> n.cost,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),n -> n.items,
            ComponentSerialization.STREAM_CODEC,n -> n.customer,
            TraderCategory.TRADER_CATEGORY_STREAM_CODEC,ItemTradeNotification::getCategory,
            MoneyValue.STREAM_CODEC,ItemTradeNotification::getTaxesPaid,
            ItemTradeNotification::new);

    public static final NotificationType<ItemTradeNotification> TYPE = new NotificationType<>(MAP_CODEC,STREAM_CODEC);

    public static final TextEntry TEXT = TextEntry.notification(TYPE);

    private final TradeDirection type;
    private final TradePriceReceipt cost;
    private final List<ItemStack> items;
    private final Component customer;

    public ItemTradeNotification(TradeDirection type,TradePriceReceipt price,List<ItemStack> soldItems,Component customer,TraderCategory trader,MoneyValue taxesPaid) {
        super(trader,taxesPaid);
        this.type = type;
        this.cost = price;
        this.items = List.copyOf(ItemHelper.combineStacks(soldItems));
        this.customer = customer;
    }

    @Override
    protected Component getMessage() {
        return TEXT.get(
                this.customer,
                this.type.getAction(),
                ItemHelper.formatItemNames(this.items),
                this.cost.getText());
    }

    @Override
    public NotificationType<?> getType() { return TYPE; }

    @Override
    protected boolean equals(Notification other) {
        if(other instanceof ItemTradeNotification n)
            return this.type == n.type && ItemHelper.listsMatch(this.items,n.items) && this.cost.equals(n.cost) && this.customer.equals(n.customer) && super.parentDataMatches(n);
        return false;
    }

    @Override
    protected int hash() { return Objects.hash(this.type,ItemHelper.hashList(this.items),this.cost,this.customer,this.hashParentData()); }

}
