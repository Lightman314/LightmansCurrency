package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredTradeModOption;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfiguredTradeMod extends VillagerTradeMod {

    private final Pair<Item,Item> defaultReplacements;
    private final Map<String,Pair<Item,Item>> regionalReplacements;

    public Pair<Item,Item> getDefaultReplacements() { return this.defaultReplacements; }
    public Map<String,Pair<Item,Item>> getRegionalReplacements() { return this.regionalReplacements; }

    public boolean isEmpty() { return this.defaultReplacements.getFirst() == null && this.defaultReplacements.getSecond() == null && this.regionalReplacements.isEmpty(); }

    public ConfiguredTradeMod(Pair<Item,Item> defaultReplacements, Map<String,Pair<Item,Item>> regionalReplacements)
    {
        this.defaultReplacements = defaultReplacements;
        this.regionalReplacements = ImmutableMap.copyOf(regionalReplacements);
    }

    public static ConfiguredTradeMod tryParse(String entries, boolean forceDefaults) throws ConfigParsingException
    {
        Pair<Item,Item> defaultReplacements = Pair.of(null,null);
        Map<String,Pair<Item,Item>> regionalReplacements = new HashMap<>();
        String[] split = entries.split("-");
        for (String subEntry : split) {

            String region = null;

            String[] subSplits = subEntry.split(";");
            Item costReplacement;
            Item resultReplacement;
            if (subEntry.startsWith("r;") || subEntry.startsWith("R;")) {
                //Define the region
                if (subSplits.length < 3)
                    throw new ConfigParsingException("Entry starts with 'r;' but is missing either the villager type or the defined replacement entry.");
                if (subSplits.length > 4)
                    throw new ConfigParsingException("Entry contains too many ';' splits!");
                region = subSplits[1];
                //LightmansCurrency.LogDebug("Parsing items for '" + region + "' region:");
                if (subSplits.length > 3) {
                    costReplacement = tryParseItem(subSplits[2]);
                    resultReplacement = tryParseItem(subSplits[3]);
                } else
                    costReplacement = resultReplacement = tryParseItem(subSplits[2]);
            } else {
                //LightmansCurrency.LogDebug("Parsing default items:");
                costReplacement = tryParseItem(subSplits[0]);
                if (subSplits.length > 1)
                    resultReplacement = tryParseItem(subSplits[1]);
                else
                    resultReplacement = costReplacement;
            }

            //Check results
            if (region == null && shouldWrite(defaultReplacements))
                throw new ConfigParsingException("Entry cannot have multiple default entries! All non-default entries should start with 'r;VILLAGER_TYPE; to define their subsequent region.");
            if (region != null && regionalReplacements.containsKey(region))
                throw new ConfigParsingException("Entry has duplicate villager type entry for the '" + region + "' villager type!");
            if (region == null)
                defaultReplacements = Pair.of(costReplacement, resultReplacement);
            else
                regionalReplacements.put(region, Pair.of(costReplacement, resultReplacement));

        }
        if(forceDefaults && (defaultReplacements.getFirst() == null || defaultReplacements.getSecond() == null))
            throw new ConfigParsingException("Missing default cost and/or result entry!");
        if(defaultReplacements.getFirst() == null && defaultReplacements.getSecond() == null && regionalReplacements.isEmpty())
            throw new ConfigParsingException("No valid sub-entries were parsed!");
        return new ConfiguredTradeMod(defaultReplacements,regionalReplacements);
    }

    @Nullable
    private static Item tryParseItem(String item) throws ConfigParsingException
    {
        //LightmansCurrency.LogDebug("Attempting to parse '" + item + "' as an item!");
        if(item.isBlank() || item.equals("minecraft:air"))
            return null;
        try { return BuiltInRegistries.ITEM.get(VersionUtil.parseResource(item));
        } catch (ResourceLocationException e) { throw new ConfigParsingException(item + " is not a valid ResourceLocation!",e); }
    }

    private static boolean shouldWrite(Pair<Item,Item> pair) { return pair.getFirst() != null || pair.getSecond() != null; }

    public final void write(StringBuilder builder)
    {
        AtomicBoolean addDash = new AtomicBoolean(false);
        if(shouldWrite(this.defaultReplacements))
        {
            if(addDash.get())
                builder.append("-");
            else
                addDash.set(true);
            writePair(this.defaultReplacements, builder);
        }
        this.regionalReplacements.forEach((key,pair) -> {
            if(shouldWrite(pair))
            {
                if(addDash.get())
                    builder.append("-");
                else
                    addDash.set(true);
                builder.append("r;").append(key).append(";");
                writePair(pair,builder);
            }
        });
    }

    private static void writePair(Pair<Item,Item> pair, StringBuilder builder)
    {
        if(pair.getFirst() != null && pair.getFirst() == pair.getSecond())
            builder.append(getID(pair.getFirst()));
        else
            builder.append(getID(pair.getFirst())).append(";").append(getID(pair.getSecond()));
    }

    
    private static String getID(@Nullable Item item) {
        if(item == null)
            return "";
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    @Nullable
    private String getType(@Nullable Entity villager)
    {
        if(villager instanceof Villager v)
        {
            VillagerData d = v.getVillagerData();
            if(d != null)
            {
                VillagerType t = d.getType();
                if(t != null)
                    return BuiltInRegistries.VILLAGER_TYPE.getKey(t).toString();
            }
        }
        return null;
    }

    private Pair<Item,Item> getPair(Entity villager)
    {
        String type = this.getType(villager);
        if(type == null)
            return this.defaultReplacements;
        else
            return this.regionalReplacements.getOrDefault(type, this.defaultReplacements);
    }

    @Nullable
    private Item getCost(Entity villager)
    {
        Pair<Item,Item> pair = this.getPair(villager);
        Item first = pair.getFirst();
        if(first == null && this != LCConfig.COMMON.defaultEmeraldReplacementMod.get())
            return LCConfig.COMMON.defaultEmeraldReplacementMod.get().getCost(villager);
        return first;
    }

    
    private Item getResult(Entity villager)
    {
        Pair<Item,Item> pair = this.getPair(villager);
        Item second = pair.getSecond();
        if(second == null && this != LCConfig.COMMON.defaultEmeraldReplacementMod.get())
            return LCConfig.COMMON.defaultEmeraldReplacementMod.get().getResult(villager);
        return second;
    }


    
    @Override
    public ItemCost modifyCost(@Nullable Entity villager, ItemCost cost) {
        if(cost.item().value() == Items.EMERALD)
            return this.copyWithNewItem(cost, this.getCost(villager));
        return cost;
    }

    @Override
    public ItemStack modifyResult(@Nullable Entity villager, ItemStack result) {
        if(result.getItem() == Items.EMERALD)
            return this.copyWithNewItem(result, this.getResult(villager));
        return result;
    }

    public static ModBuilder builder() { return new ModBuilder(null); }
    public static ModBuilder builder(VillagerTradeMods.Builder parent) { return new ModBuilder(parent); }

    public static final class ModBuilder
    {
        private final VillagerTradeMods.Builder parent;

        private Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> defaultReplacement = Pair.of(null,null);
        private final Map<String, Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>>> regionalReplacements = new HashMap<>();

        private ModBuilder(@Nullable VillagerTradeMods.Builder parent) { this.parent = parent; }

        @Nullable
        public VillagerTradeMods.Builder back() { return this.parent; }

        
        private Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> replaceCost(Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair, Supplier<? extends ItemLike> newCost) { return Pair.of(newCost,pair.getSecond()); }
        private Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> replaceResult(Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair, Supplier<? extends ItemLike> newResult) { return Pair.of(pair.getFirst(),newResult); }

        
        public ModBuilder defaultCost(Supplier<? extends ItemLike> costReplacement) { this.defaultReplacement = this.replaceCost(this.defaultReplacement,costReplacement); return this; }
        
        public ModBuilder defaultResult(Supplier<? extends ItemLike> resultReplacement) { this.defaultReplacement = this.replaceResult(this.defaultReplacement,resultReplacement); return this; }
        
        public ModBuilder defaults(Supplier<? extends ItemLike> replacement) { this.defaultReplacement = Pair.of(replacement,replacement); return this; }

        
        public ModBuilder costForRegion(VillagerType type, Supplier<? extends ItemLike> costReplacement) { return this.costForRegion(BuiltInRegistries.VILLAGER_TYPE.getKey(type),costReplacement); }
        public ModBuilder costForRegion(ResourceLocation type, Supplier<? extends ItemLike> costReplacement) { return this.costForRegion(type.toString(),costReplacement); }
        
        public ModBuilder costForRegion(String type, Supplier<? extends ItemLike> costReplacement) {
            Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair = this.regionalReplacements.getOrDefault(type,Pair.of(null,null));
            this.regionalReplacements.put(type,this.replaceCost(pair,costReplacement));
            return this;
        }
        
        public ModBuilder resultForRegion(VillagerType type, Supplier<? extends ItemLike> resultReplacement) { return this.resultForRegion(BuiltInRegistries.VILLAGER_TYPE.getKey(type),resultReplacement); }
        
        public ModBuilder resultForRegion(ResourceLocation type, Supplier<? extends ItemLike> resultReplacement) { return this.resultForRegion(type.toString(),resultReplacement); }
        
        public ModBuilder resultForRegion(String type, Supplier<? extends ItemLike> resultReplacement) {
            Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair = this.regionalReplacements.getOrDefault(type,Pair.of(null,null));
            this.regionalReplacements.put(type,this.replaceResult(pair,resultReplacement));
            return this;
        }

        
        public ModBuilder bothForRegion(VillagerType type, Supplier<? extends ItemLike> replacement) { return this.bothForRegion(BuiltInRegistries.VILLAGER_TYPE.getKey(type),replacement); }
        
        public ModBuilder bothForRegion(ResourceLocation type, Supplier<? extends ItemLike> replacement) { return this.bothForRegion(type.toString(),replacement); }
        
        public ModBuilder bothForRegion(String type, Supplier<? extends ItemLike> replacement) {
            this.regionalReplacements.put(type,Pair.of(replacement,replacement));
            return this;
        }


        public ConfiguredTradeMod build(boolean forceDefaults) {
            Map<String,Pair<Item,Item>> temp = new HashMap<>();
            this.regionalReplacements.forEach((key,pair) ->
                    temp.put(key,buildPair(pair))
            );
            Pair<Item,Item> defaultReplacements = buildPair(this.defaultReplacement);
            if(forceDefaults && (defaultReplacements.getFirst() == null || defaultReplacements.getSecond() == null))
            {
                //Attempt to autofill the missing default
                if(defaultReplacements.getFirst() == null)
                {
                    if(defaultReplacements.getSecond() == null) //Both are missing, illegal state located
                        throw new IllegalStateException("Cannot built a ConfiguredTradeMod with no default replacements!");
                    else
                        defaultReplacements = Pair.of(defaultReplacements.getSecond(),defaultReplacements.getSecond());
                }
                else
                    defaultReplacements = Pair.of(defaultReplacements.getFirst(),defaultReplacements.getFirst());
            }
            return new ConfiguredTradeMod(defaultReplacements,temp);
        }
        
        public ConfiguredTradeModOption buildOption() { return ConfiguredTradeModOption.create(() -> this.build(true)); }

    }

    
    public static Pair<Item,Item> buildPair(Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair) { return Pair.of(safeGet(pair.getFirst()), safeGet(pair.getSecond())); }

    @Nullable
    private static Item safeGet(@Nullable Supplier<? extends ItemLike> supplier)
    {
        if(supplier == null || supplier.get() == null)
            return null;
        return supplier.get().asItem();
    }

}
