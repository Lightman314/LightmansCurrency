package io.github.lightman314.lightmanscurrency.api.helpers.network;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public sealed class FancyPacketMap permits FancyPacketMap.Mutable {

    public static final StreamCodec<RegistryFriendlyByteBuf,FancyPacketMap> STREAM_CODEC = StreamCodec.of((buf,map) -> {
        buf.writeInt(map.dataMap.size());
        map.dataMap.forEach((key,entry) -> {
            buf.writeUtf(key);
            entry.encode(buf);
        });
    },buf -> {
        int count = buf.readInt();
        Map<String,Entry<?>> map = new HashMap<>();
        for(int i = 0; i < count; ++i)
            map.put(buf.readUtf(),Entry.decode(buf));
        return new FancyPacketMap(map);
    });

    public static final FancyPacketMap EMPTY = new FancyPacketMap(ImmutableMap.of());

    protected final Map<String,Entry<?>> dataMap;

    protected FancyPacketMap(Map<String,Entry<?>> data) { this.dataMap = this.copyMap(data); }

    protected Map<String,Entry<?>> copyMap(Map<String,Entry<?>> data)
    {
        ImmutableMap.Builder<String,Entry<?>> builder = ImmutableMap.builderWithExpectedSize(data.size());
        data.forEach((key,entry) -> builder.put(key,entry.copy()));
        return builder.build();
    }

    protected final Entry<?> getData(String key) { return this.dataMap.getOrDefault(key,Entry.NULL); }

    public boolean isEmpty() { return this.dataMap.isEmpty(); }
    public Set<String> keySet() { return this.dataMap.keySet(); }

    public boolean contains(String key) { return this.dataMap.containsKey(key); }
    public boolean contains(String key,FancyPacketType<?> type) { return this.contains(key) && this.getData(key).type() == type; }

    @Nullable
    public <T> T get(String key,Supplier<FancyPacketType<T>> type) { return this.get(key,type,null); }
    public <T> T get(String key,FancyPacketType<T> type) { return this.get(key,type,null); }
    public <T> T get(String key,Supplier<FancyPacketType<T>> type,T defaultValue) { return this.get(key,type.get(),defaultValue); }
    public <T> T get(String key,FancyPacketType<T> type,T defaultValue)
    {
        Entry<?> entry = this.getData(key);
        if(entry.is(type))
            return (T)entry.value;
        return defaultValue;
    }

    public boolean getBoolean(String key) { return this.getBoolean(key,false); }
    public boolean getBoolean(String key,boolean defaultValue) { return this.get(key,LCFancyPacketTypes.BOOLEAN,defaultValue); }

    public int getInt(String key) { return this.getInt(key,0); }
    public int getInt(String key,int defaultValue) { return this.get(key, LCFancyPacketTypes.INT,defaultValue); }

    public long getLong(String key) { return this.getLong(key,0L); }
    public long getLong(String key,long defaultValue) { return this.get(key, LCFancyPacketTypes.LONG,defaultValue); }

    public float getFloat(String key) { return this.getFloat(key,0f); }
    public float getFloat(String key,float defaultValue) { return this.get(key, LCFancyPacketTypes.FLOAT,defaultValue); }

    public double getDouble(String key) { return this.getDouble(key,0d); }
    public double getDouble(String key,double defaultValue) { return this.get(key, LCFancyPacketTypes.DOUBLE,defaultValue); }

    public String getString(String key) { return this.getString(key,""); }
    public String getString(String key,String defaultValue) { return this.get(key, LCFancyPacketTypes.STRING,defaultValue); }
    @Nullable
    public UUID getUUID(String key) { return this.getUUID(key,null); }
    public UUID getUUID(String key,UUID defaultValue) { return this.get(key, LCFancyPacketTypes.UUID,defaultValue); }

    @Nullable
    public <T extends Enum<T>> T getEnum(String key,Class<T> clazz) { return this.getEnum(key,clazz,null); }
    public <T extends Enum<T>> T getEnum(String key,Class<T> clazz,T defaultValue) {
        Entry<?> entry = this.getData(key);
        if(entry.is(LCFancyPacketTypes.INT.get()))
            return EnumHelper.enumFromOrdinal((int)entry.value,clazz.getEnumConstants(),defaultValue);
        return defaultValue;
    }

    @Nullable
    public Identifier getIdentifier(String key) { return this.getIdentifier(key,null); }
    public Identifier getIdentifier(String key,Identifier defaultValue) { return this.get(key, LCFancyPacketTypes.ID,defaultValue); }

    public Component getText(String key) { return this.getText(key,Component.empty()); }
    public Component getText(String key,Component defaultValue) { return this.get(key, LCFancyPacketTypes.TEXT,defaultValue); }

    public BlockPos getBlockPos(String key) { return this.getBlockPos(key,BlockPos.ZERO); }
    public BlockPos getBlockPos(String key,BlockPos defaultValue) { return this.get(key, LCFancyPacketTypes.BLOCK_POS,defaultValue); }

    public ItemStack getItem(String key) { return this.getItem(key,ItemStack.EMPTY); }
    public ItemStack getItem(String key,ItemStack defaultValue) { return this.get(key, LCFancyPacketTypes.ITEM_STACK,defaultValue); }

    @Nullable
    public <T> T getRegistryEntry(String key,Registry<T> registry) { return registry.byId(this.getInt(key)); }
    @Nullable
    public <T> T getFullRegistryEntry(String key,Registry<T> registry) { return registry.getValue(this.getIdentifier(key)); }

    public FancyPacketMap getMap(String key) { return this.getMap(key,EMPTY); }
    public FancyPacketMap getMap(String key,FancyPacketMap defaultValue) { return this.get(key, LCFancyPacketTypes.MAP,defaultValue); }

    public <T> List<T> getList(String key,Supplier<FancyPacketType<T>> listType) { return this.getList(key,listType.get()); }
    public <T> List<T> getList(String key,FancyPacketType<T> listType)
    {
        Entry<?> entry = this.getData(key);
        if(entry.is(LCFancyPacketTypes.LIST.get()))
            return castList(entry,listType).list;
        return new ArrayList<>();
    }

    public FancyPacketMap immutable() { return this; }
    public FancyPacketMap.Mutable mutable() { return new Mutable(this.dataMap); }

    public static Mutable newMutable() { return new Mutable(); }

    protected static <T> PacketList<T> castList(Entry<?> entry,FancyPacketType<T> type) { return ((PacketList<?>)entry.value).forceType(type); }

    public static final class Mutable extends FancyPacketMap
    {
        private Mutable() { this(new HashMap<>()); }
        private Mutable(Map<String, Entry<?>> data) { super(data); }
        @Override
        protected Map<String, Entry<?>> copyMap(Map<String, Entry<?>> data) {
            Map<String,Entry<?>> map = new HashMap<>();
            data.forEach((key,entry) -> map.put(key,entry.copy()));
            return map;
        }
        @Override
        public FancyPacketMap immutable() { return new FancyPacketMap(this.dataMap); }
        @Override
        public Mutable mutable() { return this; }

        public <T> Mutable set(String key, Supplier<FancyPacketType<T>> type, T value) { return this.set(key,type.get(),value); }
        public <T> Mutable set(String key,FancyPacketType<T> type,T value) { this.dataMap.put(key,new Entry<>(type,value)); return this; }
        public Mutable setFlag(String key) { this.dataMap.put(key,Entry.NULL); return this; }
        public Mutable setBoolean(String key,boolean value) { return this.set(key, LCFancyPacketTypes.BOOLEAN,value); }
        public Mutable setInt(String key,int value) { return this.set(key, LCFancyPacketTypes.INT,value); }
        public Mutable setLong(String key,long value) { return this.set(key, LCFancyPacketTypes.LONG,value); }
        public Mutable setFloat(String key,float value) { return this.set(key, LCFancyPacketTypes.FLOAT,value); }
        public Mutable setDouble(String key,double value) { return this.set(key, LCFancyPacketTypes.DOUBLE,value); }
        public Mutable setString(String key,String value) { return this.set(key, LCFancyPacketTypes.STRING,value); }

        public Mutable setUUID(String key,UUID value) { return this.set(key, LCFancyPacketTypes.UUID,value); }
        public <T extends Enum<T>>Mutable setEnum(String key,T value) { return this.setInt(key,value.ordinal()); }

        public Mutable setText(String key,Component value) { return this.set(key, LCFancyPacketTypes.TEXT,value); }
        public Mutable setIdentifier(String key,Identifier value) { return this.set(key, LCFancyPacketTypes.ID,value); }
        public Mutable setBlockPos(String key,BlockPos value) { return this.set(key, LCFancyPacketTypes.BLOCK_POS,value); }
        public Mutable setItem(String key,ItemStack value) { return this.set(key, LCFancyPacketTypes.ITEM_STACK,value.copy()); }

        public <T> Mutable setRegistryEntry(String key,Registry<T> registry,T value) { return this.setInt(key,registry.getId(value)); }
        public <T> Mutable setFullRegistryEntry(String key, Registry<T> registry, T value) { return this.setIdentifier(key,registry.getKey(value)); }

        public <T> Mutable setList(String key,Supplier<FancyPacketType<T>> listType,List<T> list) { return this.setList(key,listType.get(),list); }
        public <T> Mutable setList(String key,FancyPacketType<T> listType,List<T> list) { return this.set(key,LCFancyPacketTypes.LIST,new PacketList<>(listType,list)); }

        public <T> Mutable addToList(String key,Supplier<FancyPacketType<T>> listType,T value) { return this.addToList(key,listType.get(),value); }
        public <T> Mutable addToList(String key,FancyPacketType<T> listType,T value) {
            List<T> list = this.getList(key,listType);
            list.add(value);
            return this.setList(key,listType,list);
        }

        public <T> Mutable setListEntry(String key,Supplier<FancyPacketType<T>> listType,int index,T entry,Supplier<T> emptyEntry) { return this.setListEntry(key,listType.get(),index,entry,emptyEntry); }
        public <T> Mutable setListEntry(String key,FancyPacketType<T> listType,int index,T entry,Supplier<T> emptyEntry) {
            List<T> list = this.getList(key,listType);
            while(list.size() <= index)
                list.add(emptyEntry.get());
            list.set(index,entry);
            return this.setList(key,listType,list);
        }

        public <T> Mutable modifyListEntry(String key,Supplier<FancyPacketType<T>> listType,int index,UnaryOperator<T> modification,Supplier<T> emptyEntry) { return this.modifyListEntry(key,listType.get(),index,modification,emptyEntry); }
        public <T> Mutable modifyListEntry(String key,FancyPacketType<T> listType,int index,UnaryOperator<T> modification,Supplier<T> emptyEntry) {
            List<T> list = this.getList(key,listType);
            while(list.size() <= index)
                list.add(emptyEntry.get());
            list.set(index,modification.apply(list.get(index)));
            return this.setList(key,listType,list);
        }

        public Mutable setMap(String key,FancyPacketMap value) { return this.set(key, LCFancyPacketTypes.MAP,value.immutable()); }
        public Mutable setOptionalMap(String key, FancyPacketMap value) {
            if(value.isEmpty())
                return this;
            return this.setMap(key,value);
        }
        public Mutable modifyMap(String key,Consumer<Mutable> action) {
            Mutable map = this.getMap(key).mutable();
            action.accept(map);
            return this.setMap(key,map);
        }

        public Mutable clear() { this.dataMap.clear(); return this; }
        public Mutable remove(String key) { this.dataMap.remove(key); return this; }

    }

    public record Entry<T>(FancyPacketType<T> type,T value)
    {
        public Entry(Supplier<FancyPacketType<T>> type,T value) { this(type.get(),value); }

        private static final Entry<Unit> NULL = new Entry<>(LCFancyPacketTypes.NULL,Unit.INSTANCE);

        public boolean is(FancyPacketType<?> type) { return this.type == type; }
        //Copy PacketList entries for safety just in case the builder gets modified later
        private Entry<?> copy() {
            if(this.value instanceof PacketList<?> list)
                return new Entry<>(LCFancyPacketTypes.LIST,list.copy());
            return this;
        }
        private void encode(RegistryFriendlyByteBuf buf)
        {
            buf.writeInt(LCRegistries.Network.PACKET_TYPE.getId(this.type));
            this.type.codec().encode(buf,this.value);
        }
        private static Entry<?> decode(RegistryFriendlyByteBuf buf)
        {
            FancyPacketType<?> type = LCRegistries.Network.PACKET_TYPE.byId(buf.readInt());
            Object value = type.codec().decode(buf);
            return parse(type,forceCast(type,value));
        }
        private static <T> Entry<T> parse(FancyPacketType<T> type,Object value) { return new Entry<>(type,forceCast(type,value)); }
        private static <T> T forceCast(FancyPacketType<T> type,Object value) { return (T)value; }

    }

}
