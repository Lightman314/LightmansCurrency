package io.github.lightman314.lightmanscurrency.api.variants;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.api.variants.block.VariantBlockWrapper;
import io.github.lightman314.lightmanscurrency.api.variants.item.VariantItemWrapper;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class VariantProvider<T,V> {

    private static final VariantProvider<Block,IVariantBlock> BLOCK_PROVIDER;
    private static final VariantProvider<Item, IVariantItem> ITEM_PROVIDER;

    private final List<Function<T,V>> providers = new ArrayList<>();
    private final List<Pair<Predicate<T>,Function<T,V>>> builders = new ArrayList<>();
    private void addProvider(Function<T,V> provider) {
        if(!this.resultCache.isEmpty() || !nullValues.isEmpty())
            LightmansCurrency.LogWarning("Variant Provider registered after results have already been cached!",new Throwable());
        this.providers.add(Objects.requireNonNull(provider));
    }
    private void addBuilder(Predicate<T> test,Function<T,V> builder) { this.builders.add(Pair.of(test,builder)); }
    private final Function<T,ResourceLocation> keyGetter;
    private final List<ResourceLocation> nullValues = new ArrayList<>();
    private final Map<ResourceLocation,V> resultCache = new HashMap<>();

    public VariantProvider(Function<T,ResourceLocation> keyGetter) { this.keyGetter = keyGetter; }
    private void setupBuilderSource()
    {
        this.providers.add((object) -> {
            for(var builderPair : new ArrayList<>(this.builders))
            {
                if(builderPair.getFirst().test(object))
                    return builderPair.getSecond().apply(object);
            }
            return null;
        });
    }

    static {
        BLOCK_PROVIDER = new VariantProvider<>(ForgeRegistries.BLOCKS::getKey);
        BLOCK_PROVIDER.addProvider(b -> { if(b instanceof IVariantBlock vb) return vb; return null;});
        BLOCK_PROVIDER.setupBuilderSource();

        ITEM_PROVIDER = new VariantProvider<>(ForgeRegistries.ITEMS::getKey);
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
        ITEM_PROVIDER.setupBuilderSource();
    }

    @Nullable
    public final V get(T value)
    {
        ResourceLocation key = this.keyGetter.apply(Objects.requireNonNull(value));
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

    public static void registerVariantBlock(VariantBlockWrapper variant) { BLOCK_PROVIDER.addBuilder(block -> block == variant.getBlock(),b -> variant); }

    public static void registerVariantBlockBuilder(Predicate<Block> test, Function<Block,IVariantBlock> builder) { BLOCK_PROVIDER.addBuilder(test,builder); }

    public static void registerVariantBlock(Function<Block,IVariantBlock> provider) { BLOCK_PROVIDER.addProvider(Objects.requireNonNull(provider)); }

    @SafeVarargs
    public static void registerBasicVariantItem(Supplier<Item>... items)
    {
        List<Supplier<Item>> list = ImmutableList.copyOf(items);
        registerBasicVariantItemBuilder(item -> list.stream().anyMatch(sup -> sup.get() == item));
    }
    public static void registerBasicVariantItem(Item... items)
    {
        List<Item> list = ImmutableList.copyOf(items);
        registerBasicVariantItemBuilder(list::contains);
    }
    public static void registerVariantItem(VariantItemWrapper variant) {
        ITEM_PROVIDER.addBuilder(item -> item == variant.getItem(),i -> variant);
    }
    public static void registerBasicVariantItemBuilder(Predicate<Item> test) { registerVariantItemBuilder(test,VariantItemWrapper::simple); }
    public static void registerVariantItemBuilder(Predicate<Item> test, Function<Item,IVariantItem> builder) { ITEM_PROVIDER.addBuilder(test,builder); }

    public static void registerVariantItem(Function<Item,IVariantItem> provider) { ITEM_PROVIDER.addProvider(Objects.requireNonNull(provider)); }

    @Nullable
    public static IVariantBlock getVariantBlock(Block block) { return BLOCK_PROVIDER.get(block); }
    @Nullable
    public static IVariantItem getVariantItem(ItemStack stack) { return getVariantItem(stack.getItem()); }
    @Nullable
    public static IVariantItem getVariantItem(Item item) { return ITEM_PROVIDER.get(item); }

}