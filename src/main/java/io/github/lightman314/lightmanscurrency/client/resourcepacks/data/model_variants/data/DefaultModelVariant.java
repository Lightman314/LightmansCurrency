package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DefaultModelVariant extends ModelVariant {


    private final IVariantBlock block;
    private DefaultModelVariant(IVariantBlock block) { this.block = block; }

    public static ModelVariant of(IVariantBlock block) { return new DefaultModelVariant(block); }

    @Override
    public List<ResourceLocation> getTargets() { return List.of(this.block.getBlockID()); }

    @Override
    public MutableComponent getName() { return LCText.BLOCK_VARIANT_DEFAULT.get(); }

    @Override
    @Nullable
    public ItemStack getItemIcon() {
        if(this.block instanceof ItemLike item)
            return new ItemStack(item);
        return null;
    }

    @Override
    public JsonObject write() { throw new IllegalStateException("Cannot save the Default Variant to file!"); }

    @Override
    public void validate(@Nullable Map<ResourceLocation, ModelVariant> otherVariants, ResourceLocation id) { }

}
