package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class CustomModelData {

    List<Pair<CustomModelTest,ModelResourceLocation>> data;

    private CustomModelData(List<Pair<CustomModelTest,ModelResourceLocation>> data) { this.data = ImmutableList.copyOf(data); if(this.data.isEmpty()) throw new IllegalArgumentException("Data & Tests cannot be empty!"); }

    @Nullable
    public ModelResourceLocation getCustomModel(ItemTraderBlockEntity blockEntity, ItemStack item)
    {
        for(var pair : this.data)
        {
            if(pair.getFirst().test(blockEntity,item))
                return pair.getSecond();
        }
        return null;
    }

    public JsonObject write() {
        JsonObject json = new JsonObject();
        JsonArray list = new JsonArray();
        for(var pair : this.data)
        {
            JsonObject entry = new JsonObject();
            entry.add("test",pair.getFirst().write());
            ModelResourceLocation modelID = pair.getSecond();
            //Only save the id if it's a standalone
            if(modelID.getVariant().equals("standalone"))
                entry.addProperty("model",VersionUtil.modResource(modelID.getNamespace(),modelID.getPath()).toString());
            else //If it's not a standalone, save the entire model id including variant
                entry.addProperty("model",modelID.toString());
        }
        json.add("custom_model_providers",list);
        return json;
    }

    public static CustomModelData read(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        JsonArray list = GsonHelper.getAsJsonArray(json,"custom_model_providers");
        if(list.isEmpty())
            throw new JsonSyntaxException("custom_model_providers cannot be empty!");
        List<Pair<CustomModelTest,ModelResourceLocation>> data = new ArrayList<>();
        for(int i = 0; i < list.size(); ++i)
        {
            JsonObject entry = GsonHelper.convertToJsonObject(list.get(i),"custom_model_providers[" + i + "]");
            JsonObject testEntry = GsonHelper.getAsJsonObject(entry,"test");
            CustomModelTest test = CustomModelTest.parse(testEntry);
            String modelID = GsonHelper.getAsString(json,"model");
            final ModelResourceLocation model;
            if(modelID.contains("#"))
            {
                String[] split = modelID.split("#",2);
                model = new ModelResourceLocation(VersionUtil.parseResource(split[0]),split[1]);
            }
            else
                model = new ModelResourceLocation(VersionUtil.parseResource(modelID),"standalone");
            data.add(Pair.of(test,model));
        }
        return new CustomModelData(data);
    }

    public static class Builder
    {
        private Builder() {}

        private final List<Pair<CustomModelTest,ModelResourceLocation>> data = new ArrayList<>();

        public Builder add(CustomModelTest test, ModelResourceLocation model) { this.data.add(Pair.of(test,model)); return this; }

        public CustomModelData build() { return new CustomModelData(this.data); }

    }

}