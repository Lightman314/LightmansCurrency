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
import net.minecraft.world.level.block.Blocks;

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
    public boolean isInvalid() { return this.shouldRemove() || this.invalid; }

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
    public Component getName() { return this.name == null ? LCText.BLOCK_VARIANT_UNNAMED.get() : this.name; }

    @Nullable
    private ModelResourceLocation item;
    public ModelResourceLocation getItem() {
        if(this.item == null && this.parentVariant != null)
            return this.parentVariant.getItem();
        return this.item;
    }
    public void overrideItemModel(ModelResourceLocation modelID)
    {
        this.item = modelID;
    }
    
    @Nullable
    public ItemStack getItemIcon() { return null; }

    private final List<ResourceLocation> models;
    @Nullable
    public List<ResourceLocation> getModels() {
        if(this.models.isEmpty() && this.parent != null)
            return parentVariant == null ? ImmutableList.of() : parentVariant.getModels();
        return this.models;
    }
    @Nullable
    public ResourceLocation getModel(int index)
    {
        List<ResourceLocation> models = this.getModels();
        if(index < 0 || index >= models.size())
            return null;
        return models.get(index);
    }
    @Nullable
    public ModelResourceLocation getStandaloneModel(int index)
    {
        ResourceLocation model = this.getModel(index);
        return model == null ? null : ModelResourceLocation.standalone(model);
    }

    private final Map<String,ResourceLocation> textureOverrides;
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

    protected ModelVariant() { this(null,new ArrayList<>(), null,null,new ArrayList<>(),new HashMap<>(),new HashMap<>()); }
    private ModelVariant(@Nullable ResourceLocation parent, List<ResourceLocation> targets, @Nullable Component name, @Nullable ModelResourceLocation item, List<ResourceLocation> models, Map<String,ResourceLocation> textureOverrides, Map<VariantProperty<?>,Object> properties)
    {
        this.parent = parent;
        this.targets = ImmutableList.copyOf(targets);
        this.name = name;
        this.item = item;
        this.models = ImmutableList.copyOf(models);
        this.textureOverrides = ImmutableMap.copyOf(textureOverrides);
        this.properties = ImmutableMap.copyOf(properties);
    }

    protected final ModelResourceLocation buildModel(@Nullable ResourceLocation model) { return ModelResourceLocation.standalone(Objects.requireNonNullElseGet(model, () -> VersionUtil.vanillaResource("null"))); }
    protected final List<ModelResourceLocation> buildModels(List<ResourceLocation> models) {
        ImmutableList.Builder<ModelResourceLocation> builder = ImmutableList.builderWithExpectedSize(models.size());
        models.forEach(m -> builder.add(this.buildModel(m)));
        return builder.build();
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
        //Name is never null
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
        return new ModelVariant(parent,targets,name,ModelResourceLocation.standalone(item),models,textureOverrides,properties);
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
                    this.completelyInvalid = true;
                    return;
                }
                //Check if valid when combined
                this.invalid = invalidWhenCombined(loop);
            }
            else
            {
                this.completelyInvalid = true;
            }
        }
        else if(otherVariants != null)
        {
            this.invalid = invalidWhenCombined(new LoopData(id,this));
        }
    }

    private static boolean invalidWhenCombined(LoopData loop)
    {
        boolean notFound = true;
        //Check if we have valid targets
        List<ResourceLocation> targets = new ArrayList<>();
        for(ModelVariant v : loop.variants)
        {
            if (!v.targets.isEmpty()) {
                notFound = false;
                targets = v.targets;
                break;
            }
        }
        //Missing targets
        if(notFound)
            return true;
        notFound = true;
        //Check if the top-most variant has a valid name
        if(loop.variants.getFirst().name == null)
            return true;
        //Check if we have a valid icon/item form
        for(ModelVariant v : loop.variants)
        {
            if(v.item != null) {
                notFound = false;
                break;
            }
        }
        if(notFound)
            return true;
        //Check for valid models
        for(ModelVariant v : loop.variants)
        {
            if(!v.models.isEmpty()) {
                for(ResourceLocation t : targets)
                {
                    Block block = BuiltInRegistries.BLOCK.get(t);
                    if(block == Blocks.AIR)
                        return true;
                    if(block instanceof IVariantBlock vb)
                    {
                        if(vb.requiredModels() != v.models.size())
                            return true;
                    }
                    else
                        return true;
                }
            }
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

    public static Builder builder() { return new Builder(null); }
    public static Builder childBuilder(ResourceLocation parent) { return new Builder(parent); }

    public static class Builder
    {
        @Nullable
        private final ResourceLocation parent;
        private final List<ResourceLocation> targets = new ArrayList<>();
        private Component name;
        private ModelResourceLocation item = null;
        private final List<ResourceLocation> models = new ArrayList<>();
        private final Map<String,ResourceLocation> textureOverrides = new HashMap<>();
        private final Map<VariantProperty<?>,Object> properties = new HashMap<>();

        private Builder(@Nullable ResourceLocation parent) { this.parent = parent; this.name = LCText.BLOCK_VARIANT_UNNAMED.get(); }

        public Builder withTarget(Supplier<? extends Block> block) { return this.withTarget(block.get()); }
        public Builder withTarget(Block block) { return this.withTarget(BuiltInRegistries.BLOCK.getKey(block)); }
        public Builder withTarget(ResourceLocation target) { if(!this.targets.contains(target)) this.targets.add(target); return this; }

        public Builder withName(Component name) { this.name = name; return this; }

        public Builder withItem(ResourceLocation item) { this.item = ModelResourceLocation.standalone(item); return this; }

        public Builder withModel(ResourceLocation... model) { this.models.addAll(Lists.newArrayList(model)); return this; }

        public Builder withTexture(String textureKey, ResourceLocation texture) { this.textureOverrides.put(textureKey,texture); return this; }

        public <T> Builder withProperty(VariantProperty<T> property, T value) { this.properties.put(property,value); return this; }

        public ModelVariant build() { return new ModelVariant(this.parent,this.targets,this.name,this.item,this.models,this.textureOverrides,this.properties); }

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