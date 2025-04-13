package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.tests.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomModelDataManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final CustomModelDataManager INSTANCE = new CustomModelDataManager();

    private CustomModelDataManager() {
        super(GSON, "lightmanscurrency/custom_model_data");
        //Register built-in CustomModelTests here
        CustomModelTest.register(ConstantTest.TYPE,ConstantTest::parse);
        CustomModelTest.register(AndTest.TYPE,AndTest::parse);
        CustomModelTest.register(OrTest.TYPE,OrTest::parse);
        CustomModelTest.register(BlockEntityTest.TYPE,BlockEntityTest::parse);
    }

    private final Map<ResourceLocation,CustomModelData> data = new HashMap<>();

    @Nullable
    public static ModelResourceLocation getCustomModel(ItemTraderBlockEntity blockEntity, ItemStack item)
    {
        ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(item.getItem());
        if(INSTANCE.data.containsKey(itemID))
            return INSTANCE.data.get(itemID).getCustomModel(blockEntity,item);
        return null;
    }

    @Override
    protected void apply(Map<ResourceLocation,JsonElement> map, ResourceManager resourceManager, ProfilerFiller filler) {
        this.data.clear();
        map.forEach((id,json) -> {
            try {
                CustomModelData data = CustomModelData.read(GsonHelper.convertToJsonObject(json,"top element"));
                this.data.put(id,data);
            }catch (JsonSyntaxException | IllegalArgumentException | ResourceLocationException e) {
                LightmansCurrency.LogError("Parsing error loading custom model data " + id, e);
            }
        });
    }

}
