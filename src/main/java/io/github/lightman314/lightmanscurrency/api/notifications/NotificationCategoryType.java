package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public abstract class NotificationCategoryType<T extends NotificationCategory> {

    public static final Codec<NotificationCategoryType<?>> CODEC = LCRegistries.NOTIFICATION_CATEGORIES.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf,NotificationCategoryType<?>> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.NOTIFICATION_CATEGORY_KEY);

    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    public abstract T loadOldData(CompoundTag tag, HolderLookup.Provider lookup);
    @Override
    public String toString() { return "NotificationCategoryType[" + LCRegistries.NOTIFICATION_CATEGORIES.getKey(this) + "]"; }

    public static final class Instance<T extends NotificationCategory> extends NotificationCategoryType<T>
    {
        private final MapCodec<T> codec;
        private final StreamCodec<ByteBuf,T> streamCodec;
        private final T instance;
        public Instance(T instance) {
            this.codec = MapCodec.unit(instance);
            this.streamCodec = StreamCodec.unit(instance);
            this.instance = instance;
        }
        @Override
        public MapCodec<T> codec() { return this.codec; }
        @Override
        public StreamCodec<ByteBuf,T> streamCodec() { return this.streamCodec; }
        @Override
        public T loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { return this.instance; }
    }

}
