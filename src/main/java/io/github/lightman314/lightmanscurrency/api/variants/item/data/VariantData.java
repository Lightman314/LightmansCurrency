package io.github.lightman314.lightmanscurrency.api.variants.item.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.TooltipInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record VariantData(ResourceLocation variant) implements TooltipProvider {

    public static final VariantData NULL = new VariantData(null);

    public static final Codec<VariantData> CODEC = ResourceLocation.CODEC.xmap(VariantData::new,VariantData::variant);
    public static final StreamCodec<ByteBuf,VariantData> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(VariantData::new,VariantData::variant);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        ModelVariant variant = ModelVariantDataManager.getVariant(this.variant);
        if(variant != null)
        {
            adder.accept(LCText.TOOLTIP_MODEL_VARIANT_NAME.get(variant.getName().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
            if(variant.has(VariantProperties.TOOLTIP_INFO))
            {
                TooltipInfo extraTooltip = variant.get(VariantProperties.TOOLTIP_INFO);
                if(extraTooltip.drawOnItem)
                {
                    for(Component l : extraTooltip.getTooltip())
                        adder.accept(l);
                }
            }
        }
        if(flag.isAdvanced())
            adder.accept(LCText.TOOLTIP_MODEL_VARIANT_ID.get(this.variant).withStyle(ChatFormatting.DARK_GRAY));
    }

}
