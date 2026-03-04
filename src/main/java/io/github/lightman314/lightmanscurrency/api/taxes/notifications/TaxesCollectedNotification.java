package io.github.lightman314.lightmanscurrency.api.taxes.notifications;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.categories.TaxEntryCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class TaxesCollectedNotification extends SingleLineNotification {

    public static final NotificationType<TaxesCollectedNotification> TYPE = new Type();

    private Component taxedName = EasyText.literal("NULL");
    private MoneyValue amount = MoneyValue.empty();
    private TaxEntryCategory category = new TaxEntryCategory(EasyText.empty(),-100);

    private TaxesCollectedNotification() {}
    private TaxesCollectedNotification(Component taxedName, MoneyValue amount, TaxEntryCategory category) { this.taxedName = taxedName; this.amount = amount; this.category = category; }
    private TaxesCollectedNotification(Component taxedName, MoneyValue amount, TaxEntryCategory category, CommonData data) { super(data); this.taxedName = taxedName; this.amount = amount; this.category = category; }

    public static Supplier<Notification> create(Component taxedName, MoneyValue amount, TaxEntryCategory category) { return () -> new TaxesCollectedNotification(taxedName, amount, category); }

    @Override
    public NotificationType<TaxesCollectedNotification> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Override
    public Component getMessage() { return LCText.NOTIFICATION_TAXES_COLLECTED.get(this.amount.getText("NULL"), this.taxedName); }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.taxedName = Component.Serializer.fromJson(compound.getString("TaxedName"), lookup);
        this.amount = MoneyValue.load(compound.getCompound("Amount"));
        this.category = new TaxEntryCategory(compound.getCompound("Category"), lookup);
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof TaxesCollectedNotification tcn)
            return tcn.taxedName.getString().equals(this.taxedName.getString()) && tcn.amount.equals(this.amount) && tcn.category.matches(this.category);
        return false;
    }

    private static class Type extends NotificationType<TaxesCollectedNotification>
    {

        private static final MapCodec<TaxesCollectedNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("taxedName").forGetter(n -> n.taxedName),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount),
                TaxEntryCategory.TYPE.codec().fieldOf("category").forGetter(n -> n.category),
                baseFields())
                .apply(builder,TaxesCollectedNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,TaxesCollectedNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.taxedName,
                MoneyValue.STREAM_CODEC,n -> n.amount,
                TaxEntryCategory.TYPE.streamCodec(),n -> n.category,
                TaxesCollectedNotification::new);

        @Override
        protected TaxesCollectedNotification createNew() { return new TaxesCollectedNotification(); }
        @Override
        public MapCodec<TaxesCollectedNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,TaxesCollectedNotification> streamCodec() { return STREAM_CODEC; }

    }

}
