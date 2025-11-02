package io.github.lightman314.lightmanscurrency.api.notifications;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class NotificationCategoryType<T extends NotificationCategory> {

    public final ResourceLocation type;
    private final NonNullFunction<CompoundTag,T> generator;

    public NotificationCategoryType(ResourceLocation type, NonNullFunction<CompoundTag,T> generator) { this.type = type; this.generator = generator; }

    public T load(CompoundTag tag)
    {
        return this.generator.apply(tag);
    }

    @Override
    public String toString() { return this.type.toString(); }

}
