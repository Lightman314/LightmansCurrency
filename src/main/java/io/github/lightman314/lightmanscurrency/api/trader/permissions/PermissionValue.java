package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public final class PermissionValue<T> {

    public static final StreamCodec<RegistryFriendlyByteBuf,PermissionValue<?>> STREAM_CODEC = StreamCodec.of((buf,val) -> {
        Permission.STREAM_CODEC.encode(buf,val.permission);
        val.encode(buf);
    },buf -> {
        Permission<?> permission = Permission.STREAM_CODEC.decode(buf);
        Object val = permission.getType().streamCodec().decode(buf);
        return forceParse(permission,val);
    });

    private static final Map<PermissionType<?>,Codec<PermissionValue<?>>> codecCache = new HashMap<>();

    public static <T> Codec<PermissionValue<?>> getCodec(Permission<T> permission)
    {
        PermissionType<T> type = permission.getType();
        if(!codecCache.containsKey(type))
        {
            Codec<PermissionValue<?>> codec = type.codec().xmap(value -> new PermissionValue<>(permission,value),val -> (T)val.get());
            codecCache.put(type,codec);
        }
        return codecCache.get(type);
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

    public boolean is(Permission<?> perm) { return perm != null && this.permission == perm; }

    public T get() { return this.value; }
    public void trySet(T value)
    {
        if(this.permission.allowedValue(value))
            this.value = value;
    }

    @Override
    public String toString() {
        return "PermissionValue[" + LCRegistries.Trader.PERMISSION.getKey(this.permission) + ";" + this.value + "]";
    }

}
