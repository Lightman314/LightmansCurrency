package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModelVariant {

    private boolean completelyInvalid = false;
    public boolean shouldRemove() { return this.completelyInvalid || (this.parentVariant != null && this.parentVariant.shouldRemove()); }
    private boolean invalid = false;
    public boolean isValid() { return !this.isInvalid(); }
    public boolean isInvalid() { return this.shouldRemove() || this.invalid || this.dummy; }

    private final boolean dummy;

    @Nullable
    private final ResourceLocation parent;
    @Nullable
    private ModelVariant parentVariant = null;

    private final List<ResourceLocation> targets;
    public List<ResourceLocation> getTargets() {
        if(this.targets.isEmpty() && this.parentVariant != null)
            return this.parentVariant.getTargets();
        return this.targets;
    }

    @Nullable
    private final Component name;
    public Component getName() {
        if(this.name == null && this.parentVariant != null)
            return this.parentVariant.getName();
        return this.name == null ? LCText.BLOCK_VARIANT_UNNAMED.get() : this.name;
    }

    @Nullable
    private ModelResourceLocation item;
    private Map<ResourceLocation,ModelResourceLocation> targetedItems = ImmutableMap.of();
    @ApiStatus.Internal
    public ModelResourceLocation getItem() {
        if(this.item == null && this.parentVariant != null)
            return this.parentVariant.getItem();
        return this.item;
    }
    public ModelResourceLocation getItem(@Nullable IVariantBlock target) {
        if(target == null)
            return this.getItem();
        ModelResourceLocation itemModel = this.targetedItems.get(target.getBlockID());
        return itemModel == null ? this.getItem() : itemModel;
    }
    @ApiStatus.Internal
    public void overrideItemModel(ModelResourceLocation modelID) { this.item = modelID; }
    public void defineTargetBasedItemModel(Map<ResourceLocation,ModelResourceLocation> targetedItems) { this.targetedItems = ImmutableMap.copyOf(targetedItems); }
    @Nullable
    public ItemStack getItemIcon() { return null; }

    private List<ResourceLocation> models;
    private Map<ResourceLocation,List<ResourceLocation>> targetedModels = ImmutableMap.of();
    public boolean hasModels() { return !this.getModels().isEmpty(); }
    public List<ResourceLocation> getModels() {
        if(this.models.isEmpty() && this.parent != null)
            return parentVariant == null ? ImmutableList.of() : parentVariant.getModels();
        return this.models;
    }
    public List<ResourceLocation> getModels(@Nullable IVariantBlock target)
    {
        if(target == null)
            return this.getModels();
        return Objects.requireNonNullElseGet(this.targetedModels.get(target.getBlockID()),this::getModels);
    }
    @ApiStatus.Internal
    public void overrideModels(List<ResourceLocation> models) { this.models = ImmutableList.copyOf(models); }
    public void defineTargetBasedModels(Map<ResourceLocation,List<ResourceLocation>> models) { this.targetedModels = ImmutableMap.copyOf(models); }
    @Nullable
    private ResourceLocation getModel(int index)
    {
        List<ResourceLocation> models = this.getModels();
        if(index < 0 || index >= models.size())
            return null;
        return models.get(index);
    }
    @Nullable
    public ResourceLocation getModel(@Nullable IVariantBlock target, int index)
    {
        if(target == null)
            return this.getModel(index);
        List<ResourceLocation> models = this.targetedModels.get(target.getBlockID());
        if(models == null)
            models = this.getModels();
        if(index < 0 || index >= models.size())
            return null;
        return models.get(index);
    }
    @Nullable
    public ModelResourceLocation getStandaloneModel(@Nullable IVariantBlock target, int index)
    {
        ResourceLocation model = this.getModel(target,index);
        return model == null ? null : ModelResourceLocation.standalone(model);
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

    private final Map<VariantProperty<?>,Object> properties;

    protected ModelVariant() { this(null,new ArrayList<>(), null,null,new ArrayList<>(),new HashMap<>(),new HashMap<>(),true); }
    private ModelVariant(@Nullable ResourceLocation parent, List<ResourceLocation> targets, @Nullable Component name, @Nullable ModelResourceLocation item, List<ResourceLocation> models, Map<String,ResourceLocation> textureOverrides, Map<VariantProperty<?>,Object> properties, boolean dummy)
    {
        this.parent = parent;
        this.targets = ImmutableList.copyOf(targets);
        this.name = name;
        this.item = item;
        this.models = ImmutableList.copyOf(models);
        this.textureOverrides = ImmutableMap.copyOf(textureOverrides);
        this.properties = ImmutableMap.copyOf(properties);
        this.dummy = dummy;
    }

    public boolean has(VariantProperty<?> property) {
        if(this.parentVariant != null && this.parentVariant.has(property))
            return true;
        return this.properties.containsKey(property);
    }

    @Nullable
    public <T> T get(VariantProperty<T> property) {
        try {
            if(!this.properties.containsKey(property) && this.parentVariant != null)
                return this.parentVariant.get(property);
            return (T)this.properties.get(property);
        } catch (ClassCastException e) { return null; }
    }

    public JsonObject write()
    {
        JsonObject json = new JsonObject();
        if(this.parent != null)
            json.addProperty("parent",this.parent.toString());
        if(this.targets.size() == 1)
            json.addProperty("target",this.targets.getFirst().toString());
        else if(!this.targets.isEmpty())
        {
            JsonArray array = new JsonArray();
            for(ResourceLocation t : this.targets)
                array.add(t.toString());
            json.add("target",array);
        }
        if(this.name != null)
            json.add("name", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,this.name).getOrThrow());
        if(this.item != null)
            json.addProperty("item",this.item.id().toString());
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
        this.properties.forEach((property,value) -> {
            try {
                JsonElement element = property.write(value);
                json.add(property.getID().toString(),element);
            }catch (Exception e) { LightmansCurrency.LogError("Error writing Variant Property",e); }
        });
        if(this.dummy)
            json.addProperty("dummy",true);
        return json;
    }

    public static ModelVariant parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException
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
                targets.add(VersionUtil.parseResource(GsonHelper.getAsString(json,"target")));
            }
            else
            {
                JsonArray array = GsonHelper.getAsJsonArray(json,"target");
                if(array.isEmpty())
                    throw new JsonSyntaxException("At least one target must be defined");
                for(int i = 0; i < array.size(); ++i)
                    targets.add(VersionUtil.parseResource(GsonHelper.convertToString(array.get(i),"target[" + i + "]")));
            }
        }

        Component name = null;
        if(json.has("name"))
            name = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE,json.get("name")).getOrThrow(JsonSyntaxException::new).getFirst();

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
        Map<VariantProperty<?>,Object> properties = new HashMap<>();
        VariantProperty.forEach((id,property) -> {
            if(json.has(id.toString()))
                properties.put(property,property.parse(json.get(id.toString())));
        });
        if(targets.isEmpty() && name == null && item == null && models.isEmpty() && textureOverrides.isEmpty() && properties.isEmpty())
        {
            if(parent == null)
                throw new JsonSyntaxException("Model Variant must have something defined!");
            else
                throw new JsonSyntaxException("Model Variant must have something other than the parent defined!");
        }
        boolean dummy = GsonHelper.getAsBoolean(json,"dummy",false);
        return new ModelVariant(parent,targets,name,item == null ? null : ModelResourceLocation.standalone(item),models,textureOverrides,properties,dummy);
    }

    
    public void validate(@Nullable Map<ResourceLocation, ModelVariant> otherVariants, ResourceLocation id) {
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
        for(ModelVariant v : loop.variants)
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
        for(ModelVariant v : loop.variants)
        {
            if(!v.textureOverrides.isEmpty())
            {
                hasTextures = true;
                break;
            }
        }

        //Check if we have a valid item model
        boolean hasItem = false;
        for(ModelVariant v : loop.variants)
        {
            if(v.item != null) {
                hasItem = true;
                break;
            }
        }

        //Check for valid models
        boolean hasModels = false;
        for(ModelVariant v : loop.variants)
        {
            if(!v.models.isEmpty()) {
                hasModels = true;
                for(ResourceLocation t : targets)
                {
                    Block block = BuiltInRegistries.BLOCK.get(t);
                    if(block instanceof IVariantBlock vb)
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
        }
        //If models are present, a custom item must also be present
        //If no textures are present, a model/item must be present
        if(hasModels != hasItem)
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
        if(!hasTextures && !hasModels)
        {
            if(id != null)
                LightmansCurrency.LogDebug(id + " does not have any custom models or custom textures defined");
            return true;
        }
        return false;
    }

    private boolean checkForLoop(Map<ResourceLocation,ModelVariant> existingVariants, ResourceLocation myID, LoopData loop)
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

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        @Nullable
        private ResourceLocation parent;
        private final List<ResourceLocation> targets = new ArrayList<>();
        @Nullable
        private Component name;
        private ModelResourceLocation item = null;
        private final List<ResourceLocation> models = new ArrayList<>();
        private final Map<String,ResourceLocation> textureOverrides = new HashMap<>();
        private final Map<VariantProperty<?>,Object> properties = new HashMap<>();
        private boolean dummy = false;

        private Builder() {}

        public Builder withParent(ResourceLocation parent) { this.parent = parent; return this; }

        public Builder withTarget(Supplier<? extends Block> block) { return this.withTarget(block.get()); }
        public Builder withTarget(Block block) { return this.withTarget(BuiltInRegistries.BLOCK.getKey(block)); }
        public Builder withTarget(ResourceLocation target) { if(!this.targets.contains(target)) this.targets.add(target); return this; }

        public Builder withName(Component name) { this.name = name; return this; }

        public Builder withItem(ResourceLocation item) { this.item = ModelResourceLocation.standalone(item); return this; }

        public Builder withModel(ResourceLocation... model) { this.models.addAll(Lists.newArrayList(model)); return this; }

        public Builder withTexture(String textureKey, ResourceLocation texture) { this.textureOverrides.put(textureKey,texture); return this; }

        public <T> Builder withProperty(VariantProperty<T> property, T value) { this.properties.put(property,value); return this; }

        public Builder asDummy() { this.dummy = true; return this; }

        public ModelVariant build() { return new ModelVariant(this.parent,this.targets,this.name,this.item,this.models,this.textureOverrides,this.properties,this.dummy); }

    }

    private record LoopData(List<ResourceLocation> ids, List<ModelVariant> variants)  {
        LoopData(ResourceLocation firstID,ModelVariant firstVariant) {
            this(Lists.newArrayList(firstID),Lists.newArrayList(firstVariant));
        }
        <T extends Throwable> boolean checkAndAdd(ResourceLocation id, ModelVariant variant)
        {
            if(this.ids.contains(id))
                return true;
            this.ids.add(id);
            this.variants.add(variant);
            return false;
        }
    }

}