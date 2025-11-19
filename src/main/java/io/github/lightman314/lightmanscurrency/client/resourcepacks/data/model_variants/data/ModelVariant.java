package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantPropertyWithDefault;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariant {

    public static final Comparator<Pair<ResourceLocation,ModelVariant>> COMPARATOR = new VariantSorter();

    private final List<ResourceLocation> targets;
    public List<ResourceLocation> getTargets() { return this.targets; }
    public boolean isValidTarget(IVariantItem item) { return this.targets.contains(item.getItemID()); }
    public boolean isValidTarget(Item item) { return this.targets.contains(ForgeRegistries.ITEMS.getKey(item)) && VariantProvider.getVariantItem(item) != null; }
    public boolean isValidTarget(ItemStack item) { return this.isValidTarget(item.getItem()); }
    public boolean isValidTarget(IVariantBlock block) { return this.targets.contains(block.getBlockID()); }
    public boolean isValidTarget(Block block) { return this.targets.contains(ForgeRegistries.BLOCKS.getKey(block)) && VariantProvider.getVariantBlock(block) != null; }

    @Nullable
    private final Component name;
    public MutableComponent getName() { return this.name == null ? LCText.BLOCK_VARIANT_UNNAMED.get() : this.name.copy(); }

    @Nullable
    private final ResourceLocation item;
    @Nullable
    public ResourceLocation getItemModel() { return this.item; }
    @Nullable
    public ItemStack getItemIcon() { return null; }

    private final List<ResourceLocation> blockModels;
    public boolean hasBlockModels() { return !this.getBlockModels().isEmpty(); }
    public List<ResourceLocation> getBlockModels() { return this.blockModels; }

    private final Map<String,ResourceLocation> textureOverrides;
    public boolean hasTextureOverrides() { return !this.getTextureOverrides().isEmpty(); }
    public Map<String, ResourceLocation> getTextureOverrides() { return this.textureOverrides; }

    private final Map<ResourceLocation,Object> properties;

    private final boolean itemVariant;
    public boolean isItemVariant() { return this.itemVariant; }

    //Only exists for the "DefaultVariant" class
    protected ModelVariant() { this(new ArrayList<>(),null,null,new ArrayList<>(),new HashMap<>(),new HashMap<>(),true); }
    public ModelVariant(List<ResourceLocation> targets, @Nullable Component name, @Nullable ResourceLocation item, List<ResourceLocation> blockModels, Map<String,ResourceLocation> textureOverrides, Map<ResourceLocation,Object> properties, boolean itemVariant)
    {
        this.targets = ImmutableList.copyOf(targets);
        this.name = name;
        this.item = item;
        this.blockModels = ImmutableList.copyOf(blockModels);
        this.textureOverrides = ImmutableMap.copyOf(textureOverrides);
        this.properties = ImmutableMap.copyOf(properties);
        this.itemVariant = itemVariant;
    }

    public boolean has(VariantProperty<?> property) { return this.properties.containsKey(property.getID()); }

    @Nullable
    public <T> T get(VariantProperty<T> property) {
        try { return (T)this.properties.get(property.getID());
        } catch (ClassCastException e) { return null; }
    }

    public <T> T getOrDefault(VariantProperty<T> property,T defaultValue)
    {
        T result = this.get(property);
        return result == null ? defaultValue : result;
    }

    public <T> T getOrDefault(VariantPropertyWithDefault<T> property) { return this.getOrDefault(property,property.getMissingDefault()); }

    private static class VariantSorter implements Comparator<Pair<ResourceLocation,ModelVariant>>
    {
        @Override
        public int compare(Pair<ResourceLocation, ModelVariant> a, Pair<ResourceLocation, ModelVariant> b) {
            ResourceLocation idA = a.getFirst();
            ResourceLocation idB = b.getFirst();
            if(idA == null)
                return -1;
            if(idB == null)
                return 1;
            ModelVariant varA = a.getSecond();
            ModelVariant varB = b.getSecond();
            String nameA = varA.getName().getString();
            String nameB = varB.getName().getString();
            int nameSort = nameA.compareToIgnoreCase(nameB);
            if(nameSort == 0)
                return idA.compareNamespaced(idB);
            return nameSort;
        }
    }

}