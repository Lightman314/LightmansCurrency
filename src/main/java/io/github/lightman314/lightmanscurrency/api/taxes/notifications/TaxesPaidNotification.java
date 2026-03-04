package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class TaxesPaidNotification extends SingleLineNotification {

    public static final NotificationType<TaxesPaidNotification> TYPE = new Type();

    private MoneyValue amount = MoneyValue.empty();
    private NotificationCategory category = NotificationCategory.GENERAL;

    private TaxesPaidNotification() {}
    private TaxesPaidNotification(MoneyValue amount, NotificationCategory category) { this.amount = amount; this.category = category;  }
    private TaxesPaidNotification(MoneyValue amount, NotificationCategory category, CommonData data) { super(data); this.amount = amount; this.category = category; }

    public static Supplier<Notification> create(MoneyValue amount, NotificationCategory category) { return () -> new TaxesPaidNotification(amount, category); }

    @Override
    public NotificationType<TaxesPaidNotification> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Override
    public MutableComponent getMessage() {
        if(this.amount.isEmpty())
            return LCText.NOTIFICATION_TAXES_PAID_NULL.get();
        else
            return LCText.NOTIFICATION_TAXES_PAID.get(this.amount.getText());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup)
    {
        this.amount = MoneyValue.load(compound.getCompound("Amount"));
        this.category = NotificationAPI.getApi().LoadCategory(compound.getCompound("Category"),lookup);
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof TaxesPaidNotification tpn)
            return tpn.amount.equals(this.amount) && tpn.category.matches(this.category);
        return false;
    }

    private static class Type extends NotificationType<TaxesPaidNotification>
    {

        private static final MapCodec<TaxesPaidNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount),
                NotificationCategory.CODEC.fieldOf("category").forGetter(n -> n.category),
                baseFields()
                ).apply(builder,TaxesPaidNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,TaxesPaidNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                MoneyValue.STREAM_CODEC,n -> n.amount,
                NotificationCategory.STREAM_CODEC,n -> n.category,
                TaxesPaidNotification::new);

        @Override
        protected TaxesPaidNotification createNew() { return new TaxesPaidNotification(); }
        @Override
        public MapCodec<TaxesPaidNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TaxesPaidNotification> streamCodec() { return STREAM_CODEC; }

    }

}
