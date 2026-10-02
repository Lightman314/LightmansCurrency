package io.github.lightman314.lightmanscurrency.api.stats;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.IdentifierException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class StatisticsDataHolder implements StatHolder.Clearable {

    public static final Codec<StatisticsDataHolder> CODEC = Codec.unboundedMap(Identifier.CODEC,StatEntry.CODEC)
            .xmap(StatisticsDataHolder::new,d -> d.data);
    public static final StreamCodec<RegistryFriendlyByteBuf,StatisticsDataHolder> STREAM_CODEC = StreamHelper.map(Identifier.STREAM_CODEC,StatEntry.STREAM_CODEC)
            .map(StatisticsDataHolder::new,d -> d.data);

    private List<StatKey<?,?>> sortedKeyCache = null;
    private final Map<Identifier,StatEntry<?,?>> data = new HashMap<>();

    private FancyPacketMap.Listener listener = c -> {};

    public StatisticsDataHolder withListener(Runnable listener) { return this.withListener(c -> listener.run()); }
    public StatisticsDataHolder withListener(FancyPacketMap.Listener listener) {
        this.listener = listener;
        return this;
    }

    private void setChanged(Consumer<FancyPacketMap.Mutable> change) { this.listener.accept(change); }
    private void setEntryChanged(StatKey<?,?> key) {
        this.setChanged(p -> {
            Identifier k = key.key();
            StatEntry<?,?> entry = this.data.get(k);
            if(entry != null)
                p.set(k.toString(),LCFancyPacketTypes.STAT_ENTRY,entry);
            else
                p.addToList("removed",LCFancyPacketTypes.ID,k);
        });
    }

    public StatisticsDataHolder() { }
    private StatisticsDataHolder(Map<Identifier,StatEntry<?,?>> data) {
        this.data.putAll(data);
    }

    public void copyFrom(StatisticsDataHolder other) {
        this.data.clear();
        other.data.forEach((k,e) -> this.data.put(k,e.copy()));
    }

    public void writePacket(FancyPacketMap.Mutable packet) {
        this.data.forEach((k,e) -> packet.set(k.toString(),LCFancyPacketTypes.STAT_ENTRY,e));
    }

    @Override
    public Set<StatKey<?,?>> getKeys() { return this.data.values().stream().map(StatEntry::getKey).collect(Collectors.toSet()); }

    @Override
    public List<StatKey<?, ?>> getSortedKeys() {
        if(this.sortedKeyCache == null) {
            List<StatKey<?,?>> list = new ArrayList<>(this.data.values().stream().map(StatEntry::getKey).toList());
            list.sort(StatKey.SORTER);
            this.sortedKeyCache = List.copyOf(list);
        }
        return this.sortedKeyCache;
    }

    @Override
    public <V,T> void addToStat(StatKey<V,T> key,T addValue) {
        StatEntry<?,?> e = this.data.get(key.key());
        if(e == null) {
            this.sortedKeyCache = null;
            e = new StatEntry<>(key);
            this.data.put(key.key(),e);
        }
        try {
            StatEntry<V,T> entry = (StatEntry<V,T>)e;
            entry.add(addValue);
        } catch (ClassCastException ignored) {
            LightmansCurrency.LogError("Stat with key " + key + " is a different type than expected. Could not add to it!");
        }
        this.setEntryChanged(key);
    }

    @Override
    public void resetStat(StatKey<?,?> key,boolean fullClear) {
        if(fullClear) {
            if(this.data.remove(key.key()) != null) {
                this.setEntryChanged(key);
                this.sortedKeyCache = null;
            }
        }
        else if(this.data.containsKey(key.key())) {
            if(this.data.get(key.key()).reset())
                this.setEntryChanged(key);
        }
    }

    @Override
    public <T> T getStat(StatKey<T,?> key) {
        StatEntry<?,?> entry = this.data.get(key.key());
        if(entry != null) {
            Object value = entry.getValue();
            if(key.isValidValue(value))
                return (T)value;
        }
        return key.type().getEmptyValue();
    }

    @Override
    public Component getStatValueText(StatKey<?,?> key) {
        StatEntry<?,?> entry = this.data.get(key.key());
        if(entry != null)
            return entry.getValueText();
        return new StatEntry<>(key).getValueText();
    }

    @Override
    @Nullable
    public List<Component> getStatValueTooltip(StatKey<?, ?> key) {
        StatEntry<?,?> entry = this.data.get(key.key());
        if(entry != null)
            return entry.getValueTooltip();
        return new StatEntry<>(key).getValueTooltip();
    }

    public void handlePacket(FancyPacketMap packet) {
        for(Identifier removed : packet.getList("removed",LCFancyPacketTypes.ID))
            this.data.remove(removed);
        for(String key : packet.keySet()) {
            if(key.equals("removed"))
                continue;
            try {
                StatEntry<?,?> entry = packet.get(key,LCFancyPacketTypes.STAT_ENTRY);
                if(entry != null) {
                    Identifier id = Identifier.parse(key);
                    this.data.put(id,entry);
                    LightmansCurrency.LogDebug("Updated value of " + id + " with " + entry.getValueText().getString());
                }
            } catch (IdentifierException ignored) { }
        }
    }

}
