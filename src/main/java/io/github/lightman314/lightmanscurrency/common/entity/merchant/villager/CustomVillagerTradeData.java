package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomVillagerTradeData {

    private static final Map<ResourceLocation,Map<Integer,List<VillagerTrades.ITrade>>> defaultValues = new HashMap<>();

    private static Map<Integer,List<VillagerTrades.ITrade>> getEmptyMap() {
        Map<Integer,List<VillagerTrades.ITrade>> map = new HashMap<>();
        for(int i = 1; i <= 5; ++i)
            map.put(i, new ArrayList<>());
        return map;
    }

    private static Map<Integer,List<VillagerTrades.ITrade>> getDefaultVillagerData(@Nonnull ResourceLocation villager) {
        return defaultValues.getOrDefault(villager, getEmptyMap());
    }

    public static void registerDefaultFile(@Nonnull ResourceLocation villager, @Nonnull Map<Integer,List<VillagerTrades.ITrade>> value) {
        if(defaultValues.containsKey(villager))
            LightmansCurrency.LogWarning("Attempted to register default villager data of type '" + villager + "' twice!");
        else
            defaultValues.put(villager, value);
    }

    @Nonnull
    public static Map<Integer,List<VillagerTrades.ITrade>> getVillagerData(@Nonnull ResourceLocation villager) {
        File file = getVillagerDataFile(villager);
        if(file.exists())
        {
            try{
                String text = FileUtil.readString(file);
                JsonObject json = FileUtil.JSON_PARSER.parse(text).getAsJsonObject();
                return ItemListingSerializer.deserialize(json);
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading villager data file '" + file.getName() + "'!", t); }
        }
        //Create default file
        try{
            File dir = file.getParentFile();
            dir.mkdirs();
            Map<Integer,List<VillagerTrades.ITrade>> defaultValues = getDefaultVillagerData(villager);
            FileUtil.writeStringToFile(file, FileUtil.GSON.toJson(ItemListingSerializer.serialize(defaultValues)));
        } catch(Throwable t) { LightmansCurrency.LogError("Error creating default villager data file '" + file.getName() + "'!", t); }

        return getDefaultVillagerData(villager);
    }

    public static @Nonnull File getVillagerDataFile(@Nonnull ResourceLocation villager) {
        String filePath = "config/" + villager.getNamespace() + "/custom_" + villager.getPath() + "_trades.json";
        return new File(filePath);
    }

}