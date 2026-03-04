package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class BankInterestNotification extends SingleLineNotification {

    public static final NotificationType<BankInterestNotification> TYPE = new Type();

    protected Component accountName = EasyText.empty();
    protected MoneyValue amount = MoneyValue.empty();

    protected BankInterestNotification() {}
    protected BankInterestNotification(Component accountName, MoneyValue amount, CommonData data) {
        super(data);
        this.accountName = accountName;
        this.amount = amount;
    }
    protected BankInterestNotification(Component accountName, MoneyValue amount)
    {
        this.accountName = accountName;
        this.amount = amount;
    }

    public static Supplier<Notification> create(MutableComponent accountName, MoneyValue amount) { return () -> new BankInterestNotification(accountName,amount); }

    @Override
    public NotificationType<BankInterestNotification> getType() { return TYPE; }
    
    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Override
    public MutableComponent getMessage() { return LCText.NOTIFICATION_BANK_INTEREST.get(this.amount.getText()); }

    @Override
    protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
        this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
        this.amount = MoneyValue.safeLoad(compound, "Amount");
    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

    private static class Type extends NotificationType<BankInterestNotification>
    {
        private static final MapCodec<BankInterestNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.amount)
        ).and(baseFields()).apply(builder,BankInterestNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,BankInterestNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.accountName,
                MoneyValue.STREAM_CODEC,n -> n.amount,
                BankInterestNotification::new);

        @Override
        protected BankInterestNotification createNew() { return new BankInterestNotification(); }
        @Override
        public MapCodec<BankInterestNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BankInterestNotification> streamCodec() { return STREAM_CODEC; }
    }

}
