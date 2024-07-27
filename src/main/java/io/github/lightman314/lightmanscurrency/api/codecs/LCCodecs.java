package io.github.lightman314.lightmanscurrency.api.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.taxes.reference.TaxableReference;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LCCodecs {

    private LCCodecs() {}

    public static final Codec<MoneyValue> MONEY_VALUE = easyCodec(MoneyValue::save,MoneyValue::load,"Money Value");
    public static final StreamCodec<ByteBuf,MoneyValue> MONEY_VALUE_STREAM = ByteBufCodecs.fromCodecTrusted(MONEY_VALUE);

    public static final Codec<MoneyValue> MONEY_VALUE_NON_EMPTY = MONEY_VALUE.validate(value -> {
        if(value.isEmpty() && !value.isFree())
            return DataResult.error(() -> "Money Value cannot be empty!");
        return DataResult.success(value);
    });

    public static final Codec<BankReference> BANK_REFERENCE = easyCodec(BankReference::save,BankReference::load,"Bank Reference");
    public static final StreamCodec<ByteBuf,BankReference> BANK_REFERENCE_STREAM = ByteBufCodecs.fromCodec(BANK_REFERENCE);

    public static final Codec<TaxableReference> TAXABLE_REFERENCE = easyCodec(TaxableReference::save,TaxableReference::load,"Taxable Reference");
    public static final StreamCodec<ByteBuf,TaxableReference> TAXABLE_REFERENCE_STREAM = ByteBufCodecs.fromCodec(TAXABLE_REFERENCE);

    public static final Codec<PlayerReference> PLAYER_REFERENCE = easyCodec(PlayerReference::save,PlayerReference::load,"Player Reference");
    public static final StreamCodec<ByteBuf,PlayerReference> PLAYER_REFERENCE_STREAM = ByteBufCodecs.fromCodec(PLAYER_REFERENCE);

    public static final Codec<Notification> NOTIFICATION = easyCodec2(Notification::save, NotificationAPI::loadNotification, "Notification");
    public static final StreamCodec<RegistryFriendlyByteBuf,Notification> NOTIFICATION_STREAM = ByteBufCodecs.fromCodecWithRegistries(NOTIFICATION);

    public static final Codec<NotificationCategory> NOTIFICATION_CATEGORY = easyCodec2(NotificationCategory::save,NotificationAPI::loadCategory, "Notification Category");
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationCategory> NOTIFICATION_CATEGORY_STREAM = ByteBufCodecs.fromCodecWithRegistries(NOTIFICATION_CATEGORY);

    public static final Codec<Owner> OWNER = easyCodec2(Owner::save, Owner::load, "Owner");
    public static final StreamCodec<RegistryFriendlyByteBuf,Owner> OWNER_STREAM = ByteBufCodecs.fromCodecWithRegistries(OWNER);

    public static final StreamCodec<RegistryFriendlyByteBuf,LazyPacketData> PACKET_DATA_STREAM = StreamCodec.of((b,d) -> d.encode(b),LazyPacketData::decode);

    private static <T> Codec<T> easyCodec(@Nonnull final Function<T,CompoundTag> save, @Nonnull final Function<CompoundTag,T> load, @Nonnull final String object)
    {
        return CompoundTag.CODEC.comapFlatMap(tag -> {
            T result = load.apply(tag);
            if(result == null)
                return DataResult.error(() -> object + " could not be decoded!");
            return DataResult.success(result);
        },save);
    }
    private static <T> Codec<T> easyCodec2(@Nonnull final BiFunction<T, HolderLookup.Provider,CompoundTag> save, @Nonnull final BiFunction<CompoundTag,HolderLookup.Provider,T> load, @Nonnull final String object)
    {
        return CompoundTag.CODEC.comapFlatMap(tag -> {
            T result = load.apply(tag, LookupHelper.getRegistryAccess(false));
            if(result == null)
                return DataResult.error(() -> object + " could not be decoded!");
            return DataResult.success(result);
        },t -> save.apply(t,LookupHelper.getRegistryAccess(false)));
    }

}
