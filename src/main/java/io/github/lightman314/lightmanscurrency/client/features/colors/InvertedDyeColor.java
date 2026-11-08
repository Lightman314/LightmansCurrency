package io.github.lightman314.lightmanscurrency.client.features.colors;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record InvertedDyeColor(int defaultColor) implements ItemTintSource {

    public static InvertedDyeColor createDefault() { return new InvertedDyeColor(0xFF000000); }

    public static final MapCodec<InvertedDyeColor> MAP_CODEC = ExtraCodecs.STRING_RGB_COLOR.fieldOf("default").xmap(InvertedDyeColor::new,InvertedDyeColor::defaultColor);

    @Override
    public int calculate(ItemStack stack,@Nullable ClientLevel level,@Nullable LivingEntity owner) {
        if(stack.has(DataComponents.DYED_COLOR))
            return ARGB.opaque(0XFFFFFFFF - stack.get(DataComponents.DYED_COLOR).rgb());
        return ARGB.opaque(this.defaultColor);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }

}
