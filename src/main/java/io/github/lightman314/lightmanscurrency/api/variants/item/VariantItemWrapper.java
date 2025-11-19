package io.github.lightman314.lightmanscurrency.api.variants.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantItemWrapper implements IVariantItem {

    private final Supplier<Item> item;
    protected VariantItemWrapper(Supplier<Item> item) { this.item = item; }

    public static VariantItemWrapper simple(Item item) { return simple(() -> item); }
    public static VariantItemWrapper simple(Supplier<Item> item) { return new VariantItemWrapper(item); }

    public static VariantItemWrapper withModels(Item item, int modelCount, Function<Integer,ResourceLocation> defaultModelSource) { return withModels(() -> item,modelCount,defaultModelSource); }
    public static VariantItemWrapper withModels(Supplier<Item> item, int modelCount, Function<Integer,ResourceLocation> defaultModelSource) { return new WithModels(item,modelCount,defaultModelSource); }

    public Item getItem() { return this.item.get(); }
    @Override
    public ResourceLocation getItemID() { return ForgeRegistries.ITEMS.getKey(this.getItem()); }

    private static class WithModels extends VariantItemWrapper
    {
        private final int modelCount;
        private final Function<Integer,ResourceLocation> defaultModelSource;
        protected WithModels(Supplier<Item> item, int modelCount, Function<Integer,ResourceLocation> defaultModelSource) {
            super(item);
            this.modelCount = modelCount;
            if(this.modelCount <= 0)
                throw new IllegalArgumentException("Cannot make an Item Variant \"with models\" with a model count of " + this.modelCount + "!");
            this.defaultModelSource = defaultModelSource;
        }

        @Override
        public final int requiredModels() { return this.modelCount; }
        @Nullable
        @Override
        public ResourceLocation getDefaultModel(int index) {
            if(index < 0 || index >= this.modelCount)
                return null;
            return Objects.requireNonNull(this.defaultModelSource.apply(index));
        }
    }

}