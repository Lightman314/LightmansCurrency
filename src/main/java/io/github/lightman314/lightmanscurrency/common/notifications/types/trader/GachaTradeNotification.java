package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.CommonData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class GachaTradeNotification extends SingleLineTaxableNotification {

    public static final NotificationType<GachaTradeNotification> TYPE = new Type();

    private TraderCategory traderData = TraderCategory.NULL;

    private ItemData item = ItemData.EMPTY;
    private MoneyValue cost = MoneyValue.empty();

    private String customer = "";

    private GachaTradeNotification() {}
    private GachaTradeNotification(TraderCategory trader, ItemData item, MoneyValue cost, String customer, MoneyValue taxes, CommonData data) {
        super(taxes,data);
        this.traderData = trader;
        this.item = item;
        this.cost = cost;
        this.customer = customer;
    }
    private GachaTradeNotification(ItemStack item, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxes)
    {
        super(taxes);
        this.traderData = traderData;
        this.item = new ItemData(item);
        this.cost = cost;

        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(ItemStack item, MoneyValue cost, PlayerReference customer, TraderCategory trader, MoneyValue taxesPaid) { return () -> new GachaTradeNotification(item,cost,customer,trader,taxesPaid); }

    
    @Override
    public NotificationType<?> getType() { return TYPE; }

    
    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    protected Component getNormalMessage() { return LCText.NOTIFICATION_TRADE_GACHA.get(this.customer,this.cost.getText("NULL"),this.item.format()); }

    @Override
    protected void loadNormal(CompoundTag compound, HolderLookup.Provider lookup) {
        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"),lookup);
        this.item = ItemData.load(compound.getCompound("Item"),lookup);
        this.cost = MoneyValue.safeLoad(compound,"Money");
        this.customer = compound.getString("Customer");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof GachaTradeNotification gtn)
            return gtn.traderData.matches(this.traderData) && gtn.item.matches(this.item) && gtn.cost.equals(this.cost) && gtn.customer.equals(this.customer) && gtn.TaxesMatch(this);
        return false;
    }

    private static class Type extends NotificationType<GachaTradeNotification>
    {
        private static final MapCodec<GachaTradeNotification> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                TraderCategory.TYPE.codec().codec().fieldOf("trader").forGetter(n -> n.traderData),
                ItemData.CODEC.fieldOf("item").forGetter(n -> n.item),
                MoneyValue.CODEC.fieldOf("cost").forGetter(n -> n.cost),
                Codec.STRING.fieldOf("customer").forGetter(n -> n.customer)
        ).and(taxableFields(builder)).apply(builder,GachaTradeNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,GachaTradeNotification> STREAM_CODEC = StreamHelper.combine(taxableStreamFields(),
                TraderCategory.TYPE.streamCodec(),n -> n.traderData,
                ItemData.STREAM_CODEC,n -> n.item,
                MoneyValue.STREAM_CODEC,n -> n.cost,
                ByteBufCodecs.STRING_UTF8,n -> n.customer,
                GachaTradeNotification::new);

        @Override
        protected GachaTradeNotification createNew() { return new GachaTradeNotification(); }
        @Override
        public MapCodec<GachaTradeNotification> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GachaTradeNotification> streamCodec() { return STREAM_CODEC; }
    }

}
