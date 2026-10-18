package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Permission<T> extends AbstractType<Permission<?>> {

    public static final Codec<Permission<?>> CODEC = LCRegistries.Trader.PERMISSION.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf,Permission<?>> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Trader.PERMISSION_KEY);
    public static final Codec<Map<Permission<?>,PermissionValue<?>>> DATA_CODEC = Codec.dispatchedMap(CODEC,PermissionValue::getCodec);

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

    public Class<T> getValueClass() { return this.type.getValueClass(); }
    public boolean isValueType(Object value) { return this.getValueClass().isInstance(value); }

    public Component getValueText(T value) { return this.type.getValueText(this,value); }

    private String descriptionID;
    private String tooltipID;
    public String getDescriptionID() {
        if(this.descriptionID == null)
            this.descriptionID = Util.makeDescriptionId("trader_permission",this.getKey());
        return this.descriptionID;
    }
    public String getTooltipID() {
        if(this.tooltipID == null)
            this.tooltipID = this.getDescriptionID() + ".tooltip";
        return this.tooltipID;
    }

    @Override
    protected Registry<Permission<?>> getRegistry() { return LCRegistries.Trader.PERMISSION; }

    public Component getPermissionName() { return Component.translatable(this.getDescriptionID()); }
    public Component getTooltip() { return Component.translatable(this.getTooltipID()); }

    public PermissionValue<T> createNew() { return new PermissionValue<>(this,this.defaultValue.get()); }

    @Override
    protected String getName() { return "Permission"; }
}