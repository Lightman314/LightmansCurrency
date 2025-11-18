package io.github.lightman314.lightmanscurrency.api.variants.item;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.List;

@MethodsReturnNonnullByDefault
public interface IVariantItem {

    default ResourceLocation getItemID()
    {
        if(this instanceof ItemLike item)
            return BuiltInRegistries.ITEM.getKey(item.asItem());
        else
            throw new IllegalStateException("IVariantItem must be applied to an Item class!");
    }

    default List<ResourceLocation> getValidVariants() { return ModelVariantDataManager.getPotentialVariants(this.getItemID()); }

    default int requiredModels() { return 0; }

    @Nullable
    default ResourceLocation getDefaultModel(int index) { if(this.requiredModels() > 0) throw new IllegalStateException("Variant Item requires custom model, but does not provide the default for that model!"); return null; }

}
