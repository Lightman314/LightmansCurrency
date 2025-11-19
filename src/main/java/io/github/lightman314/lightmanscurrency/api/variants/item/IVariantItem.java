package io.github.lightman314.lightmanscurrency.api.variants.item;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

@MethodsReturnNonnullByDefault
public interface IVariantItem {

    default ResourceLocation getItemID()
    {
        if(this instanceof ItemLike item)
            return ForgeRegistries.ITEMS.getKey(item.asItem());
        else
            throw new IllegalStateException("IVariantItem must be applied to an Item class!");
    }

    default List<ResourceLocation> getValidVariants() { return ModelVariantDataManager.getPotentialVariants(this.getItemID()); }

    default int requiredModels() { return 0; }

    @Nullable
    default ResourceLocation getDefaultModel(int index) { if(this.requiredModels() > 0) throw new IllegalStateException("Variant Item requires custom model, but does not provide the default for that model!"); return null; }

    static void setItemVariant(ItemStack item, @Nullable ResourceLocation variantID)
    {
        CompoundTag tag = item.getOrCreateTag();
        if(variantID == null)
        {
            tag.remove("ModelVariant");
            if(tag.isEmpty())
                tag = null;
        }
        else
            tag.putString("ModelVariant",variantID.toString());
        item.setTag(tag);
    }

    @Nullable
    static ResourceLocation getItemVariant(ItemStack item)
    {
        CompoundTag tag = item.getTag();
        if(tag == null || !tag.contains("ModelVariant"))
            return null;
        return VersionUtil.parseResource(tag.getString("ModelVariant"));
    }

    static boolean isLocked(ItemStack item)
    {
        CompoundTag tag = item.getTag();
        return tag != null && tag.getBoolean("VariantLock");
    }

    static void setLocked(ItemStack item,boolean locked)
    {
        CompoundTag tag = item.getOrCreateTag();
        if(locked)
            tag.putBoolean("VariantLock",true);
        else
        {
            tag.remove("VariantLock");
            if(tag.isEmpty())
                tag = null;
        }
        item.setTag(tag);
    }

}