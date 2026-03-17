package io.github.lightman314.lightmanscurrency.api.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class StatTracker implements IClientTracker
{

    public static final Codec<Map<String,StatType.Instance<?,?>>> CODEC = Codec.withAlternative(
            Codec.unboundedMap(Codec.STRING,StatType.CODEC),
            CodecHelper.oldValueLoader(StatTracker::loadOldData,"Stat Map")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,Map<String,StatType.Instance<?,?>>> STREAM_CODEC = ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,StatType.STREAM_CODEC);

    private final Runnable onChange;
    private final IClientTracker parent;
    private final Map<String,StatType.Instance<?,?>> stats = new HashMap<>();
    public Map<String,StatType.Instance<?,?>> getStatMap() { return ImmutableMap.copyOf(this.stats); }

    @Override
    public boolean isClient() { return this.parent.isClient(); }

    public StatTracker(Runnable onChange, IClientTracker parent)
    {
        this.onChange = onChange;
        this.parent = parent;
    }

    public <A,B> StatType.Instance<A,B> addStat(String key, StatType<A,B> type) { return this.addStat(key, type.create()); }
    public <A,B> StatType.Instance<A,B> addStat(StatKey<A,B> key) { return this.addStat(key.key,key.type); }
    public <A,B> StatType.Instance<A,B> addStat(String key, StatType.Instance<A,B> stat)
    {
        this.stats.put(key,stat);
        stat.setParent(this);
        return stat;
    }

    public void clear() { this.clear(false); }
    public void clear(boolean fullClear) {
        if(fullClear)
            this.stats.clear();
        else
            this.stats.forEach((key,stat) -> stat.clear());
        this.setChanged();
    }

    public void setChanged() { this.onChange.run(); }

    public Tag save(DataContext<Tag> context)
    {
        return context.write(this.stats,CODEC);
    }

    public void load(CompoundTag tag,DataContext<Tag> context)
    {
        this.load(context.read(tag,CODEC));
    }

    public void load(Map<String,StatType.Instance<?,?>> newData) {
        this.stats.clear();
        newData.forEach(this::addStat);
    }

    private static Map<String,StatType.Instance<?,?>> loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        //Clear all stats before loading, just in case they're not present in the existing data
        Map<String,StatType.Instance<?,?>> stats = new HashMap<>();
        for(String key : tag.getAllKeys())
        {
            try {
                CompoundTag entry = tag.getCompound(key);
                ResourceLocation typeID = ResourceLocation.parse(entry.getString("Type"));
                StatType<?,?> type = LCRegistries.STAT_TYPES.get(typeID);
                if(type == null)
                    throw new RuntimeException(typeID + " is not a registered StatType!");
                StatType.Instance<?,?> instance = type.create();
                instance.loadOldData(entry, lookup);
                stats.put(key,instance);
            } catch (Throwable t) {LightmansCurrency.LogError("Error loading stat!",t);}
        }
        return stats;
    }

    public Set<String> getKeys() { return this.stats.keySet(); }

    public StatType.Instance<?,?> getStat(String key) { return this.stats.get(key); }

    public <T> T getStat(StatKey<T,?> key, T defaultValue) {
        StatType.Instance<?,?> instance = this.getStat(key.key);
        if(instance == null)
            instance = this.addStat(key);
        try {
            StatType.Instance<T,?> i = (StatType.Instance<T,?>)instance;
            return i.get();
        }catch (Throwable t) { LightmansCurrency.LogError("Stat with key " + key + " is a different type than expected. Could not get!");}
        return defaultValue;
    }

    public <T> void incrementStat(StatKey<?,T> key, T addValue)
    {
        StatType.Instance<?,?> instance = this.getStat(key.key);
        if(instance == null)
            instance = this.addStat(key);
        try {
            StatType.Instance<?,T> i = (StatType.Instance<?, T>)instance;
            i.add(addValue);
        } catch (Throwable t) {
            LightmansCurrency.LogError("Stat with key " + key + " is a different type than expected. Could not increment!");
        }
    }

    public List<Component> getDisplayLines()
    {
        List<Component> result = new ArrayList<>();
        this.stats.forEach((key,stat) ->  result.add(stat.getInfoText(key)) );
        return result;
    }

}
