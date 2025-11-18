package io.github.lightman314.lightmanscurrency.api.variants;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.api.variants.block.VariantBlockWrapper;
import io.github.lightman314.lightmanscurrency.api.variants.item.VariantItemWrapper;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class VariantProvider<T,V> {

    private static final VariantProvider<Block,IVariantBlock> BLOCK_PROVIDER;
    private static final VariantProvider<Item, IVariantItem> ITEM_PROVIDER;

    private final List<Function<T,V>> providers = new ArrayList<>();
    private void addProvider(Function<T,V> provider) {
        if(!this.resultCache.isEmpty() || !nullValues.isEmpty())
            LightmansCurrency.LogWarning("Variant Provider registered after results have already been cached!",new Throwable());
        this.providers.add(Objects.requireNonNull(provider));
    }
    private final Function<T,ResourceLocation> keyGetter;
    private final List<ResourceLocation> nullValues = new ArrayList<>();
    private final Map<ResourceLocation,V> resultCache = new HashMap<>();

    public VariantProvider(Function<T,ResourceLocation> keyGetter) { this.keyGetter = keyGetter; }

    static {
        BLOCK_PROVIDER = new VariantProvider<>(BuiltInRegistries.BLOCK::getKey);
        BLOCK_PROVIDER.addProvider(b -> { if(b instanceof IVariantBlock vb) return vb; return null;});

        ITEM_PROVIDER = new VariantProvider<>(BuiltInRegistries.ITEM::getKey);
        ITEM_PROVIDER.addProvider(i -> {
            if(i instanceof IVariantItem vi)
                return vi;
            if(i instanceof BlockItem bi)
            {
                IVariantBlock block = getVariantBlock(bi.getBlock());
                if(block != null)
                    return VariantItemWrapper.simple(i);
            }
            return null;
        });
    }

    @Nullable
    public final V get(T value)
    {
        ResourceLocation key = this.keyGetter.apply(value);
        if(this.resultCache.containsKey(key))
            return this.resultCache.get(key);
        if(this.nullValues.contains(key))
            return null;
        for(var provider : this.providers)
        {
            V result = provider.apply(value);
            if(result != null)
            {
                this.resultCache.put(key,result);
                return result;
            }
        }
        this.nullValues.add(key);
        return null;
    }

    public static void registerVariantBlock(VariantBlockWrapper variant)
    {
        BLOCK_PROVIDER.addProvider(block -> {
            if(block == variant.getBlock())
                return variant;
            return null;
        });
    }

    public static void registerVariantBlock(Function<Block,IVariantBlock> provider) { BLOCK_PROVIDER.addProvider(Objects.requireNonNull(provider)); }

    @SafeVarargs
    public static void registerBasicVariantItem(Supplier<Item>... items)
    {
        for(var sup : items)
            registerVariantItem(VariantItemWrapper.simple(sup));
    }
    public static void registerBasicVariantItem(Item... items)
    {
        for(Item i : items)
            registerVariantItem(VariantItemWrapper.simple(i));
    }
    public static void registerVariantItem(VariantItemWrapper variant) {
        ITEM_PROVIDER.addProvider(item -> {
            if(item == variant.getItem())
                return variant;
            return null;
        });
    }

    public static void registerVariantItem(Function<Item,IVariantItem> provider) { ITEM_PROVIDER.addProvider(Objects.requireNonNull(provider)); }

    @Nullable
    public static IVariantBlock getVariantBlock(Block block) { return BLOCK_PROVIDER.get(block); }
    @Nullable
    public static IVariantItem getVariantItem(ItemStack stack) { return getVariantItem(stack.getItem()); }
    @Nullable
    public static IVariantItem getVariantItem(Item item) { return ITEM_PROVIDER.get(item); }

}
