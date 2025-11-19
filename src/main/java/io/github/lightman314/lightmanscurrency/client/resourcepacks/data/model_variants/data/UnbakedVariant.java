package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.IIndependentProperty;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantPropertyWithDefault;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import io.github.lightman314.lightmanscurrency.util.WildcardTargetSelector;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class UnbakedVariant {

    private boolean completelyInvalid = false;
    public boolean shouldRemove() { return this.completelyInvalid || (this.parentVariant != null && this.parentVariant.shouldRemove()); }
    private boolean invalid = false;
    public boolean isValid() { return !this.isInvalid(); }
    public boolean isInvalid() { return this.shouldRemove() || this.invalid || this.dummy; }

    private final boolean itemVariant;
    public boolean isItemVariant() {
        if(!this.itemVariant && this.parentVariant != null)
            return this.parentVariant.isItemVariant();
        return this.itemVariant;
    }
    private final boolean dummy;

    @Nullable
    private final ResourceLocation parent;
    @Nullable
    private UnbakedVariant parentVariant = null;

    private final List<String> targetSelectors;
    public List<String> getTargetSelectors()
    {
        List<String> result = new ArrayList<>(this.targetSelectors);
        if(this.parentVariant != null)
            result.addAll(this.parentVariant.getTargetSelectors());
        return result;
    }
    private final List<ResourceLocation> targets;
    public List<ResourceLocation> getTargets() {
        List<ResourceLocation> result = new ArrayList<>(this.targets);
        if(this.parentVariant != null)
            result.addAll(this.parentVariant.getTargets());
        return result;
    }

    @Nullable
    private final Component name;
    public Component getName() {
        if(this.name == null && this.parentVariant != null)
            return this.parentVariant.getName();
        return this.name;
    }

    @Nullable
    private final ResourceLocation item;
    @Nullable
    public ResourceLocation getItemModel() {
        if(this.item == null && this.parent != null)
            return this.parentVariant == null ? null : this.parentVariant.getItemModel();
        return this.item;
    }
    @Nullable
    public ItemStack getItemIcon() { return null; }

    private final List<ResourceLocation> models;
    public boolean hasModels() { return !this.getModels().isEmpty(); }
    public List<ResourceLocation> getModels() {
        if(this.models.isEmpty() && this.parent != null)
            return parentVariant == null ? ImmutableList.of() : parentVariant.getModels();
        return this.models;
    }

    private final Map<String,ResourceLocation> textureOverrides;
    public boolean hasTextureOverrides() { return !this.getTextureOverrides().isEmpty(); }
    public Map<String, ResourceLocation> getTextureOverrides() {
        if(this.parentVariant != null)
        {
            Map<String,ResourceLocation> combinedMap = new HashMap<>(this.parentVariant.getTextureOverrides());
            combinedMap.putAll(this.textureOverrides);
            return ImmutableMap.copyOf(combinedMap);
        }
        return this.textureOverrides;
    }

    private final Map<ResourceLocation,Object> properties;

    private UnbakedVariant(@Nullable ResourceLocation parent, List<ResourceLocation> targets, List<String> selectorTargets, @Nullable Component name, @Nullable ResourceLocation item, List<ResourceLocation> models, Map<String,ResourceLocation> textureOverrides, Map<ResourceLocation,Object> properties, boolean itemVariant, boolean dummy)
    {
        this.parent = parent;
        this.targets = ImmutableList.copyOf(targets);
        this.targetSelectors = ImmutableList.copyOf(selectorTargets);
        this.name = name;
        this.item = item;
        this.models = ImmutableList.copyOf(models);
        this.textureOverrides = ImmutableMap.copyOf(textureOverrides);
        this.properties = ImmutableMap.copyOf(properties);
        this.itemVariant = itemVariant;
        this.dummy = dummy;
    }

    public boolean has(VariantProperty<?> property) {
        if(this.parentVariant != null && this.parentVariant.has(property))
            return true;
        return this.properties.containsKey(property.getID());
    }

    @Nullable
    public <T> T get(VariantProperty<T> property) {
        try {
            if(!this.properties.containsKey(property.getID()) && this.parentVariant != null)
                return this.parentVariant.get(property);
            return (T)this.properties.get(property.getID());
        } catch (ClassCastException e) { return null; }
    }

    public <T> T getOrDefault(VariantProperty<T> property,T defaultValue)
    {
        T result = this.get(property);
        return result == null ? defaultValue : result;
    }

    public <T> T getOrDefault(VariantPropertyWithDefault<T> property) { return this.getOrDefault(property,property.getMissingDefault()); }

    public JsonObject write()
    {
        JsonObject json = new JsonObject();
        if(this.parent != null)
            json.addProperty("parent",this.parent.toString());
        if(this.targets.size() == 1)
            json.addProperty("target",this.targets.get(0).toString());
        else if(!this.targets.isEmpty())
        {
            JsonArray array = new JsonArray();
            for(ResourceLocation t : this.targets)
                array.add(t.toString());
            json.add("target",array);
        }
        if(this.targetSelectors.size() == 1)
            json.addProperty("targetSelector",this.targetSelectors.get(0));
        else if(!this.targetSelectors.isEmpty())
        {
            JsonArray array = new JsonArray();
            for(String t : this.targetSelectors)
                array.add(t);
            json.add("targetSelector",array);
        }
        if(this.name != null)
            json.add("name", Component.Serializer.toJsonTree(this.name));
        if(this.item != null)
            json.addProperty("item",this.item.toString());
        if(!this.models.isEmpty())
        {
            JsonArray models = new JsonArray();
            for(ResourceLocation model : this.models)
                models.add(model.toString());
            json.add("models",models);
        }
        if(!this.textureOverrides.isEmpty())
        {
            JsonObject textures = new JsonObject();
            this.textureOverrides.forEach((key,texture) -> textures.addProperty(key,texture.toString()));
            json.add("textures",textures);
        }
        this.properties.forEach((propID,value) -> {
            try {
                VariantProperty<?> property = VariantProperty.getProperty(propID);
                JsonElement element = property.write(value);
                ResourceLocation id = property.getID();
                String field;
                if(id.getNamespace().equals(LightmansCurrency.MODID))
                    field = id.getPath();
                else
                    field = id.toString();
                json.add(field,element);
            }catch (Exception e) { LightmansCurrency.LogError("Error writing Variant Property",e); }
        });
        if(this.itemVariant)
            json.addProperty("itemVariant",true);
        if(this.dummy)
            json.addProperty("dummy",true);
        return json;
    }

    public static UnbakedVariant parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        ResourceLocation parent = null;
        if(json.has("parent"))
            parent = VersionUtil.parseResource(GsonHelper.getAsString(json,"parent"));
        List<ResourceLocation> targets = new ArrayList<>();
        if(json.has("target"))
        {
            JsonElement targetElement = json.get("target");
            if(targetElement.isJsonPrimitive())
            {
                addTarget(targets,VersionUtil.parseResource(GsonHelper.getAsString(json,"target")));
            }
            else
            {
                JsonArray array = GsonHelper.getAsJsonArray(json,"target");
                for(int i = 0; i < array.size(); ++i)
                    addTarget(targets,VersionUtil.parseResource(GsonHelper.convertToString(array.get(i),"target[" + i + "]")));
            }
        }
        List<String> targetSelectors = new ArrayList<>();
        if(json.has("targetSelector"))
        {

            JsonElement targetElement = json.get("targetSelector");
            if(targetElement.isJsonPrimitive())
            {
                targetSelectors.add(GsonHelper.getAsString(json,"targetSelector"));
            }
            else
            {
                JsonArray array = GsonHelper.getAsJsonArray(json,"targetSelector");
                for(int i = 0; i < array.size(); ++i)
                    targetSelectors.add(GsonHelper.convertToString(array.get(i),"targetSelector[" + i + "]"));
            }
        }

        Component name = null;
        if(json.has("name"))
            name = Component.Serializer.fromJson(json.get("name"));

        ResourceLocation item = null;
        if(json.has("item"))
            item = VersionUtil.parseResource(GsonHelper.getAsString(json,"item"));
        else if(json.has("icon"))
            item = VersionUtil.parseResource(GsonHelper.getAsString(json,"icon"));


        List<ResourceLocation> models = new ArrayList<>();
        if(json.has("models"))
        {
            JsonArray modelList = GsonHelper.getAsJsonArray(json,"models");
            for(int i = 0; i < modelList.size(); ++i)
                models.add(VersionUtil.parseResource(GsonHelper.convertToString(modelList.get(i),"models[" + i + "]")));
        }
        Map<String,ResourceLocation> textureOverrides = new HashMap<>();
        if(json.has("textures"))
        {
            JsonObject textures = GsonHelper.getAsJsonObject(json,"textures");
            for(Map.Entry<String,JsonElement> entry : textures.entrySet())
            {
                ResourceLocation t = VersionUtil.parseResource(GsonHelper.convertToString(entry.getValue(),entry.getKey()));
                textureOverrides.put(entry.getKey(),t);
            }
        }
        Map<ResourceLocation,Object> properties = new HashMap<>();
        VariantProperty.forEach((id,property) -> {
            if(json.has(id.toString()))
                properties.put(id,property.parse(json.get(id.toString())));
            else if(id.getNamespace().equals(LightmansCurrency.MODID) && json.has(id.getPath()))
                properties.put(id,property.parse(json.get(id.getPath())));
        });
        if(targets.isEmpty() && name == null && item == null && models.isEmpty() && textureOverrides.isEmpty() && properties.isEmpty())
        {
            if(parent == null)
                throw new JsonSyntaxException("Model Variant must have something defined!");
            else
                throw new JsonSyntaxException("Model Variant must have something other than the parent defined!");
        }
        boolean itemVariant = GsonHelper.getAsBoolean(json,"itemVariant",false);
        boolean dummy = GsonHelper.getAsBoolean(json,"dummy",false);
        return new UnbakedVariant(parent,targets,targetSelectors,name,item,models,textureOverrides,properties,itemVariant,dummy);
    }

    public void validate(@Nullable Map<ResourceLocation, UnbakedVariant> otherVariants, ResourceLocation id) {
        if(this.parent != null)
        {
            if(otherVariants == null)
                return;
            LoopData loop = new LoopData(id,this);
            if(otherVariants.containsKey(this.parent))
            {
                this.parentVariant = otherVariants.get(this.parent);
                if(this.parentVariant.checkForLoop(otherVariants,this.parent,loop))
                {
                    LightmansCurrency.LogWarning("Detected infinite loop in " + id);
                    this.completelyInvalid = true;
                    return;
                }
                //Check if valid when combined
                this.invalid = invalidWhenCombined(loop,id);
            }
            else
            {
                //Parent is missing, so don't bother
                this.completelyInvalid = true;
            }
        }
        else if(otherVariants != null)
        {
            this.invalid = invalidWhenCombined(new LoopData(id,this),id);
        }
    }

    private static boolean invalidWhenCombined(LoopData loop) { return invalidWhenCombined(loop,null); }
    private static boolean invalidWhenCombined(LoopData loop,@Nullable ResourceLocation id)
    {

        //Check if we have valid targets
        boolean hasTarget = false;
        List<ResourceLocation> targets = new ArrayList<>();
        for(UnbakedVariant v : loop.variants)
        {
            if (!v.targets.isEmpty()) {
                hasTarget = true;
                targets = v.targets;
                break;
            }
        }
        //Missing targets
        if(!hasTarget)
        {
            if(id != null)
                LightmansCurrency.LogDebug(id + " has no valid targets!");
            return true;
        }

        boolean hasTextures = false;
        for(UnbakedVariant v : loop.variants)
        {
            if(!v.textureOverrides.isEmpty())
            {
                hasTextures = true;
                break;
            }
        }

        //Check if we have a valid item model
        boolean hasItem = false;
        for(UnbakedVariant v : loop.variants)
        {
            if(v.item != null) {
                hasItem = true;
                break;
            }
        }

        //Chekc if we're an item variant
        boolean itemVariant = loop.variants.stream().anyMatch(v -> v.itemVariant);

        //Check for valid models
        boolean hasModels = false;
        for(UnbakedVariant v : loop.variants)
        {
            if(!v.models.isEmpty()) {
                hasModels = true;
                if(!itemVariant)
                {
                    for(ResourceLocation t : targets)
                    {
                        Block block = ForgeRegistries.BLOCKS.getValue(t);
                        IVariantBlock vb = VariantProvider.getVariantBlock(block);
                        if(vb != null)
                        {
                            if(vb.requiredModels() != v.models.size())
                            {
                                if(id != null)
                                    LightmansCurrency.LogDebug(id + " does not have the same model count as " + t);
                                return true;
                            }
                        }
                        else
                        {
                            if(id != null)
                                LightmansCurrency.LogDebug(id + " targets an invalid variant block (" + t + ")");
                            return true;
                        }
                    }
                }
                else
                {
                    for(ResourceLocation t : targets)
                    {
                        Item item = ForgeRegistries.ITEMS.getValue(t);
                        IVariantItem vi = VariantProvider.getVariantItem(item);
                        if(vi != null)
                        {
                            if(vi.requiredModels() != v.models.size())
                            {
                                if(id != null)
                                    LightmansCurrency.LogDebug(id + " does not have the same model count as " + t);
                                return true;
                            }
                        }
                        else
                        {
                            if(id != null)
                                LightmansCurrency.LogDebug(id + " targets an invalid variant item (" + t + ")");
                            return true;
                        }
                    }
                }
            }
        }

        if(!hasModels && itemVariant)
        {
            //Check item model count and confirm that we are indeed not supposed to have models
            for(ResourceLocation t : targets)
            {
                Item item = ForgeRegistries.ITEMS.getValue(t);
                IVariantItem vi = VariantProvider.getVariantItem(item);
                if(vi != null)
                {
                    if(vi.requiredModels() > 0)
                    {
                        if(id != null)
                            LightmansCurrency.LogDebug(id + " does not have the same model count as " + t);
                        return true;
                    }
                }
                else
                {
                    if(id != null)
                        LightmansCurrency.LogDebug(id + " targets an invalid variant item (" + t + ")");
                    return true;
                }
            }
            //If no fail state located, then we have all the models we need, and we can flag "hasModels" as true
            hasModels = hasItem;
        }

        //Check for indepdent properties
        boolean hasIndependentProperty = false;
        for(UnbakedVariant v : loop.variants)
        {
            if(hasIndependentProperty)
                break;
            for(Object value : v.properties.values())
            {
                if(value instanceof IIndependentProperty)
                {
                    hasIndependentProperty = true;
                    break;
                }
            }
        }

        //If models are present, a custom item must also be present
        //If no textures are present, a model/item must be present
        if(hasModels != hasItem && !itemVariant)
        {
            if(id != null)
            {
                if(hasModels)
                    LightmansCurrency.LogDebug(id + " has custom block models defined, but no custom item model");
                else
                    LightmansCurrency.LogDebug(id + " has a custom item model defined, but no custom block models");
            }
            return true;
        }
        if(!hasTextures && !hasModels && !hasIndependentProperty)
        {
            if(id != null)
                LightmansCurrency.LogDebug(id + " does not have any custom models or custom textures defined");
            return true;
        }
        return false;
    }

    private boolean checkForLoop(Map<ResourceLocation,UnbakedVariant> existingVariants, ResourceLocation myID, LoopData loop)
    {
        if(this.completelyInvalid)
            return true;
        if(loop.checkAndAdd(myID,this))
            return true;
        if(this.parent != null)
        {
            if(!existingVariants.containsKey(this.parent))
            {
                this.completelyInvalid = true;
                return true;
            }
        }
        return false;
    }

    public ModelVariant bake(ResourceLocation id)
    {
        if(this.isInvalid())
            throw new IllegalStateException("Cannot bake an invalid UnbakedVariant");
        //LightmansCurrency.LogDebug("Baking Model Variant: " + id);
        //Assemble targets from selectors
        List<ResourceLocation> targets = this.getTargets();
        boolean itemVariant = this.isItemVariant();
        List<WildcardTargetSelector> selectors = this.targetSelectors.stream().map(WildcardTargetSelector::parse).toList();
        if(this.isItemVariant())
            addItemTargets(targets,selectors);
        else
            addBlockTargets(targets,selectors,this.getModels().size());
        Map<ResourceLocation,Object> properties = new HashMap<>();
        mergeProperties(this,properties);
        return new ModelVariant(targets,this.getName(),this.getItemModel(),this.getModels(),this.getTextureOverrides(),properties,itemVariant);
    }

    private static void mergeProperties(UnbakedVariant variant,Map<ResourceLocation,Object> data)
    {
        //Put parent properties in first, as children should override their parents data
        if(variant.parentVariant != null)
            mergeProperties(variant.parentVariant,data);
        data.putAll(variant.properties);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        @Nullable
        private ResourceLocation parent;
        private final List<ResourceLocation> targets = new ArrayList<>();
        private final List<String> selectorTargets = new ArrayList<>();
        @Nullable
        private Component name;
        private ResourceLocation item = null;
        private final List<ResourceLocation> models = new ArrayList<>();
        private final Map<String,ResourceLocation> textureOverrides = new HashMap<>();
        private final Map<ResourceLocation,Object> properties = new HashMap<>();
        private boolean itemVariant = false;
        private boolean dummy = false;

        private Builder() { VariantProperty.confirmRegistration(); }

        public Builder withParent(ResourceLocation parent) { this.parent = parent; return this; }

        //We can hopefully assume that and Block has the same ID as its item, so it should be safe to use the same method for both
        public Builder withTarget(Supplier<? extends ItemLike> target) { return this.withTarget(target.get().asItem()); }
        public Builder withTarget(Item item) { return this.withTarget(ForgeRegistries.ITEMS.getKey(item)); }
        public Builder withTarget(Block block) { return this.withTarget(ForgeRegistries.BLOCKS.getKey(block)); }
        public Builder withTarget(ResourceLocation target) { if(!this.targets.contains(target)) this.targets.add(target); return this; }
        public Builder withSelectorTarget(String selectorTarget) { if(!this.selectorTargets.contains(selectorTarget)) this.selectorTargets.add(selectorTarget); return this; }

        public Builder withName(Component name) { this.name = name; return this; }

        public Builder withItem(ResourceLocation item) { this.item = item; return this; }

        public Builder withModel(ResourceLocation... model) { this.models.addAll(Lists.newArrayList(model)); return this; }

        public Builder withTexture(String textureKey, ResourceLocation texture) { this.textureOverrides.put(textureKey,texture); return this; }

        public <T> Builder withProperty(VariantProperty<T> property, T value) { this.properties.put(property.getID(),value); return this; }

        public <T> Builder withProperty(VariantPropertyWithDefault<T> property) { this.properties.put(property.getID(),property.getBuilderDefault()); return this; }

        public Builder asItemVariant() { this.itemVariant = true; return this; }
        public Builder asDummy() { this.dummy = true; return this; }

        public UnbakedVariant build() { return new UnbakedVariant(this.parent,this.targets,this.selectorTargets,this.name,this.item,this.models,this.textureOverrides,this.properties,this.itemVariant,this.dummy); }

    }

    private record LoopData(List<ResourceLocation> ids, List<UnbakedVariant> variants)  {
        LoopData(ResourceLocation firstID,UnbakedVariant firstVariant) {
            this(Lists.newArrayList(firstID),Lists.newArrayList(firstVariant));
        }
        <T extends Throwable> boolean checkAndAdd(ResourceLocation id, UnbakedVariant variant)
        {
            if(this.ids.contains(id))
                return true;
            this.ids.add(id);
            this.variants.add(variant);
            return false;
        }
    }

    private static void addItemTargets(List<ResourceLocation> targets, List<WildcardTargetSelector> targetSelectors)
    {
        for(Item item : ForgeRegistries.ITEMS)
        {
            IVariantItem variant = VariantProvider.getVariantItem(item);
            if(variant != null)
            {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if(testSelectors(targetSelectors,id))
                    addTarget(targets,id);
            }
        }
    }

    private static void addBlockTargets(List<ResourceLocation> targets, List<WildcardTargetSelector> targetSelectors, int modelCount)
    {
        for(Block block : ForgeRegistries.BLOCKS)
        {
            IVariantBlock variant = VariantProvider.getVariantBlock(block);
            if(variant != null && variant.requiredModels() == modelCount)
            {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
                if(testSelectors(targetSelectors,id))
                    addTarget(targets,id);
            }
        }
    }

    private static boolean testSelectors(List<WildcardTargetSelector> targetSelectors,ResourceLocation id)
    {
        String idString = id.toString();
        return targetSelectors.stream().anyMatch(s -> s.matches(idString));
    }

    private static void addTargets(List<ResourceLocation> targets, List<ResourceLocation> newTargets)
    {
        for(ResourceLocation newTarget : newTargets)
            addTarget(targets,newTarget);
    }

    private static void addTarget(List<ResourceLocation> targets, ResourceLocation target)
    {
        if(targets.contains(target))
            return;
        targets.add(target);
    }

    private static boolean matchesSelectors(ResourceLocation id, List<WildcardTargetSelector> targetSelectors)
    {
        String idString = id.toString();
        return targetSelectors.stream().anyMatch(s -> s.matches(idString));
    }

    private static class VariantSorter implements Comparator<Pair<ResourceLocation,UnbakedVariant>>
    {
        @Override
        public int compare(Pair<ResourceLocation, UnbakedVariant> a, Pair<ResourceLocation, UnbakedVariant> b) {
            ResourceLocation idA = a.getFirst();
            ResourceLocation idB = b.getFirst();
            if(idA == null)
                return -1;
            if(idB == null)
                return 1;
            UnbakedVariant varA = a.getSecond();
            UnbakedVariant varB = b.getSecond();
            String nameA = varA.getName().getString();
            String nameB = varB.getName().getString();
            int nameSort = nameA.compareToIgnoreCase(nameB);
            if(nameSort == 0)
                return idA.compareNamespaced(idB);
            return nameSort;
        }
    }

}