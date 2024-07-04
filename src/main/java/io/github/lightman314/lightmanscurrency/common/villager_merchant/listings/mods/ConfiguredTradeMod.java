package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredTradeModOption;
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
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ConfiguredTradeMod extends VillagerTradeMod {

    private final Pair<Item,Item> defaultReplacements;
    private final Map<String,Pair<Item,Item>> regionalReplacements;

    public ConfiguredTradeMod(@Nonnull Pair<Item,Item> defaultReplacements, @Nonnull Map<String,Pair<Item,Item>> regionalReplacements)
    {
        this.defaultReplacements = defaultReplacements;
        this.regionalReplacements = ImmutableMap.copyOf(regionalReplacements);
    }

    @Nonnull
    public static ConfiguredTradeMod tryParse(@Nonnull String entries, boolean forceDefaults) throws ConfigParsingException
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
                LightmansCurrency.LogDebug("Parsing items for '" + region + "' region:");
                if (subSplits.length > 3) {
                    costReplacement = tryParseItem(subSplits[2]);
                    resultReplacement = tryParseItem(subSplits[3]);
                } else
                    costReplacement = resultReplacement = tryParseItem(subSplits[2]);
            } else {
                LightmansCurrency.LogDebug("Parsing default items:");
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
    private static Item tryParseItem(@Nonnull String item) throws ConfigParsingException
    {
        LightmansCurrency.LogDebug("Attempting to parse '" + item + "' as an item!");
        if(item.isBlank() || item.equals("minecraft:air"))
            return null;
        try { return BuiltInRegistries.ITEM.get(ResourceLocation.parse(item));
        } catch (ResourceLocationException e) { throw new ConfigParsingException(item + " is not a valid ResourceLocation!",e); }
    }

    private static boolean shouldWrite(@Nonnull Pair<Item,Item> pair) { return pair.getFirst() != null || pair.getSecond() != null; }

    public final void write(@Nonnull StringBuilder builder)
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

    private static void writePair(@Nonnull Pair<Item,Item> pair, @Nonnull StringBuilder builder)
    {
        if(pair.getFirst() != null && pair.getFirst() == pair.getSecond())
            builder.append(getID(pair.getFirst()));
        else
            builder.append(getID(pair.getFirst())).append(";").append(getID(pair.getSecond()));
    }

    @Nonnull
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

    private Pair<Item,Item> getPair(@Nonnull Entity villager)
    {
        String type = this.getType(villager);
        if(type == null)
            return this.defaultReplacements;
        else
            return this.regionalReplacements.getOrDefault(type, this.defaultReplacements);
    }

    @Nullable
    private Item getCost(@Nonnull Entity villager)
    {
        Pair<Item,Item> pair = this.getPair(villager);
        Item first = pair.getFirst();
        if(first == null && this != LCConfig.COMMON.defaultEmeraldReplacementMod.get())
            return LCConfig.COMMON.defaultEmeraldReplacementMod.get().getCost(villager);
        return first;
    }

    @Nonnull
    private Item getResult(@Nonnull Entity villager)
    {
        Pair<Item,Item> pair = this.getPair(villager);
        Item second = pair.getSecond();
        if(second == null && this != LCConfig.COMMON.defaultEmeraldReplacementMod.get())
            return LCConfig.COMMON.defaultEmeraldReplacementMod.get().getResult(villager);
        return second;
    }


    @Nonnull
    @Override
    public ItemCost modifyCost(@Nullable Entity villager, @Nonnull ItemCost cost) {
        if(cost.item().is(Tags.Items.GEMS_EMERALD))
            return this.copyWithNewItem(cost, this.getCost(villager));
        return cost;
    }

    @Nonnull
    @Override
    public ItemStack modifyResult(@Nullable Entity villager, @Nonnull ItemStack result) {
        if(result.getItem() == Items.EMERALD)
            return this.copyWithNewItem(result, this.getResult(villager));
        return result;
    }

    public static ModBuilder builder() { return new ModBuilder(null); }
    public static ModBuilder builder(@Nonnull VillagerTradeMods.Builder parent) { return new ModBuilder(parent); }

    public static final class ModBuilder
    {
        private final VillagerTradeMods.Builder parent;

        private Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> defaultReplacement = Pair.of(null,null);
        private final Map<String, Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>>> regionalReplacements = new HashMap<>();

        private ModBuilder(@Nullable VillagerTradeMods.Builder parent) { this.parent = parent; }

        @Nullable
        public VillagerTradeMods.Builder back() { return this.parent; }

        @Nonnull
        private Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> replaceCost(@Nonnull Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair, Supplier<? extends ItemLike> newCost) { return Pair.of(newCost,pair.getSecond()); }
        private Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> replaceResult(@Nonnull Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair, Supplier<? extends ItemLike> newResult) { return Pair.of(pair.getFirst(),newResult); }

        @Nonnull
        public ModBuilder defaultCost(@Nonnull Supplier<? extends ItemLike> costReplacement) { this.defaultReplacement = this.replaceCost(this.defaultReplacement,costReplacement); return this; }
        @Nonnull
        public ModBuilder defaultResult(@Nonnull Supplier<? extends ItemLike> resultReplacement) { this.defaultReplacement = this.replaceResult(this.defaultReplacement,resultReplacement); return this; }
        @Nonnull
        public ModBuilder defaults(@Nonnull Supplier<? extends ItemLike> replacement) { this.defaultReplacement = Pair.of(replacement,replacement); return this; }

        @Nonnull
        public ModBuilder costForRegion(@Nonnull VillagerType type, @Nonnull Supplier<? extends ItemLike> costReplacement) { return this.costForRegion(BuiltInRegistries.VILLAGER_TYPE.getKey(type),costReplacement); }
        public ModBuilder costForRegion(@Nonnull ResourceLocation type, @Nonnull Supplier<? extends ItemLike> costReplacement) { return this.costForRegion(type.toString(),costReplacement); }
        @Nonnull
        public ModBuilder costForRegion(@Nonnull String type, @Nonnull Supplier<? extends ItemLike> costReplacement) {
            Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair = this.regionalReplacements.getOrDefault(type,Pair.of(null,null));
            this.regionalReplacements.put(type,this.replaceCost(pair,costReplacement));
            return this;
        }
        @Nonnull
        public ModBuilder resultForRegion(@Nonnull VillagerType type, @Nonnull Supplier<? extends ItemLike> resultReplacement) { return this.resultForRegion(BuiltInRegistries.VILLAGER_TYPE.getKey(type),resultReplacement); }
        @Nonnull
        public ModBuilder resultForRegion(@Nonnull ResourceLocation type, @Nonnull Supplier<? extends ItemLike> resultReplacement) { return this.resultForRegion(type.toString(),resultReplacement); }
        @Nonnull
        public ModBuilder resultForRegion(@Nonnull String type, @Nonnull Supplier<? extends ItemLike> resultReplacement) {
            Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair = this.regionalReplacements.getOrDefault(type,Pair.of(null,null));
            this.regionalReplacements.put(type,this.replaceResult(pair,resultReplacement));
            return this;
        }

        @Nonnull
        public ModBuilder bothForRegion(@Nonnull VillagerType type, @Nonnull Supplier<? extends ItemLike> replacement) { return this.bothForRegion(BuiltInRegistries.VILLAGER_TYPE.getKey(type),replacement); }
        @Nonnull
        public ModBuilder bothForRegion(@Nonnull ResourceLocation type, @Nonnull Supplier<? extends ItemLike> replacement) { return this.bothForRegion(type.toString(),replacement); }
        @Nonnull
        public ModBuilder bothForRegion(@Nonnull String type, @Nonnull Supplier<? extends ItemLike> replacement) {
            this.regionalReplacements.put(type,Pair.of(replacement,replacement));
            return this;
        }

        @Nonnull
        public ConfiguredTradeMod build() {
            Map<String,Pair<Item,Item>> temp = new HashMap<>();
            this.regionalReplacements.forEach((key,pair) ->
                temp.put(key,buildPair(pair))
            );
            return new ConfiguredTradeMod(buildPair(this.defaultReplacement), temp); }

        @Nonnull
        public ConfiguredTradeModOption buildOption() { return ConfiguredTradeModOption.create(this::build); }

    }

    @Nonnull
    public static Pair<Item,Item> buildPair(@Nonnull Pair<Supplier<? extends ItemLike>,Supplier<? extends ItemLike>> pair) { return Pair.of(safeGet(pair.getFirst()), safeGet(pair.getSecond())); }

    @Nullable
    private static Item safeGet(@Nullable Supplier<? extends ItemLike> supplier)
    {
        if(supplier == null || supplier.get() == null)
            return null;
        return supplier.get().asItem();
    }

}
