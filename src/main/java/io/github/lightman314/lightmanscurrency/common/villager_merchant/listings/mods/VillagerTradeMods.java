package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.common.config.VillagerTradeModsOption;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.ConfiguredTradeMod.ModBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VillagerTradeMods {


    private final Map<String,ConfiguredTradeMod> modMap;
    public final Map<String,ConfiguredTradeMod> getModMap() { return this.modMap; }
    public int getSize() { return this.modMap.size(); }
    private VillagerTradeMods(Builder builder)
    {
        Map<String,ConfiguredTradeMod> temp = new HashMap<>();
        builder.dataMap.forEach((key,subBuilder) -> temp.put(key, subBuilder.build(false)));
        this.modMap = ImmutableMap.copyOf(temp);
    }
    public VillagerTradeMods(Map<String,ConfiguredTradeMod> map) { this.modMap = ImmutableMap.copyOf(map); }
    public VillagerTradeMods(List<String> parseableData)
    {
        Map<String,ConfiguredTradeMod> temp = new HashMap<>();
        for(String data : parseableData)
        {
            try {
                Pair<String,ConfiguredTradeMod> results = tryParseEntry(data);
                if(temp.containsKey(results.getFirst()))
                    throw new ConfigParsingException("Duplicate profession type '" + results.getFirst() + "' modded!");
                else
                    temp.put(results.getFirst(),results.getSecond());
            } catch (ConfigParsingException e) {
                LightmansCurrency.LogError("Error parsing '" + data + "' as a villager trade modification.",e);
            }
        }
        this.modMap = ImmutableMap.copyOf(temp);
    }

    public final List<String> writeToConfig()
    {
        List<String> data = new ArrayList<>();
        this.modMap.forEach((key,entry) -> {
            StringBuilder builder = new StringBuilder(key).append("-");
            entry.write(builder);
            data.add(builder.toString());
        });
        return data;
    }

    private Pair<String,ConfiguredTradeMod> tryParseEntry(String entry) throws ConfigParsingException
    {
        String[] split = entry.split("-",2);
        if(split.length < 2)
            throw new ConfigParsingException("Missing '-' dividers!");
        String profession = split[0];
        LightmansCurrency.LogDebug("Attempting to parse entries for '" + profession + "' profession.");
        ConfiguredTradeMod mod = ConfiguredTradeMod.tryParse(split[1],false);
        return Pair.of(profession,mod);
    }

    public VillagerTradeMods edit(String value, String key, boolean isSet) throws ConfigParsingException
    {
        if(isSet)
        {
            ConfiguredTradeMod mod = ConfiguredTradeMod.tryParse(value,false);
            HashMap<String,ConfiguredTradeMod> map = new HashMap<>(this.modMap);
            map.put(key,mod);
            return new VillagerTradeMods(map);
        }
        else
        {
            if(this.modMap.containsKey(key))
            {
                HashMap<String,ConfiguredTradeMod> map = new HashMap<>(this.modMap);
                this.modMap.remove(key);
                return new VillagerTradeMods(map);
            }
            else throw new ConfigParsingException("Cannot remove the entry for the \"" + key + "\" profession as is is not present.");
        }
    }

    public final ConfiguredTradeMod getModFor(String trader) {
        if(this.modMap.containsKey(trader))
            return this.modMap.get(trader);
        return LCConfig.COMMON.defaultEmeraldReplacementMod.get();
    }

    public static Builder builder() { return new Builder(); }


    public static final class Builder
    {

        private final Map<String,ModBuilder> dataMap = new HashMap<>();

        private Builder() {}


        public ModBuilder forProfession(VillagerProfession profession) { return this.forProfession(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession)); }
        public ModBuilder forProfession(ResourceLocation profession) { return this.forProfession(profession.toString()); }

        public ModBuilder forProfession(String profession)
        {
            if(!this.dataMap.containsKey(profession))
                this.dataMap.put(profession,ConfiguredTradeMod.builder(this));
            return this.dataMap.get(profession);
        }

        public VillagerTradeMods build() { return new VillagerTradeMods(this); }

        public VillagerTradeModsOption buildOption() { return VillagerTradeModsOption.create(this::build); }

    }

}