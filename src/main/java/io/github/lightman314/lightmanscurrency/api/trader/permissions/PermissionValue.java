package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PermissionValue<T> {

    public static final Codec<PermissionValue<?>> CODEC = Permission.CODEC.dispatch(PermissionValue::getPerm,PermissionValue::getMapCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf,PermissionValue<?>> STREAM_CODEC = StreamCodec.of((buf,val) -> {
        Permission.STREAM_CODEC.encode(buf,val.permission);
        val.encode(buf);
    },buf -> {
        Permission<?> permission = Permission.STREAM_CODEC.decode(buf);
        Object val = permission.getType().streamCodec().decode(buf);
        return forceParse(permission,val);
    });

    private static final Map<Permission<?>,Codec<PermissionValue<?>>> codecCache = new HashMap<>();
    private static final Map<Permission<?>,MapCodec<PermissionValue<?>>> mapCodecCache = new HashMap<>();

    public static <T> Codec<PermissionValue<?>> getCodec(Permission<T> permission)
    {
        PermissionType<T> type = permission.getType();
        if(!codecCache.containsKey(permission))
        {
            Codec<PermissionValue<?>> codec = type.codec().xmap(value -> new PermissionValue<>(permission,value),val -> (T)val.get());
            //Make a fallback codec that will load the default value so that permission maps won't break entire traders if I decided to change a permission's type
            Codec<PermissionValue<T>> fallbackCodec = MapCodec.unitCodec(permission::createNew);
            codecCache.put(permission,Codec.withAlternative(codec,fallbackCodec));
        }
        return codecCache.get(permission);
    }
    private static <T> MapCodec<PermissionValue<?>> getMapCodec(Permission<T> permission) {
        PermissionType<T> type = permission.getType();
        if(!mapCodecCache.containsKey(permission))
        {
            MapCodec<PermissionValue<?>> codec = permission.getType().codec().fieldOf("value").xmap(value -> new PermissionValue<>(permission,value),val -> (T)val.get());
            //Make a fallback codec that will load the default value so that permission maps won't break entire traders if I decided to change a permission's type
            MapCodec<PermissionValue<T>> fallbackCodec = MapCodec.unit(permission::createNew);
            MapCodec<PermissionValue<?>> either = Codec.mapEither(codec,fallbackCodec).xmap(Either::unwrap,Either::left);
            mapCodecCache.put(permission,either);
        }
        return mapCodecCache.get(permission);
    }

    private final Permission<T> permission;
    public Permission<T> getPerm() { return this.permission; }
    public String getKey() { return this.permission.getKey().toString(); }
    private T value;
    public PermissionValue(Permission<T> permission, T value)
    {
        this.permission = permission;
        this.value = value;
    }

    private static <T> PermissionValue<T> forceParse(Permission<T> permission,Object value) { return new PermissionValue<>(permission,(T)value); }

    private void encode(RegistryFriendlyByteBuf buffer) { this.permission.getType().streamCodec().encode(buffer,this.value); }

    public boolean is(Permission<?> perm) { return this.permission == perm; }

    public T get() { return this.value; }
    public void trySet(T value)
    {
        if(this.permission.allowedValue(value))
            this.value = value;
    }
    public boolean setUnsafe(@Nullable Object value) {
        if(this.permission.isValueType(value)) {
            T v = (T)value;
            if(this.permission.allowedValue(v) && !this.value.equals(v)) {
                this.value = v;
                return true;
            }
        }
        return false;
    }

    public Component getName() { return this.permission.getPermissionName(); }

    public Component getValueText() { return this.permission.getValueText(this.value); }

    public PermissionValue<T> copy() { return new PermissionValue<>(this.permission,this.value); }

    @Override
    public String toString() {
        return "PermissionValue[" + LCRegistries.Trader.PERMISSION.getKey(this.permission) + ";" + this.value + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        return obj instanceof PermissionValue<?> pv && pv.permission == this.permission && pv.value.equals(this.value);
    }

    @Override
    public int hashCode() { return Objects.hash(this.permission,this.value); }

}