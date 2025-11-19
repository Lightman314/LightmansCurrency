package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DefaultModelVariant extends ModelVariant {

    private final Supplier<ResourceLocation> id;
    private DefaultModelVariant(Supplier<ResourceLocation> id) { this.id = id; }

    public static DefaultModelVariant of(IVariantBlock block) { return new DefaultModelVariant(block::getBlockID); }
    public static DefaultModelVariant of(IVariantItem item) { return new DefaultModelVariant(item::getItemID); }

    @Override
    public List<ResourceLocation> getTargets() { return List.of(this.id.get()); }

    @Override
    public MutableComponent getName() { return LCText.BLOCK_VARIANT_DEFAULT.get(); }

    @Override
    @Nullable
    public ItemStack getItemIcon() { return new ItemStack(ForgeRegistries.ITEMS.getValue(this.id.get())); }

}