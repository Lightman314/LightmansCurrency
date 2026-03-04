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
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class LowBalanceNotification extends SingleLineNotification {

	public static final NotificationType<LowBalanceNotification> TYPE = new Type();
	
	private Component accountName = EasyText.empty();
	private MoneyValue value = MoneyValue.empty();

	private LowBalanceNotification() { }
    private LowBalanceNotification(Component accountName, MoneyValue value, CommonData data) {
        super(data);
        this.accountName = accountName;
        this.value = value;
    }
	protected LowBalanceNotification(Component accountName, MoneyValue value) {
		this.accountName = accountName;
		this.value = value;
	}

	public static Supplier<Notification> create(Component accountName, MoneyValue value) { return () -> new LowBalanceNotification(accountName,value); }

    @Override
	public NotificationType<LowBalanceNotification> getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

	@Override
	public Component getMessage() { return LCText.NOTIFICATION_BANK_LOW_BALANCE.get(this.value.getText()); }

	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		this.accountName = Component.Serializer.fromJson(compound.getString("Name"),lookup);
		this.value = MoneyValue.safeLoad(compound, "Amount");
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof LowBalanceNotification lbn)
		{
			if(!lbn.accountName.getString().equals(this.accountName.getString()))
				return false;
			if(!lbn.value.equals(this.value))
				return false;
			//Passed all the checks.
			return true;
		}
		return false;
	}

    private static class Type extends NotificationType<LowBalanceNotification>
    {
        private static final MapCodec<LowBalanceNotification> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("account").forGetter(n -> n.accountName),
                MoneyValue.CODEC.fieldOf("amount").forGetter(n -> n.value),
                baseFields()
        ).apply(builder,LowBalanceNotification::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,LowBalanceNotification> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.accountName,
                MoneyValue.STREAM_CODEC,n -> n.value,
                LowBalanceNotification::new);

        @Override
        protected LowBalanceNotification createNew() { return new LowBalanceNotification(); }
        @Override
        public MapCodec<LowBalanceNotification> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LowBalanceNotification> streamCodec() { return STREAM_CODEC; }
    }
	
	
	
}
