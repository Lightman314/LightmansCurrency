package io.github.lightman314.lightmanscurrency.api.settings.data;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SavedSettingData
{

    private static <T> void encodeMap(@Nonnull FriendlyByteBuf buffer, @Nonnull Map<String,T> map, @Nonnull BiConsumer<FriendlyByteBuf,T> encoder)
    {
        buffer.writeInt(map.size());
        map.forEach((key,val) -> {
            buffer.writeUtf(key);
            encoder.accept(buffer,val);
        });
    }

    private static <T> Map<String,T> decodeMap(@Nonnull FriendlyByteBuf buffer, @Nonnull Function<FriendlyByteBuf,T> decoder)
    {
        Map<String,T> map = new HashMap<>();
        int count = buffer.readInt();
        for(int i = 0; i < count; ++i)
        {
            String key = buffer.readUtf();
            T val = decoder.apply(buffer);
            map.put(key,val);
        }
        return map;
    }

    public static final SavedSettingData EMPTY = new SavedSettingData();

    private final Map<String,Boolean> boolData;
    private final Map<String,Long> intData;
    private final Map<String,Double> floatData;
    private final Map<String,String> stringData;
    private final Map<String,CompoundTag> tagData;

    private SavedSettingData() {
        this.boolData = ImmutableMap.of();
        this.intData = ImmutableMap.of();
        this.floatData = ImmutableMap.of();
        this.stringData = ImmutableMap.of();
        this.tagData = ImmutableMap.of();
    }
    private SavedSettingData(@Nonnull Map<String,Boolean> boolData, @Nonnull Map<String,Long> intData, @Nonnull Map<String,Double> floatData, @Nonnull Map<String,String> stringData, @Nonnull Map<String,CompoundTag> tagData)
    {
        this.boolData = ImmutableMap.copyOf(boolData);
        this.intData = ImmutableMap.copyOf(intData);
        this.floatData = ImmutableMap.copyOf(floatData);
        this.stringData = ImmutableMap.copyOf(stringData);
        this.tagData = ImmutableMap.copyOf(tagData);
    }

    public boolean hasNode(String node) { return !this.getNode(node).isEmpty(); }

    public NodeAccess getNode(String node) { return new NodeAccess(this,node); }

    @Nonnull
    public Mutable makeMutable() { return new Mutable(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    //1.20.1
    @Nonnull
    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        if(!this.boolData.isEmpty())
            tag.put("booleans", UpgradeData.writeMap(this.boolData,(b,t) -> t.putBoolean("value",b)));
        if(!this.intData.isEmpty())
            tag.put("integers", UpgradeData.writeMap(this.intData,(i,t) -> t.putLong("value",i)));
        if(!this.floatData.isEmpty())
            tag.put("floats", UpgradeData.writeMap(this.floatData,(f,t) -> t.putDouble("value",f)));
        if(!this.stringData.isEmpty())
            tag.put("strings", UpgradeData.writeMap(this.stringData,(s,t) -> t.putString("value",s)));
        if(!this.tagData.isEmpty())
            tag.put("compounds", UpgradeData.writeMap(this.tagData,(v,t) -> t.put("value",v)));
        return tag;
    }

    @Nonnull
    public static SavedSettingData parse(@Nonnull CompoundTag tag)
    {
        Map<String,Boolean> boolData = new HashMap<>();
        Map<String,Long> intData = new HashMap<>();
        Map<String,Double> floatData = new HashMap<>();
        Map<String,String> stringData = new HashMap<>();
        Map<String,CompoundTag> tagData = new HashMap<>();
        if(tag.contains("booleans", Tag.TAG_LIST))
            UpgradeData.readMap(boolData,tag.getList("booleans",Tag.TAG_COMPOUND),t -> t.getBoolean("value"));
        if(tag.contains("integers",Tag.TAG_LIST))
            UpgradeData.readMap(intData,tag.getList("integers",Tag.TAG_COMPOUND),t -> t.getLong("value"));
        if(tag.contains("floats",Tag.TAG_LIST))
            UpgradeData.readMap(floatData,tag.getList("floats",Tag.TAG_COMPOUND),t -> t.getDouble("value"));
        if(tag.contains("strings",Tag.TAG_LIST))
            UpgradeData.readMap(stringData,tag.getList("strings",Tag.TAG_COMPOUND),t -> t.getString("value"));
        if(tag.contains("compounds",Tag.TAG_LIST))
            UpgradeData.readMap(tagData,tag.getList("compounds",Tag.TAG_COMPOUND),t -> t.getCompound("value"));
        return new SavedSettingData(boolData,intData,floatData,stringData,tagData);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SavedSettingData other)
            return this.boolData.equals(other.boolData) && this.intData.equals(other.intData) && this.floatData.equals(other.floatData) && this.stringData.equals(other.stringData) && this.tagData.equals(other.tagData);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.boolData,this.intData,this.floatData,this.stringData,this.tagData); }

    private static boolean hasNodeEntry(Map<String,?> map,String node) { return map.keySet().stream().anyMatch(key -> key.startsWith(node)); }

    public static final class NodeAccess
    {

        private final SavedSettingData data;
        private final String node;
        private NodeAccess(SavedSettingData data, String node) { this.data = data; this.node = node + "."; }

        public NodeAccess forSubNode(String subNode) { return new NodeAccess(this.data,this.node + "." + subNode); }

        private boolean hasNodeEntry(Map<String,?> map) { return SavedSettingData.hasNodeEntry(map,this.node); }

        public boolean isEmpty() {
            return !this.hasNodeEntry(this.data.boolData) && !this.hasNodeEntry(this.data.intData) && !this.hasNodeEntry(this.data.floatData) && !this.hasNodeEntry(this.data.stringData) && !this.hasNodeEntry(this.data.tagData);
        }

        public boolean hasBoolValue(@Nonnull String tag) { return this.data.boolData.containsKey(this.node + tag); }
        public boolean hasIntValue(@Nonnull String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasLongValue(@Nonnull String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasFloatValue(@Nonnull String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasDoubleValue(@Nonnull String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasStringValue(@Nonnull String tag) { return this.data.stringData.containsKey(this.node + tag); }
        public boolean hasCompoundValue(@Nonnull String tag) { return this.data.tagData.containsKey(this.node + tag); }

        public boolean getBooleanValue(@Nonnull String tag) { return this.data.boolData.getOrDefault(this.node + tag,false); }
        public int getIntValue(@Nonnull String tag) { return this.data.intData.getOrDefault(this.node + tag,0L).intValue(); }
        public long getLongValue(@Nonnull String tag) { return this.data.intData.getOrDefault(this.node + tag,0L); }
        public float getFloatValue(@Nonnull String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d).floatValue(); }
        public double getDoubleValue(@Nonnull String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d); }
        @Nonnull
        public String getStringValue(@Nonnull String tag)  { return this.data.stringData.getOrDefault(this.node + tag,""); }
        @Nonnull
        public CompoundTag getCompoundValue(@Nonnull String tag) { return this.data.tagData.getOrDefault(this.node + tag,new CompoundTag()); }

    }

    public static final class Mutable
    {

        private final Map<String,Boolean> boolData;
        private final Map<String,Long> intData;
        private final Map<String,Double> floatData;
        private final Map<String,String> stringData;
        private final Map<String,CompoundTag> tagData;

        public MutableNodeAccess getNode(String node) { return new MutableNodeAccess(this,node); }

        private Mutable(@Nonnull Map<String,Boolean> boolData, @Nonnull Map<String,Long> intData,@Nonnull Map<String,Double> floatData, @Nonnull Map<String,String> stringData, @Nonnull Map<String,CompoundTag> tagData)
        {
            this.boolData = new HashMap<>(boolData);
            this.intData = new HashMap<>(intData);
            this.floatData = new HashMap<>(floatData);
            this.stringData = new HashMap<>(stringData);
            this.tagData = new HashMap<>(tagData);
        }

        public void merge(@Nonnull SavedSettingData data)
        {
            this.boolData.putAll(data.boolData);
            this.intData.putAll(data.intData);
            this.floatData.putAll(data.floatData);
            this.stringData.putAll(data.stringData);
            this.tagData.putAll(copyTags(data.tagData));
        }

        @Nonnull
        public SavedSettingData makeImmutable() { return new SavedSettingData(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    }

    public static final class MutableNodeAccess
    {
        private final Mutable data;
        private final String node;
        private MutableNodeAccess(Mutable data,String node) { this.data = data; this.node = node + "."; }

        public MutableNodeAccess forSubNode(String subNode) { return new MutableNodeAccess(this.data,this.node + "." + subNode); }

        public boolean hasBoolValue(@Nonnull String tag) { return this.data.boolData.containsKey(this.node + tag); }
        public boolean hasIntValue(@Nonnull String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasLongValue(@Nonnull String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasFloatValue(@Nonnull String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasDoubleValue(@Nonnull String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasStringValue(@Nonnull String tag) { return this.data.stringData.containsKey(this.node + tag); }
        public boolean hasCompoundValue(@Nonnull String tag) { return this.data.tagData.containsKey(this.node + tag); }

        public boolean getBooleanValue(@Nonnull String tag) { return this.data.boolData.getOrDefault(this.node + tag,false); }
        public int getIntValue(@Nonnull String tag) { return this.data.intData.getOrDefault(this.node + tag,0L).intValue(); }
        public long getLongValue(@Nonnull String tag) { return this.data.intData.getOrDefault(this.node + tag,0L); }
        public float getFloatValue(@Nonnull String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d).floatValue(); }
        public double getDoubleValue(@Nonnull String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d); }
        @Nonnull
        public String getStringValue(@Nonnull String tag)  { return this.data.stringData.getOrDefault(this.node + tag,""); }
        @Nonnull
        public CompoundTag getCompoundValue(@Nonnull String tag) { return this.data.tagData.getOrDefault(this.node + tag,new CompoundTag()); }

        public void setBooleanValue(@Nonnull String tag, boolean value) { this.data.boolData.put(this.node + tag,value); }
        public void setIntValue(@Nonnull String tag, int value) { this.data.intData.put(this.node + tag,(long)value); }
        public void setLongValue(@Nonnull String tag, long value) { this.data.intData.put(this.node + tag,value); }
        public void setFloatValue(@Nonnull String tag, float value) { this.data.floatData.put(this.node + tag,(double)value); }
        public void setDoubleValue(@Nonnull String tag, double value) { this.data.floatData.put(this.node + tag,value); }
        public void setStringValue(@Nonnull String tag, @Nonnull String value) { this.data.stringData.put(this.node + tag,value); }
        public void setCompoundValue(@Nonnull String tag, @Nonnull CompoundTag value) { this.data.tagData.put(this.node + tag, value.copy()); }

    }

    private static Map<String,CompoundTag> copyTags(Map<String,CompoundTag> original)
    {
        Map<String,CompoundTag> copy = new HashMap<>();
        original.forEach((key,tag) -> copy.put(key,tag.copy()));
        return copy;
    }

}