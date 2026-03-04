package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class UpgradeData
{

    public static final Codec<UpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(Codec.STRING,Codec.BOOL).fieldOf("booleans").forGetter(data -> data.boolData),
                    Codec.unboundedMap(Codec.STRING,Codec.LONG).fieldOf("integers").forGetter(data -> data.intData),
                    Codec.unboundedMap(Codec.STRING,Codec.DOUBLE).fieldOf("floats").forGetter(data -> data.floatData),
                    Codec.unboundedMap(Codec.STRING,Codec.STRING).fieldOf("strings").forGetter(data -> data.stringData),
                    Codec.unboundedMap(Codec.STRING,CompoundTag.CODEC).fieldOf("compounds").forGetter(data -> data.tagData)
                    ).apply(builder,UpgradeData::new)
            );

    public static final StreamCodec<FriendlyByteBuf,UpgradeData> STREAM_CODEC = StreamCodec.of(
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
                return new UpgradeData(boolData,intData,floatData,stringData,tagData);
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
    private UpgradeData(Map<String,Boolean> boolData, Map<String,Long> intData,Map<String,Double> floatData, Map<String,String> stringData, Map<String,CompoundTag> tagData)
    {
        this.boolData = ImmutableMap.copyOf(boolData);
        this.intData = ImmutableMap.copyOf(intData);
        this.floatData = ImmutableMap.copyOf(floatData);
        this.stringData = ImmutableMap.copyOf(stringData);
        this.tagData = ImmutableMap.copyOf(tagData);
    }

    public boolean isEmpty() { return this.boolData.isEmpty() && this.intData.isEmpty() && this.floatData.isEmpty() && this.stringData.isEmpty() && this.tagData.isEmpty(); }

    public boolean hasBoolValue(String tag) { return this.boolData.containsKey(tag); }
    public boolean hasIntValue(String tag) { return this.intData.containsKey(tag); }
    public boolean hasLongValue(String tag) { return this.intData.containsKey(tag); }
    public boolean hasFloatValue(String tag) { return this.floatData.containsKey(tag); }
    public boolean hasDoubleValue(String tag) { return this.floatData.containsKey(tag); }
    public boolean hasStringValue(String tag) { return this.stringData.containsKey(tag); }
    public boolean hasCompoundValue(String tag) { return this.tagData.containsKey(tag); }

    public boolean getBooleanValue(String tag) { return this.boolData.getOrDefault(tag,false); }
    public int getIntValue(String tag) { return this.intData.getOrDefault(tag,0L).intValue(); }
    public long getLongValue(String tag) { return this.intData.getOrDefault(tag,0L); }
    public float getFloatValue(String tag) { return this.floatData.getOrDefault(tag,0d).floatValue(); }
    public double getDoubleValue(String tag) { return this.floatData.getOrDefault(tag,0d); }
    
    public String getStringValue(String tag)  { return this.stringData.getOrDefault(tag,""); }
    
    public CompoundTag getCompoundValue(String tag) { return this.tagData.getOrDefault(tag,new CompoundTag()); }

    
    public Mutable makeMutable() { return new Mutable(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UpgradeData other)
            return this.boolData.equals(other.boolData) && this.intData.equals(other.intData) && this.floatData.equals(other.floatData) && this.stringData.equals(other.stringData) && this.tagData.equals(other.tagData);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.boolData,this.intData,this.floatData,this.stringData,this.tagData); }

    public static final class Mutable
    {

        private final Map<String,Boolean> boolData;
        private final Map<String,Long> intData;
        private final Map<String,Double> floatData;
        private final Map<String,String> stringData;
        private final Map<String,CompoundTag> tagData;

        private Mutable(Map<String,Boolean> boolData, Map<String,Long> intData,Map<String,Double> floatData, Map<String,String> stringData, Map<String,CompoundTag> tagData)
        {
            this.boolData = new HashMap<>(boolData);
            this.intData = new HashMap<>(intData);
            this.floatData = new HashMap<>(floatData);
            this.stringData = new HashMap<>(stringData);
            this.tagData = new HashMap<>(tagData);
        }

        public boolean hasBoolValue(String tag) { return this.boolData.containsKey(tag); }
        public boolean hasIntValue(String tag) { return this.intData.containsKey(tag); }
        public boolean hasLongValue(String tag) { return this.intData.containsKey(tag); }
        public boolean hasFloatValue(String tag) { return this.floatData.containsKey(tag); }
        public boolean hasDoubleValue(String tag) { return this.floatData.containsKey(tag); }
        public boolean hasStringValue(String tag) { return this.stringData.containsKey(tag); }
        public boolean hasCompoundValue(String tag) { return this.tagData.containsKey(tag); }

        public boolean getBooleanValue(String tag) { return this.boolData.getOrDefault(tag,false); }
        public int getIntValue(String tag) { return this.intData.getOrDefault(tag,0L).intValue(); }
        public long getLongValue(String tag) { return this.intData.getOrDefault(tag,0L); }
        public float getFloatValue(String tag) { return this.floatData.getOrDefault(tag,0d).floatValue(); }
        public double getDoubleValue(String tag) { return this.floatData.getOrDefault(tag,0d); }
        
        public String getStringValue(String tag)  { return this.stringData.getOrDefault(tag,""); }
        
        public CompoundTag getCompoundValue(String tag) { return this.tagData.getOrDefault(tag,new CompoundTag()); }

        public void setBooleanValue(String tag, boolean value) { this.boolData.put(tag,value); }
        public void setIntValue(String tag, int value) { this.intData.put(tag,(long)value); }
        public void setLongValue(String tag, long value) { this.intData.put(tag,value); }
        public void setFloatValue(String tag, float value) { this.floatData.put(tag,(double)value); }
        public void setDoubleValue(String tag, double value) { this.floatData.put(tag,value); }
        public void setStringValue(String tag, String value) { this.stringData.put(tag,value); }
        public void setCompoundValue(String tag, CompoundTag value) { this.tagData.put(tag, value.copy()); }

        public void merge(UpgradeData data)
        {
            data.boolData.forEach(this::setBooleanValue);
            data.intData.forEach(this::setLongValue);
            data.floatData.forEach(this::setDoubleValue);
            data.stringData.forEach(this::setStringValue);
            data.tagData.forEach(this::setCompoundValue);
        }

        public UpgradeData makeImmutable() { return new UpgradeData(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    }

    private static Map<String,CompoundTag> copyTags(Map<String,CompoundTag> original)
    {
        Map<String,CompoundTag> copy = new HashMap<>();
        original.forEach((key,tag) -> copy.put(key,tag.copy()));
        return copy;
    }

}