package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Permission<T> {

    public static final StreamCodec<RegistryFriendlyByteBuf,Permission<?>> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Trader.PERMISSION_KEY);
    public static final Codec<Map<Permission<?>,PermissionValue<?>>> DATA_CODEC = Codec.dispatchedMap(LCRegistries.Trader.PERMISSION.byNameCodec(),PermissionValue::getCodec);

    public Identifier getKey() { return LCRegistries.Trader.PERMISSION.getKey(this); }
    private final PermissionType<T> type;
    public PermissionType<T> getType() { return this.type; }
    private final Supplier<T> defaultValue;
    private final Supplier<T> maxValue;
    private final Predicate<T> allowedValue;

    public Permission(PermissionType<T> type,Supplier<T> defaultValue) { this(type,defaultValue, Predicates.alwaysTrue()); }
    public Permission(PermissionType<T> type,Supplier<T> defaultValue,Supplier<T> maxValue) { this(type,defaultValue,Optional.of(maxValue),Predicates.alwaysTrue()); }
    public Permission(PermissionType<T> type,Supplier<T> defaultValue,Predicate<T> allowedValue) { this(type,defaultValue,Optional.empty(),allowedValue); }
    public Permission(PermissionType<T> type,Supplier<T> defaultValue,Supplier<T> maxValue, Predicate<T> allowedValue) { this(type,defaultValue,Optional.of(maxValue),allowedValue); }
    private Permission(PermissionType<T> type,Supplier<T> defaultValue,Optional<Supplier<T>> maxValue, Predicate<T> allowedValue)
    {
        this.type = type;
        this.defaultValue = defaultValue;
        this.maxValue = maxValue.orElse(this.type::getMaxValue);
        this.allowedValue = allowedValue;
    }

    public T getEmpty() { return this.getType().getEmpty(); }
    public T getMaxValue() {
        T maxValue = this.maxValue.get();
        if(!this.allowedValue(maxValue))
            throw new IllegalStateException("Permission of type " + this.getKey() + " has an illegally defined maximum value that does not match its allowed values.");
        return maxValue;
    }
    public T getHighest(T value1, T value2) { return this.type.getHighest(value1,value2); }

    public boolean allowedValue(T value) { return this.allowedValue.test(value) && this.type.allowedValue(value); }

    public Component getName() {
        Identifier key = this.getKey();
        return Component.translatable("gui." + key.getNamespace() + ".permission." + key.getPath());
    }

    public PermissionValue<T> createNew() { return new PermissionValue<>(this,this.defaultValue.get()); }

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Trader.PERMISSION,this); }
    @Override
    public String toString() { return "Permission[" + LCRegistries.Trader.PERMISSION.getKey(this) + "," + LCRegistries.Trader.PERMISSION_TYPE.getKey(this.type) + "," + this.defaultValue.get() + "]"; }

}