package io.github.lightman314.lightmanscurrency.api.trader.permissions.builtin;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.TriStatePermission;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class EnumPermissionType<T extends Enum<T> & StringRepresentable> extends PermissionType<T> {

    public static final EnumPermissionType<TriStatePermission> TRI_STATE = new EnumPermissionType<>(TriStatePermission.class,List.of(TriStatePermission.values()),TriStatePermission.CODEC,TriStatePermission.STREAM_CODEC);

    private final List<T> values;
    private final Codec<T> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec;
    private final Class<T> clazz;
    private List<Component> valueNames;

    public EnumPermissionType(Class<T> clazz,String name) { this(clazz,List.of(clazz.getEnumConstants()),StringRepresentable.fromEnum(clazz::getEnumConstants),EnumHelper.buildStreamCodec(clazz,name)); }
    public EnumPermissionType(Class<T> clazz,List<T> values,Codec<T> codec,StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {
        this.clazz = clazz;
        this.values = List.copyOf(values);
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public List<T> getAllValues() { return this.values; }
    public List<Component> getAllValueNames(Permission<T> permission) {
        if(this.valueNames == null) {
            List<Component> temp = new ArrayList<>();
            for(T value : this.values)
                temp.add(Component.translatable(getEntryKey(permission,value)));
            this.valueNames = List.copyOf(temp);
        }
        return this.valueNames;
    }

    @Override
    public boolean allowedValue(T value) { return this.values.contains(value); }
    @Override
    public Codec<T> codec() { return this.codec; }
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() { return this.streamCodec; }
    @Override
    public T getEmpty() { return this.values.getFirst(); }
    @Override
    public T getMaxValue() { return this.values.getLast(); }
    @Override
    public T getHighest(T value1, T value2) { return value1.ordinal() > value2.ordinal() ? value1 : value2; }
    @Override
    public Component getValueText(Permission<T> permission,T value) { return Component.translatable(getEntryKey(permission,value)); }

    @Override
    public Class<T> getValueClass() { return this.clazz; }

    public Permission<T> create(T defaultValue) { return this.create(() -> defaultValue); }
    public Permission<T> create(Supplier<T> defaultValue) { return new Permission<>(this,defaultValue); }

    public static <T extends Enum<T> & StringRepresentable> String getEntryKey(Permission<?> permission,T entry) { return permission.getDescriptionID() + ".state." + entry.getSerializedName(); }

}