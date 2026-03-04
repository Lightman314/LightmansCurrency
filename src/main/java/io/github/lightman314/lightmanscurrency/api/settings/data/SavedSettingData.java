package io.github.lightman314.lightmanscurrency.api.settings.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SavedSettingData
{

    public static final Codec<SavedSettingData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(Codec.STRING,Codec.BOOL).fieldOf("booleans").forGetter(data -> data.boolData),
                    Codec.unboundedMap(Codec.STRING,Codec.LONG).fieldOf("integers").forGetter(data -> data.intData),
                    Codec.unboundedMap(Codec.STRING,Codec.DOUBLE).fieldOf("floats").forGetter(data -> data.floatData),
                    Codec.unboundedMap(Codec.STRING,Codec.STRING).fieldOf("strings").forGetter(data -> data.stringData),
                    Codec.unboundedMap(Codec.STRING,CompoundTag.CODEC).fieldOf("compounds").forGetter(data -> data.tagData)
            ).apply(builder,SavedSettingData::new)
    );

    public static final StreamCodec<FriendlyByteBuf,SavedSettingData> STREAM_CODEC = StreamCodec.of(
            (b,d) -> {
                encodeMap(b,d.boolData,FriendlyByteBuf::writeBoolean);
                encodeMap(b,d.intData,FriendlyByteBuf::writeLong);
                encodeMap(b,d.floatData,FriendlyByteBuf::writeDouble);
                encodeMap(b,d.stringData,FriendlyByteBuf::writeUtf);
                encodeMap(b,d.tagData,(a,t) -> a.writeNbt(t));
            },
            (b) -> {
                Map<String,Boolean> boolData = decodeMap(b,FriendlyByteBuf::readBoolean);
                Map<String,Long> intData = decodeMap(b,FriendlyByteBuf::readLong);
                Map<String,Double> floatData = decodeMap(b,FriendlyByteBuf::readDouble);
                Map<String,String> stringData = decodeMap(b,FriendlyByteBuf::readUtf);
                Map<String,CompoundTag> tagData = decodeMap(b,a -> a.readNbt());
                return new SavedSettingData(boolData,intData,floatData,stringData,tagData);
            }
    );

    private static <T> void encodeMap(FriendlyByteBuf buffer, Map<String,T> map, BiConsumer<FriendlyByteBuf,T> encoder)
    {
        buffer.writeInt(map.size());
        map.forEach((key,val) -> {
            buffer.writeUtf(key);
            encoder.accept(buffer,val);
        });
    }

    private static <T> Map<String,T> decodeMap(FriendlyByteBuf buffer, Function<FriendlyByteBuf,T> decoder)
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

    private DataContext<Tag> context;
    public void withContext(DataContext<Tag> context) { this.context = context; }
    public void withContext(HolderLookup.Provider lookup) { this.context = DataContext.createNBT(lookup); }

    private SavedSettingData() {
        this.boolData = ImmutableMap.of();
        this.intData = ImmutableMap.of();
        this.floatData = ImmutableMap.of();
        this.stringData = ImmutableMap.of();
        this.tagData = ImmutableMap.of();
    }
    private SavedSettingData(Map<String,Boolean> boolData, Map<String,Long> intData, Map<String,Double> floatData, Map<String,String> stringData, Map<String,CompoundTag> tagData)
    {
        this.boolData = ImmutableMap.copyOf(boolData);
        this.intData = ImmutableMap.copyOf(intData);
        this.floatData = ImmutableMap.copyOf(floatData);
        this.stringData = ImmutableMap.copyOf(stringData);
        this.tagData = ImmutableMap.copyOf(tagData);
    }

    public boolean hasNode(String node) { return !this.getNode(node).isEmpty(); }

    public NodeAccess getNode(String node) { return new NodeAccess(this,node,this.context); }

    public Mutable makeMutable(DataContext<Tag> context) { return new Mutable(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData),context); }
    public Mutable makeMutable(HolderLookup.Provider lookup) { return this.makeMutable(DataContext.createNBT(lookup)); }

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
        private final DataContext<Tag> context;
        private NodeAccess(SavedSettingData data, String node, @Nullable DataContext<Tag> context) { this.data = data; this.node = node + "."; this.context = context; }

        public NodeAccess forSubNode(String subNode) { return new NodeAccess(this.data,this.node + "." + subNode,this.context); }

        private boolean hasNodeEntry(Map<String,?> map) { return SavedSettingData.hasNodeEntry(map,this.node); }

        public boolean isEmpty() {
            return !this.hasNodeEntry(this.data.boolData) && !this.hasNodeEntry(this.data.intData) && !this.hasNodeEntry(this.data.floatData) && !this.hasNodeEntry(this.data.stringData) && !this.hasNodeEntry(this.data.tagData);
        }

        public boolean hasBoolValue(String tag) { return this.data.boolData.containsKey(this.node + tag); }
        public boolean hasIntValue(String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasLongValue(String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasFloatValue(String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasDoubleValue(String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasStringValue(String tag) { return this.data.stringData.containsKey(this.node + tag); }
        public boolean hasCompoundValue(String tag) { return this.data.tagData.containsKey(this.node + tag); }

        public boolean getBooleanValue(String tag) { return this.data.boolData.getOrDefault(this.node + tag,false); }
        public int getIntValue(String tag) { return this.data.intData.getOrDefault(this.node + tag,0L).intValue(); }
        public long getLongValue(String tag) { return this.data.intData.getOrDefault(this.node + tag,0L); }
        public float getFloatValue(String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d).floatValue(); }
        public double getDoubleValue(String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d); }
        
        public String getStringValue(String tag)  { return this.data.stringData.getOrDefault(this.node + tag,""); }

        @Nullable
        public <T extends Enum<T>> T getEnumValue(String tag,Class<T> clazz) { return this.getEnumValue(tag,clazz,null); }
        public <T extends Enum<T>> T getEnumValue(String tag,Class<T> clazz, @Nullable T defaultValue) { return EnumUtil.enumFromOrdinal(this.getIntValue(tag),clazz.getEnumConstants(),defaultValue); }

        public CompoundTag getCompoundValue(String tag) { return this.data.tagData.getOrDefault(this.node + tag,new CompoundTag()); }
        @Nullable
        public <T> T getCustomValue(String tag,Codec<T> codec) { return this.getCustomValue(tag,codec,null); }
        @Contract("_,_,null -> null;_,_,!null -> !null")
        @Nullable
        public <T> T getCustomValue(String tag,Codec<T> codec,@Nullable T defaultValue) {
            if(this.context == null)
                return defaultValue;
            try { return codec.decode(this.context.ops(),this.getCompoundValue(tag)).getOrThrow().getFirst();
            } catch (IllegalStateException ignored) { return defaultValue; }
        }
        public <T> List<T> getCustomList(String tag,Codec<T> codec) { return this.getCustomList(tag,codec,new ArrayList<>()); }
        public <T> List<T> getCustomList(String tag,Codec<T> codec,String fallback) { return this.getCustomList(tag,codec,new ArrayList<>()); }
        public <T> List<T> getCustomList(String tag,Codec<T> codec,List<T> defaultValue) { return this.getCustomList(tag,codec,null,defaultValue); }
        public <T> List<T> getCustomList(String tag,Codec<T> codec,@Nullable String fallback,List<T> defaultValue) {
            CompoundTag entry = this.getCompoundValue(tag);
            if(entry.contains("list",Tag.TAG_LIST))
            {
                try { return new ArrayList<>(codec.listOf().decode(this.context.ops(),entry.get("list")).getOrThrow().getFirst());
                } catch (IllegalStateException ignored) { }
            }
            else if(fallback != null && entry.contains(fallback,Tag.TAG_LIST))
            {
                try { return new ArrayList<>(codec.listOf().decode(this.context.ops(),entry.get(fallback)).getOrThrow().getFirst());
                } catch (IllegalStateException ignored) { }
            }
            return defaultValue;
        }
        //Easy way to get a custom lists size for simple count displays, without needing to waste time actually parsing the data
        public int getCustomListSize(String tag) { return this.getCustomListSize(tag,null); }
        public int getCustomListSize(String tag,@Nullable String fallback) {
            CompoundTag entry = this.getCompoundValue(tag);
            if(entry.get("list") instanceof ListTag list)
                return list.size();
            if(fallback != null && entry.get(fallback) instanceof ListTag list)
                return list.size();
            return 0;
        }

    }

    public static final class Mutable
    {

        private final Map<String,Boolean> boolData;
        private final Map<String,Long> intData;
        private final Map<String,Double> floatData;
        private final Map<String,String> stringData;
        private final Map<String,CompoundTag> tagData;

        private final DataContext<Tag> context;

        public MutableNodeAccess getNode(String node) { return new MutableNodeAccess(this,node,context); }

        private Mutable(Map<String,Boolean> boolData, Map<String,Long> intData,Map<String,Double> floatData, Map<String,String> stringData, Map<String,CompoundTag> tagData,DataContext<Tag> context)
        {
            this.boolData = new HashMap<>(boolData);
            this.intData = new HashMap<>(intData);
            this.floatData = new HashMap<>(floatData);
            this.stringData = new HashMap<>(stringData);
            this.tagData = new HashMap<>(tagData);
            this.context = context;
        }

        public void merge(SavedSettingData data)
        {
            this.boolData.putAll(data.boolData);
            this.intData.putAll(data.intData);
            this.floatData.putAll(data.floatData);
            this.stringData.putAll(data.stringData);
            this.tagData.putAll(copyTags(data.tagData));
        }

        public SavedSettingData makeImmutable() { return new SavedSettingData(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    }

    public static final class MutableNodeAccess
    {
        private final Mutable data;
        private final String node;
        private final DataContext<Tag> context;
        private MutableNodeAccess(Mutable data,String node,DataContext<Tag> context) { this.data = data; this.node = node + "."; this.context = context; }

        public MutableNodeAccess forSubNode(String subNode) {
            if(subNode.isEmpty())
                return this;
            return new MutableNodeAccess(this.data,this.node + "." + subNode,this.context);
        }

        public boolean hasBoolValue(String tag) { return this.data.boolData.containsKey(this.node + tag); }
        public boolean hasIntValue(String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasLongValue(String tag) { return this.data.intData.containsKey(this.node + tag); }
        public boolean hasFloatValue(String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasDoubleValue(String tag) { return this.data.floatData.containsKey(this.node + tag); }
        public boolean hasStringValue(String tag) { return this.data.stringData.containsKey(this.node + tag); }
        public boolean hasCompoundValue(String tag) { return this.data.tagData.containsKey(this.node + tag); }
        public boolean hasCustomValue(String tag) { return this.hasCompoundValue(tag); }

        public boolean getBooleanValue(String tag) { return this.data.boolData.getOrDefault(this.node + tag,false); }
        public int getIntValue(String tag) { return this.data.intData.getOrDefault(this.node + tag,0L).intValue(); }
        public long getLongValue(String tag) { return this.data.intData.getOrDefault(this.node + tag,0L); }
        public float getFloatValue(String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d).floatValue(); }
        public double getDoubleValue(String tag) { return this.data.floatData.getOrDefault(this.node + tag,0d); }
        
        public String getStringValue(String tag)  { return this.data.stringData.getOrDefault(this.node + tag,""); }
        
        public CompoundTag getCompoundValue(String tag) { return this.data.tagData.getOrDefault(this.node + tag,new CompoundTag()); }

        public void setBooleanValue(String tag, boolean value) { this.data.boolData.put(this.node + tag,value); }
        public void setIntValue(String tag, int value) { this.data.intData.put(this.node + tag,(long)value); }
        public void setLongValue(String tag, long value) { this.data.intData.put(this.node + tag,value); }
        public void setFloatValue(String tag, float value) { this.data.floatData.put(this.node + tag,(double)value); }
        public void setDoubleValue(String tag, double value) { this.data.floatData.put(this.node + tag,value); }
        public void setStringValue(String tag, String value) { this.data.stringData.put(this.node + tag,value); }

        public <T extends Enum<T>> void setEnumValue(String tag,T value) { this.setIntValue(tag,value.ordinal()); }

        public void setCompoundValue(String tag, CompoundTag value) { this.data.tagData.put(this.node + tag, value.copy()); }
        public <T> void setCustom(String tag,T value,Codec<T> codec) { this.setCompoundValue(tag,(CompoundTag)codec.encodeStart(this.context.ops(),value).getOrThrow());}
        public <T> void setCustomList(String tag,List<T> value, Codec<T> codec) {
            CompoundTag entry = new CompoundTag();
            ListTag list = (ListTag)codec.listOf().encodeStart(this.context.ops(),value).getOrThrow();
            entry.put("list",list);
            this.setCompoundValue(tag,entry);
        }
    }

    private static Map<String,CompoundTag> copyTags(Map<String,CompoundTag> original)
    {
        Map<String,CompoundTag> copy = new HashMap<>();
        original.forEach((key,tag) -> copy.put(key,tag.copy()));
        return copy;
    }

}