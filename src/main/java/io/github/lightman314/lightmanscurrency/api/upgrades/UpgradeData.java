package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class UpgradeData
{

    public static final UpgradeData EMPTY = new UpgradeData();

    private final Map<String,Boolean> boolData;
    private final Map<String,Long> intData;
    private final Map<String,Double> floatData;
    private final Map<String,String> stringData;
    private final Map<String,CompoundTag> tagData;

    private UpgradeData() {
        this.boolData = ImmutableMap.of();
        this.intData = ImmutableMap.of();
        this.floatData = ImmutableMap.of();
        this.stringData = ImmutableMap.of();
        this.tagData = ImmutableMap.of();
    }
    private UpgradeData(@Nonnull Map<String,Boolean> boolData, @Nonnull Map<String,Long> intData,@Nonnull Map<String,Double> floatData, @Nonnull Map<String,String> stringData, @Nonnull Map<String,CompoundTag> tagData)
    {
        this.boolData = ImmutableMap.copyOf(boolData);
        this.intData = ImmutableMap.copyOf(intData);
        this.floatData = ImmutableMap.copyOf(floatData);
        this.stringData = ImmutableMap.copyOf(stringData);
        this.tagData = ImmutableMap.copyOf(tagData);
    }

    public boolean isEmpty() { return this.boolData.isEmpty() && this.intData.isEmpty() && this.floatData.isEmpty() && this.stringData.isEmpty() && this.tagData.isEmpty(); }

    public boolean hasBoolValue(@Nonnull String tag) { return this.boolData.containsKey(tag); }
    public boolean hasIntValue(@Nonnull String tag) { return this.intData.containsKey(tag); }
    public boolean hasLongValue(@Nonnull String tag) { return this.intData.containsKey(tag); }
    public boolean hasFloatValue(@Nonnull String tag) { return this.floatData.containsKey(tag); }
    public boolean hasDoubleValue(@Nonnull String tag) { return this.floatData.containsKey(tag); }
    public boolean hasStringValue(@Nonnull String tag) { return this.stringData.containsKey(tag); }
    public boolean hasCompoundValue(@Nonnull String tag) { return this.tagData.containsKey(tag); }

    public boolean getBooleanValue(@Nonnull String tag) { return this.boolData.getOrDefault(tag,false); }
    public int getIntValue(@Nonnull String tag) { return this.intData.getOrDefault(tag,0L).intValue(); }
    public long getLongValue(@Nonnull String tag) { return this.intData.getOrDefault(tag,0L); }
    public float getFloatValue(@Nonnull String tag) { return this.floatData.getOrDefault(tag,0d).floatValue(); }
    public double getDoubleValue(@Nonnull String tag) { return this.floatData.getOrDefault(tag,0d); }
    @Nonnull
    public String getStringValue(@Nonnull String tag)  { return this.stringData.getOrDefault(tag,""); }
    @Nonnull
    public CompoundTag getCompoundValue(@Nonnull String tag) { return this.tagData.getOrDefault(tag,new CompoundTag()); }

    @Nonnull
    public Mutable makeMutable() { return new Mutable(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UpgradeData other)
            return this.boolData.equals(other.boolData) && this.intData.equals(other.intData) && this.floatData.equals(other.floatData) && this.stringData.equals(other.stringData) && this.tagData.equals(other.tagData);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.boolData,this.intData,this.floatData,this.stringData,this.tagData); }

    //1.20.1
    @Nonnull
    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        if(!this.boolData.isEmpty())
            tag.put("booleans", writeMap(this.boolData,(b,t) -> t.putBoolean("value",b)));
        if(!this.intData.isEmpty())
            tag.put("integers", writeMap(this.intData,(i,t) -> t.putLong("value",i)));
        if(!this.floatData.isEmpty())
            tag.put("floats", writeMap(this.floatData,(f,t) -> t.putDouble("value",f)));
        if(!this.stringData.isEmpty())
            tag.put("strings", writeMap(this.stringData,(s,t) -> t.putString("value",s)));
        if(!this.tagData.isEmpty())
            tag.put("compounds", writeMap(this.tagData,(v,t) -> t.put("value",v)));
        return tag;
    }

    @Nonnull
    public static <T> ListTag writeMap(@Nonnull Map<String,T> map, @Nonnull BiConsumer<T,CompoundTag> valueWriter)
    {
        ListTag list = new ListTag();
        for(var entry : map.entrySet())
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("key",entry.getKey());
            valueWriter.accept(entry.getValue(),tag);
            list.add(tag);
        }
        return list;
    }

    @Nonnull
    public static UpgradeData parse(@Nonnull CompoundTag tag)
    {
        Map<String,Boolean> boolData = new HashMap<>();
        Map<String,Long> intData = new HashMap<>();
        Map<String,Double> floatData = new HashMap<>();
        Map<String,String> stringData = new HashMap<>();
        Map<String,CompoundTag> tagData = new HashMap<>();
        if(tag.contains("booleans",Tag.TAG_LIST))
            readMap(boolData,tag.getList("booleans",Tag.TAG_COMPOUND),t -> t.getBoolean("value"));
        if(tag.contains("integers",Tag.TAG_LIST))
            readMap(intData,tag.getList("integers",Tag.TAG_COMPOUND),t -> t.getLong("value"));
        if(tag.contains("floats",Tag.TAG_LIST))
            readMap(floatData,tag.getList("floats",Tag.TAG_COMPOUND),t -> t.getDouble("value"));
        if(tag.contains("strings",Tag.TAG_LIST))
            readMap(stringData,tag.getList("strings",Tag.TAG_COMPOUND),t -> t.getString("value"));
        if(tag.contains("compounds",Tag.TAG_LIST))
            readMap(tagData,tag.getList("compounds",Tag.TAG_COMPOUND),t -> t.getCompound("value"));
        return new UpgradeData(boolData,intData,floatData,stringData,tagData);
    }

    public static <T> void readMap(@Nonnull Map<String,T> map, @Nonnull ListTag list, @Nonnull Function<CompoundTag,T> valueReader)
    {
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag tag = list.getCompound(i);
            String key = tag.getString("key");
            T value = valueReader.apply(tag);
            map.put(key,value);
        }
    }

    public static final class Mutable
    {

        private final Map<String,Boolean> boolData;
        private final Map<String,Long> intData;
        private final Map<String,Double> floatData;
        private final Map<String,String> stringData;
        private final Map<String,CompoundTag> tagData;

        private Mutable(@Nonnull Map<String,Boolean> boolData, @Nonnull Map<String,Long> intData,@Nonnull Map<String,Double> floatData, @Nonnull Map<String,String> stringData, @Nonnull Map<String,CompoundTag> tagData)
        {
            this.boolData = new HashMap<>(boolData);
            this.intData = new HashMap<>(intData);
            this.floatData = new HashMap<>(floatData);
            this.stringData = new HashMap<>(stringData);
            this.tagData = new HashMap<>(tagData);
        }

        public boolean hasBoolValue(@Nonnull String tag) { return this.boolData.containsKey(tag); }
        public boolean hasIntValue(@Nonnull String tag) { return this.intData.containsKey(tag); }
        public boolean hasLongValue(@Nonnull String tag) { return this.intData.containsKey(tag); }
        public boolean hasFloatValue(@Nonnull String tag) { return this.floatData.containsKey(tag); }
        public boolean hasDoubleValue(@Nonnull String tag) { return this.floatData.containsKey(tag); }
        public boolean hasStringValue(@Nonnull String tag) { return this.stringData.containsKey(tag); }
        public boolean hasCompoundValue(@Nonnull String tag) { return this.tagData.containsKey(tag); }

        public boolean getBooleanValue(@Nonnull String tag) { return this.boolData.getOrDefault(tag,false); }
        public int getIntValue(@Nonnull String tag) { return this.intData.getOrDefault(tag,0L).intValue(); }
        public long getLongValue(@Nonnull String tag) { return this.intData.getOrDefault(tag,0L); }
        public float getFloatValue(@Nonnull String tag) { return this.floatData.getOrDefault(tag,0d).floatValue(); }
        public double getDoubleValue(@Nonnull String tag) { return this.floatData.getOrDefault(tag,0d); }
        @Nonnull
        public String getStringValue(@Nonnull String tag)  { return this.stringData.getOrDefault(tag,""); }
        @Nonnull
        public CompoundTag getCompoundValue(@Nonnull String tag) { return this.tagData.getOrDefault(tag,new CompoundTag()); }

        public void setBooleanValue(@Nonnull String tag, boolean value) { this.boolData.put(tag,value); }
        public void setIntValue(@Nonnull String tag, int value) { this.intData.put(tag,(long)value); }
        public void setLongValue(@Nonnull String tag, long value) { this.intData.put(tag,value); }
        public void setFloatValue(@Nonnull String tag, float value) { this.floatData.put(tag,(double)value); }
        public void setDoubleValue(@Nonnull String tag, double value) { this.floatData.put(tag,value); }
        public void setStringValue(@Nonnull String tag, @Nonnull String value) { this.stringData.put(tag,value); }
        public void setCompoundValue(@Nonnull String tag, @Nonnull CompoundTag value) { this.tagData.put(tag, value.copy()); }

        public void merge(@Nonnull UpgradeData data)
        {
            data.boolData.forEach(this::setBooleanValue);
            data.intData.forEach(this::setLongValue);
            data.floatData.forEach(this::setDoubleValue);
            data.stringData.forEach(this::setStringValue);
            data.tagData.forEach(this::setCompoundValue);
        }

        @Nonnull
        public UpgradeData makeImmutable() { return new UpgradeData(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    }

    private static Map<String,CompoundTag> copyTags(Map<String,CompoundTag> original)
    {
        Map<String,CompoundTag> copy = new HashMap<>();
        original.forEach((key,tag) -> copy.put(key,tag.copy()));
        return copy;
    }

}