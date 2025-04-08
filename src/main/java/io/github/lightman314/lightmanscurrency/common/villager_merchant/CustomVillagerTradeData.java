package io.github.lightman314.lightmanscurrency.common.villager_merchant;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomVillagerTradeData {

    private static final ResourceLocation WANDERING_TRADER_ID = VersionUtil.vanillaResource( "wandering_trader");

    private static final Map<ResourceLocation,Map<Integer,List<ItemListing>>> defaultValues = new HashMap<>();

    private static Map<Integer,List<ItemListing>> getEmptyMap() {
        Map<Integer,List<ItemListing>> map = new HashMap<>();
        for(int i = 1; i <= 5; ++i)
            map.put(i, new ArrayList<>());
        return map;
    }

    private static Map<Integer,List<ItemListing>> getDefaultVillagerData(@Nonnull ResourceLocation villager) {
        return defaultValues.getOrDefault(villager, getEmptyMap());
    }

    public static void registerDefaultWanderingTrades(@Nonnull List<ItemListing> genericValues, @Nonnull List<ItemListing> rareValues)
    {
        Map<Integer,List<ItemListing>> valueMap = new HashMap<>();
        valueMap.put(1,genericValues);
        valueMap.put(2, rareValues);
        registerDefaultFile(WANDERING_TRADER_ID, valueMap);
    }

    public static void registerDefaultFile(@Nonnull ResourceLocation villager, @Nonnull Map<Integer,List<ItemListing>> value) {
        if(defaultValues.containsKey(villager))
            LightmansCurrency.LogWarning("Attempted to register default villager data of type '" + villager + "' twice!");
        else
        {
            for(int i = 1; i <=5; ++i)
            {
                if(value.get(i) == null && villager != WANDERING_TRADER_ID)
                    LightmansCurrency.LogError("Default value for '" + villager + "' does not have all five valid entries!");
            }
            defaultValues.put(villager, value);
        }
    }

    public static Pair<List<ItemListing>,List<ItemListing>> getWanderingTraderData()
    {
        Map<Integer,List<ItemListing>> value = getVillagerData(WANDERING_TRADER_ID);
        return Pair.of(value.getOrDefault(1, new ArrayList<>()),value.getOrDefault(2, new ArrayList<>()));
    }

    @Nonnull
    public static Map<Integer,List<ItemListing>> getVillagerData(@Nonnull ResourceLocation villager) {
        File file = getVillagerDataFile(villager);
        HolderLookup.Provider lookup = LookupHelper.getRegistryAccess();
        if(file.exists())
        {
            try{
                String text = Files.readString(file.toPath());
                JsonObject json = GsonHelper.parse(text);
                return ItemListingSerializer.deserialize(json,lookup);
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading villager data file '" + file.getName() + "'!", t); }
        }
        else
        {
            //Create default file
            try{
                File dir = file.getParentFile();
                dir.mkdirs();
                Map<Integer,List<ItemListing>> defaultValues = getDefaultVillagerData(villager);
                FileUtil.writeStringToFile(file, FileUtil.GSON.toJson(ItemListingSerializer.serialize(defaultValues, villager.equals(WANDERING_TRADER_ID) ? 2 : 5,lookup)));
            } catch(Throwable t) { LightmansCurrency.LogError("Error creating default villager data file '" + file.getName() + "'!", t); }
        }

        return getDefaultVillagerData(villager);
    }

    public static @Nonnull File getVillagerDataFile(@Nonnull ResourceLocation villager) {
        String filePath = "config/trades/" + villager.getNamespace() + "/custom_" + villager.getPath() + "_trades.json";
        return new File(filePath);
    }

}