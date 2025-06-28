package io.github.lightman314.lightmanscurrency.integration.create.attributes;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class VariantAttributeType implements ItemAttributeType {

    private static final ResourceLocation NULL_ID = VersionUtil.modResource("null","null");
    private static final VariantAttribute NO_VARIANT_ATTRIBUTE = new VariantAttribute(NULL_ID);

    @Override
    @Nonnull
    public ItemAttribute createAttribute() { return new VariantAttribute(VersionUtil.lcResource("null")); }

    @Override
    public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {

        if(stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof IVariantBlock)
        {
            ResourceLocation variantID = IVariantBlock.getItemVariant(stack);
            if(variantID != null)
                return List.of(new VariantAttribute(variantID));
            return List.of(NO_VARIANT_ATTRIBUTE);
        }
        return List.of();
    }

    public static class VariantAttribute implements ItemAttribute
    {
        private static VariantAttribute create(ResourceLocation variantID)
        {
            if(variantID == NULL_ID)
                return NO_VARIANT_ATTRIBUTE;
            return new VariantAttribute(variantID);
        }
        private ResourceLocation variantID;
        private VariantAttribute(ResourceLocation variantID) { this.variantID = variantID; }
        private boolean isNullType() { return this.variantID == NULL_ID; }
        @Override
        public boolean appliesTo(ItemStack stack, Level world) {
            ResourceLocation variant = IVariantBlock.getItemVariant(stack);
            if(this.isNullType())
                return variant == null;
            return Objects.equals(this.variantID,variant);
        }
        @Override
        public ItemAttributeType getType() { return LCItemAttributes.VARIANT_ATTRIBUTE.get(); }

        @Override
        public void save(CompoundTag tag) { tag.putString("value",this.variantID.toString()); }

        @Override
        public void load(CompoundTag tag) { this.variantID = VersionUtil.parseResource(tag.getString("value")); }

        @Override
        public String getTranslationKey() { return this.isNullType() ? "lightmanscurrency.model_variant.null" : "lightmanscurrency.model_variant"; }
        @Override
        public Object[] getTranslationParameters() { return this.isNullType() ? ItemAttribute.super.getTranslationParameters() : new Object[] { this.variantID.toString() }; }

    }

}