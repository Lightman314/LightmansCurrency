package io.github.lightman314.lightmanscurrency.api.settings.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.util.TriConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class SavedSettingData
{

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

    public NodeAccess getNode(String node) { return new NodeAccess(this,node); }

    public Mutable makeMutable() { return new Mutable(this.boolData,this.intData,this.floatData,this.stringData,copyTags(this.tagData)); }

    //1.20.1
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

    public JsonObject write()
    {
        JsonObject json = new JsonObject();
        if(!this.boolData.isEmpty())
            json.add("booleans",writeMap(this.boolData,JsonObject::addProperty));
        if(!this.intData.isEmpty())
            json.add("integers",writeMap(this.intData,JsonObject::addProperty));
        if(!this.floatData.isEmpty())
            json.add("floats",writeMap(this.floatData,JsonObject::addProperty));
        if(!this.stringData.isEmpty())
            json.add("strings",writeMap(this.stringData,JsonObject::addProperty));
        if(!this.tagData.isEmpty())
            json.add("compounds",writeMap(this.tagData,(e,k,tag) -> e.addProperty(k,tag.getAsString())));
        return json;
    }

    private static <T> JsonObject writeMap(Map<String,T> map, TriConsumer<JsonObject,String,T> writer)
    {
        JsonObject entry = new JsonObject();
        map.forEach((key,val) -> writer.accept(entry,key,val));
        return entry;
    }
    
    public static SavedSettingData parse(CompoundTag tag)
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

    public static SavedSettingData read(JsonElement element) throws JsonSyntaxException
    {
        JsonObject json = GsonHelper.convertToJsonObject(element,"root");
        Map<String,Boolean> boolData = new HashMap<>();
        Map<String,Long> intData = new HashMap<>();
        Map<String,Double> floatData = new HashMap<>();
        Map<String,String> stringData = new HashMap<>();
        Map<String,CompoundTag> tagData = new HashMap<>();
        if(json.has("booleans"))
            readMap(GsonHelper.getAsJsonObject(json,"booleans"),GsonHelper::getAsBoolean,boolData);
        if(json.has("integers"))
            readMap(GsonHelper.getAsJsonObject(json,"integers"),GsonHelper::getAsLong,intData);
        if(json.has("floats"))
            readMap(GsonHelper.getAsJsonObject(json,"floats"),GsonHelper::getAsDouble,floatData);
        if(json.has("strings"))
            readMap(GsonHelper.getAsJsonObject(json,"strings"),GsonHelper::getAsString,stringData);
        if(json.has("compounds"))
            readMap(GsonHelper.getAsJsonObject(json,"compounds"),(j,k) -> {
                try {
                    return TagParser.parseTag(GsonHelper.getAsString(j,k));
                } catch (CommandSyntaxException e) { throw new JsonSyntaxException(e); }
            },tagData);
        return new SavedSettingData(boolData,intData,floatData,stringData,tagData);
    }

    private static <T> Map<String,T> readMap(JsonObject json, BiFunction<JsonObject,String,T> reader,Map<String,T> map) throws JsonSyntaxException
    {
        for(String key : json.keySet())
            map.put(key,reader.apply(json,key));
        return map;
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

        public NodeAccess forSubNode(String subNode) {
            if(subNode.isEmpty())
                return this;
            return new NodeAccess(this.data,this.node + "." + subNode);
        }

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
        
        public CompoundTag getCompoundValue(String tag) { return this.data.tagData.getOrDefault(this.node + tag,new CompoundTag()); }

    }

    public static final class Mutable
    {

        private final Map<String,Boolean> boolData;
        private final Map<String,Long> intData;
        private final Map<String,Double> floatData;
        private final Map<String,String> stringData;
        private final Map<String,CompoundTag> tagData;

        public MutableNodeAccess getNode(String node) { return new MutableNodeAccess(this,node); }

        private Mutable(Map<String,Boolean> boolData, Map<String,Long> intData,Map<String,Double> floatData, Map<String,String> stringData, Map<String,CompoundTag> tagData)
        {
            this.boolData = new HashMap<>(boolData);
            this.intData = new HashMap<>(intData);
            this.floatData = new HashMap<>(floatData);
            this.stringData = new HashMap<>(stringData);
            this.tagData = new HashMap<>(tagData);
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
        private MutableNodeAccess(Mutable data,String node) { this.data = data; this.node = node + "."; }

        public MutableNodeAccess forSubNode(String subNode) { return new MutableNodeAccess(this.data,this.node + "." + subNode); }

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
        
        public CompoundTag getCompoundValue(String tag) { return this.data.tagData.getOrDefault(this.node + tag,new CompoundTag()); }

        public void setBooleanValue(String tag, boolean value) { this.data.boolData.put(this.node + tag,value); }
        public void setIntValue(String tag, int value) { this.data.intData.put(this.node + tag,(long)value); }
        public void setLongValue(String tag, long value) { this.data.intData.put(this.node + tag,value); }
        public void setFloatValue(String tag, float value) { this.data.floatData.put(this.node + tag,(double)value); }
        public void setDoubleValue(String tag, double value) { this.data.floatData.put(this.node + tag,value); }
        public void setStringValue(String tag, String value) { this.data.stringData.put(this.node + tag,value); }
        public void setCompoundValue(String tag, CompoundTag value) { this.data.tagData.put(this.node + tag, value.copy()); }

    }

    private static Map<String,CompoundTag> copyTags(Map<String,CompoundTag> original)
    {
        Map<String,CompoundTag> copy = new HashMap<>();
        original.forEach((key,tag) -> copy.put(key,tag.copy()));
        return copy;
    }

}