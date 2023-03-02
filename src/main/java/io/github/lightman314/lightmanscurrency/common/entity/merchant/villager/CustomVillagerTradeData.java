package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomVillagerTradeData {

    private static final Map<ResourceLocation,Map<Integer,List<ItemListing>>> defaultValues = new HashMap<>();

    private static Map<Integer,List<ItemListing>> getEmptyMap() {
        Map<Integer,List<ItemListing>> map = new HashMap<>();
        for(int i = 1; i <= 5; ++i)
            map.put(i, new ArrayList<>());
        return map;
    }

    private static Map<Integer,List<ItemListing>> getDefaultVillagerData(@NotNull ResourceLocation villager) {
        return defaultValues.getOrDefault(villager, getEmptyMap());
    }

    public static void registerDefaultFile(@NotNull ResourceLocation villager, @NotNull Map<Integer,List<ItemListing>> value) {
        if(defaultValues.containsKey(villager))
            LightmansCurrency.LogWarning("Attempted to register default villager data of type '" + villager + "' twice!");
        else
            defaultValues.put(villager, value);
    }

    @NotNull
    public static Map<Integer,List<ItemListing>> getVillagerData(@NotNull ResourceLocation villager) {
        File file = getVillagerDataFile(villager);
        if(file.exists())
        {
            try{
                String text = Files.readString(file.toPath());
                JsonObject json = GsonHelper.parse(text);
                return ItemListingSerializer.deserialize(json);
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading villager data file '" + file.getName() + "'!", t); }
        }
        //Create default file
        try{
            File dir = file.getParentFile();
            dir.mkdirs();
            Map<Integer,List<ItemListing>> defaultValues = getDefaultVillagerData(villager);
            FileUtil.writeStringToFile(file, FileUtil.GSON.toJson(ItemListingSerializer.serialize(defaultValues)));
        } catch(Throwable t) { LightmansCurrency.LogError("Error creating default villager data file '" + file.getName() + "'!", t); }

        return getDefaultVillagerData(villager);
    }

    public static @NotNull File getVillagerDataFile(@NotNull ResourceLocation villager) {
        String filePath = "config/" + villager.getNamespace() + "/custom_" + villager.getPath() + "_trades.json";
        return new File(filePath);
    }

}