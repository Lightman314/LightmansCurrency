package io.github.lightman314.lightmanscurrency.api.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LazyPacketData {

    public static final StreamCodec<RegistryFriendlyByteBuf,LazyPacketData> STREAM_CODEC = StreamCodec.of((buffer, value) -> value.encode(buffer),LazyPacketData::decode);

    //Normal Types
    public static final byte TYPE_NULL = 0;
    public static final byte TYPE_BOOLEAN = 1;
    public static final byte TYPE_INT = 2;
    public static final byte TYPE_LONG = 3;
    public static final byte TYPE_FLOAT = 4;
    public static final byte TYPE_DOUBLE = 5;
    public static final byte TYPE_STRING = 6;

    //Fancy Types
    public static final byte TYPE_UUID = 32;

    //Minecraft Types
    public static final byte TYPE_TEXT = 64;
    public static final byte TYPE_NBT = 65;
    //Nested LazyPacketData
    public static final byte TYPE_CUSTOM = 125;
    public static final byte TYPE_LIST = 126;
    public static final byte TYPE_LPD = 127;

    public static final Function<Void,Data> FLAG_FACTORY = o -> Data.ofNull();
    public static final Function<Boolean,Data> BOOLEAN_FACTORY = Data::ofBoolean;
    public static final Function<Integer,Data> INT_FACTORY = Data::ofInt;
    public static final Function<Long,Data> LONG_FACTORY = Data::ofLong;
    public static final Function<Float,Data> FLOAT_FACTORY = Data::ofFloat;
    public static final Function<Double,Data> DOUBLE_FACTORY = Data::ofDouble;
    public static final Function<String,Data> STRING_FACTORY = Data::ofString;
    public static final Function<UUID,Data> UUID_FACTORY = Data::ofUUID;
    public static final Function<ResourceLocation,Data> RL_FACTORY = Data::ofResourceLocation;
    public static final Function<Component,Data> TEXT_FACTORY = Data::ofText;
    public static final Function<CompoundTag,Data> TAG_FACTORY = Data::ofNBT;
    public static final Function<LazyPacketData,Data> MAP_FACTORY = Data::ofPacketData;
    public static final Function<Builder,Data> BUILDER_FACTORY = b -> Data.ofPacketData(b.build());

    private final ImmutableMap<String,Data> dataMap;
    public final HolderLookup.Provider lookup;

    private LazyPacketData(Map<String,Data> data, HolderLookup.Provider lookup) { this.dataMap = ImmutableMap.copyOf(data); this.lookup = lookup; }

    private Data getData(String key) { return this.dataMap.getOrDefault(key,Data.NULL); }

    public boolean isEmpty() { return this.dataMap.isEmpty(); }
    public Set<String> keySet() { return this.dataMap.keySet(); }

    public boolean contains(String key) { return this.dataMap.containsKey(key); }
    public boolean contains(String key, byte type)
    {
        return this.contains(key) && this.getData(key).type == type;
    }

    public boolean getBoolean(String key) { return this.getBoolean(key, false); }
    public boolean getBoolean(String key, boolean defaultValue) {
        Data d = this.getData(key);
        if(d.type == TYPE_BOOLEAN)
            return (boolean)d.value;
        return defaultValue;
    }

    public int getInt(String key) { return this.getInt(key, 0); }
    public int getInt(String key, int defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_INT)
            return (int)d.value;
        return defaultValue;
    }

    public long getLong(String key) { return this.getLong(key, 0L); }
    public long getLong(String key, long defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_LONG)
            return (long)d.value;
        return defaultValue;
    }

    public float getFloat(String key) { return this.getFloat(key, 0f); }
    public float getFloat(String key, float defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_FLOAT)
            return (float)d.value;
        return defaultValue;
    }

    public double getDouble(String key) { return this.getDouble(key, 0d); }
    public double getDouble(String key, double defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_DOUBLE)
            return (double)d.value;
        return defaultValue;
    }

    public String getString(String key) { return this.getString(key, null); }
    public String getString(String key, String defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_STRING)
            return (String)d.value;
        return defaultValue;
    }

    public UUID getUUID(String key) { return this.getUUID(key,null); }
    public UUID getUUID(String key, UUID defaultValue) {
        Data d = this.getData(key);
        if(d.type == TYPE_UUID)
            return (UUID)d.value;
        return defaultValue;
    }

    public ResourceLocation getResourceLocation(String key) { return this.getResourceLocation(key, null); }
    public ResourceLocation getResourceLocation(String key, ResourceLocation defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_STRING)
        {
            try { return ResourceLocation.parse((String)d.value);
            } catch (ResourceLocationException ignored) {}
        }
        return defaultValue;
    }

    public Component getText(String key) { return this.getText(key, EasyText.empty()); }
    public Component getText(String key, Component defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_TEXT)
            return (Component)d.value;
        return defaultValue;
    }

    public <T> T decodeObject(String key, Codec<T> codec) { return this.decodeObject(key,codec,null); }
    public <T> T decodeObject(String key, Codec<T> codec, T defaultValue)
    {
        CompoundTag tag = this.getTag(key);
        try { return codec.decode(RegistryOps.create(NbtOps.INSTANCE,this.lookup),tag).getOrThrow().getFirst();
        } catch (Exception ignored) { return defaultValue; }
    }

    public CompoundTag getTag(String key) { return this.getTag(key, new CompoundTag()); }
    public CompoundTag getTag(String key, CompoundTag defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return (CompoundTag)d.value;
        return defaultValue;
    }

    public BlockPos getBlockPos(String key) { return this.getBlockPos(key,BlockPos.ZERO); }
    public BlockPos getBlockPos(String key, BlockPos defaultValue) { return this.getCustom(key,ModLazyPackets.BLOCK_POS,defaultValue); }

    public ItemStack getItem(String key) { return this.getItem(key,ItemStack.EMPTY); }
    public ItemStack getItem(String key, ItemStack defaultValue) { return this.getCustom(key,ModLazyPackets.ITEM_STACK,defaultValue); }
    public MoneyValue getMoneyValue(String key) { return this.getMoneyValue(key, MoneyValue.empty()); }
    public MoneyValue getMoneyValue(String key, MoneyValue defaultValue) { return this.getCustom(key,ModLazyPackets.MONEY_VALUE,defaultValue); }
    @Nullable
    public Owner getOwner(String key) { return this.getOwner(key,null); }
    public Owner getOwner(String key, Owner defaultValue) { return this.getCustom(key,ModLazyPackets.OWNER,defaultValue); }

    @Nullable
    public <T> T getCustom(String key,Codec<T> codec) { return this.getCustom(key,codec,null); }
    @Nullable
    public <T> T getCustom(String key,Codec<T> codec,@Nullable T defaultValue) {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
        {
            try {
                return codec.decode(RegistryOps.create(NbtOps.INSTANCE,this.lookup),(CompoundTag)d.value)
                        .getOrThrow(RuntimeException::new).getFirst();
            } catch (RuntimeException ignored) {}
        }
        return defaultValue;
    }

    @Nullable
    public <T> T getCustom(String key, Supplier<LazyPacketType<T>> codec) { return this.getCustom(key,codec.get(),null); }
    @Nullable
    public <T> T getCustom(String key, LazyPacketType<T> codec) { return this.getCustom(key,codec,null); }
    @Nullable
    public <T> T getCustom(String key, Supplier<LazyPacketType<T>> codec, T defaultValue) { return this.getCustom(key,codec.get(),defaultValue); }
    @Nullable
    public <T> T getCustom(String key, LazyPacketType<T> codec, T defaultValue) {
        Data d = this.getData(key);
        if(d.type == TYPE_CUSTOM)
        {
            try { return (T)((CustomData<?>)d.value).value;
            } catch (ClassCastException ignored) {}
        }
        return defaultValue;
    }

    public LazyPacketData getMap(String key) { return this.getMap(key,builder(this.lookup).build()); }
    public LazyPacketData getMap(String key, LazyPacketData defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_LPD)
            return (LazyPacketData)d.value;
        return defaultValue;
    }

    public <T> List<T> getList(String key,Supplier<LazyPacketType<T>> type) { return this.getList(key,type.get()); }
    public <T> List<T> getList(String key,LazyPacketType<T> type) { return this.getList(key,new ArrayList<>(),type); }
    public <T> List<T> getList(String key,List<T> defaultValue,Supplier<LazyPacketType<T>> type) { return this.getList(key,defaultValue,type.get()); }
    public <T> List<T> getList(String key,List<T> defaultValue,LazyPacketType<T> type) {
        Data data = this.getData(key);
        if(data.type == TYPE_LIST)
        {
            List<T> result = new ArrayList<>();
            List<Data> list = (List<Data>)data.value;
            for(Data d : list)
            {
                if(d.type == TYPE_CUSTOM)
                {
                    try {
                        result.add((T)((CustomData<?>)d.value).value);
                    } catch (ClassCastException ignored) {}
                }
            }
            return result;
        }
        return defaultValue;
    }

    public <T> List<T> getList(String key,Class<T> type) { return this.getList(key,new ArrayList<>(),type); }
    public <T> List<T> getList(String key,List<T> defaultValue,Class<T> type)
    {
        Data data = this.getData(key);
        if(data.type == TYPE_LIST)
        {
            List<T> result = new ArrayList<>();
            List<Data> list = (List<Data>)data.value;
            for(Data d : list)
            {
                try { result.add(type.cast(d.value));
                } catch (ClassCastException ignored) {}
            }
            return result;
        }
        return defaultValue;
    }

    public void encode(RegistryFriendlyByteBuf buffer)
    {
        //Write the entry count
        buffer.writeInt(this.dataMap.size());
        //Write each entry
        this.dataMap.forEach((key,data) -> {
            buffer.writeUtf(key,100);
            buffer.writeByte(data.type);
            data.encode(buffer);
        });
    }

    public static LazyPacketData decode(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readInt();
        HashMap<String,Data> dataMap = new HashMap<>();
        for(int i = 0; i < count; ++i)
        {
            String key = buffer.readUtf(100);
            Data data = Data.decode(buffer);
            dataMap.put(key, data);
        }
        return new LazyPacketData(dataMap, buffer.registryAccess());
    }

    public Builder copyToBuilder()
    {
        Builder b = new Builder(this.lookup);
        this.dataMap.forEach(b::addData);
        return b;
    }

    public int size() { return this.dataMap.size(); }
    public int size(@Nullable String ignoreKey)
    {
        int size = this.dataMap.size();
        if(ignoreKey != null &&  this.contains(ignoreKey))
            return size - 1;
        return size;
    }

    public static Builder builder(HolderLookup.Provider lookup) { return new Builder(lookup); }
    public static final class Builder
    {

        public final HolderLookup.Provider lookup;
        private Builder(HolderLookup.Provider lookup) { this.lookup = lookup; }
        Map<String,Data> data = new HashMap<>();

        public boolean has(String key) { return this.data.containsKey(key); }

        public boolean isEmpty() { return this.data.isEmpty(); }

        private void addData(String key, Data data) { this.data.put(key, data); }
        public Builder setFlag(String key) { this.data.put(key,Data.ofNull()); return this; }
        public Builder setBoolean(String key, boolean value) { this.data.put(key, Data.ofBoolean(value)); return this; }
        public Builder setInt(String key, int value) { this.data.put(key, Data.ofInt(value)); return this; }
        public Builder setLong(String key, long value) { this.data.put(key, Data.ofLong(value)); return this; }
        public Builder setFloat(String key, float value) { this.data.put(key, Data.ofFloat(value)); return this; }
        public Builder setDouble(String key, double value) { this.data.put(key, Data.ofDouble(value)); return this; }
        public Builder setString(String key, String value) { this.data.put(key, Data.ofString(value)); return this; }

        public Builder setUUID(String key, UUID uuid) { this.data.put(key,Data.ofUUID(uuid)); return this; }

        public Builder setResourceLocation(String key, ResourceLocation value) { this.data.put(key, Data.ofString(value.toString())); return this; }
        public Builder setText(String key, Component value) { this.data.put(key, Data.ofText(value)); return this; }
        public Builder setTag(String key, CompoundTag value) { this.data.put(key, Data.ofNBT(value)); return this; }
        public Builder setBlockPos(String key, BlockPos value) { return this.setCustom(key,value,ModLazyPackets.BLOCK_POS); }
        public Builder setItem(String key, ItemStack value) { return this.setCustom(key,value,ModLazyPackets.ITEM_STACK); }
        public Builder setMoneyValue(String key, MoneyValue value) { return this.setCustom(key,value,ModLazyPackets.MONEY_VALUE); }
        public Builder setOwner(String key, Owner value) { return this.setCustom(key,value,ModLazyPackets.OWNER); }

        /**
         * @deprecated Use {@link #setCustom(String, Object, LazyPacketType)} instead if possible
         */
        @Deprecated
        public <T> Builder setCustom(String key, T object, Codec<T> codec) { this.data.put(key,Data.ofNBT((CompoundTag)codec.encodeStart(RegistryOps.create(NbtOps.INSTANCE,this.lookup),object).getOrThrow())); return this; }
        public <T> Builder setCustom(String key, T object, Supplier<LazyPacketType<T>> type) { return this.setCustom(key,object,type.get()); }
        public <T> Builder setCustom(String key, T object, LazyPacketType<T> type) {
            if(!LCRegistries.LAZY_PACKETS.containsValue(type))
                throw new IllegalArgumentException("Cannot set a custom stream codec value for " + object.getClass().getSimpleName() + " as its stream codec has not been registered to the " + LCRegistries.LAZY_PACKETS_KEY.location() + " registry!");
            this.data.put(key,Data.ofCustom(new CustomData<>(type,object)));
            return this;
        }

        public Builder setMap(String key, LazyPacketData value) { this.data.put(key,Data.ofPacketData(value)); return this; }
        public Builder setMap(String key, LazyPacketData.Builder value) { return this.setMap(key,value == null ? null : value.build()); }

        //The only real "get" needed in the builder tbh
        public Builder modifyMap(String key, Consumer<Builder> consumer) {
            Data d = this.data.get(key);
            if(d == null || !(d.value instanceof LazyPacketData map))
            {
                Builder b = builder(this.lookup);
                consumer.accept(b);
                this.setMap(key,b);
            }
            else
            {
                Builder b = map.copyToBuilder();
                consumer.accept(b);
                this.setMap(key,b);
            }
            return this;
        }

        public <T> Builder setList(String key, Collection<T> list, Supplier<LazyPacketType<T>> type) { return this.setList(key,list,type.get()); }
        public <T> Builder setList(String key, Collection<T> list, LazyPacketType<T> type) { return this.setList(key,list, e -> Data.ofCustom(type,e)); }
        /**
         * Adds a list to the data builder
         * @param key The key of the data entry.
         * @param list The list to write to the data builder
         * @param factory The factory for creating the {@link Data} entry.<br>
         *                Default Factories can be found as constants in this builder class.<br>
         *                Use {@link #setList(String, Collection, Supplier)} to set custom lists that don't utilize those default factories
         */
        public <T> Builder setList(String key, Collection<T> list, Function<T,Data> factory)
        {
            List<Data> result = new ArrayList<>();
            for(T entry : list)
                result.add(factory.apply(entry));
            this.data.put(key,Data.ofList(result));
            return this;
        }

        public <T> Builder addToList(String key, T entry,Supplier<LazyPacketType<T>> type) { return this.addToList(key,entry,type.get()); }
        public <T> Builder addToList(String key, T entry,LazyPacketType<T> type) { return this.addToList(key,entry,e -> Data.ofCustom(type,e)); }
        public <T> Builder addToList(String key, T entry,Function<T,Data> factory)
        {
            if(this.data.containsKey(key))
            {
                Data d = this.data.get(key);
                if(d.type == TYPE_LIST)
                {
                    try {
                        List<Data> list = (List<Data>)d.value;
                        list.add(factory.apply(entry));
                        return this;
                    } catch (ClassCastException ignored) {}
                }
            }
            //If the exception was thrown and caught (or the list doesn't exist at all), instead set the data to the list directly
            return this.setList(key,ImmutableList.of(entry),factory);
        }

        public Builder clear() { this.data.clear(); return this; }

        public Builder remove(String key) { this.data.remove(key); return this; }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("LazyPacketData$Builder[");
            AtomicBoolean notFirst = new AtomicBoolean(false);
            this.data.forEach((key,val) -> {
                if(notFirst.get())
                    builder.append(",");
                builder.append(key).append(":");
                builder.append(val.toString());
                notFirst.set(true);
            });
            return builder.append("]").toString();
        }

        public LazyPacketData build() { return new LazyPacketData(this.data, this.lookup); }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("LazyPacketData[");
        AtomicBoolean notFirst = new AtomicBoolean(false);
        this.dataMap.forEach((key,val) -> {
            if(notFirst.get())
                builder.append(",");
            builder.append(key).append(":");
            builder.append(val);
            notFirst.set(true);
        });
        return builder.append("]").toString();
    }

    public record Data(byte type, Object value) {

        static final Data NULL = new Data(TYPE_NULL, null);

        private static Data ofNull() { return NULL; }
        private static Data ofBoolean(boolean value) { return new Data(TYPE_BOOLEAN, value); }
        private static Data ofInt(int value) { return new Data(TYPE_INT, value); }
        private static Data ofLong(long value) { return new Data(TYPE_LONG, value); }
        private static Data ofFloat(float value) { return new Data(TYPE_FLOAT, value); }
        private static Data ofDouble(double value) { return new Data(TYPE_DOUBLE, value); }
        private static Data ofString(@Nullable String value) { return value == null ? NULL : new Data(TYPE_STRING, value); }
        private static Data ofUUID(@Nullable UUID value) { return value == null ? NULL : new Data(TYPE_UUID, value); }
        private static Data ofResourceLocation(@Nullable ResourceLocation value) { return value == null ? NULL : new Data(TYPE_STRING, value.toString()); }
        private static Data ofText(@Nullable Component value) { return value == null ? NULL : new Data(TYPE_TEXT, value); }
        private static Data ofNBT(@Nullable CompoundTag value) { return value == null ? NULL : new Data(TYPE_NBT, value); }
        private static <T> Data ofCustom(LazyPacketType<T> type,@Nullable T value) {
            if(value == null)
                return ofCustom(null);
            return ofCustom(new CustomData<>(type,value));
        }
        private static Data ofCustom(@Nullable CustomData<?> value) { return value == null || value.value == null ? NULL : new Data(TYPE_CUSTOM,value); }
        private static Data ofList(@Nullable List<Data> value) { return value == null ? NULL : new Data(TYPE_LPD,value); }
        private static Data ofPacketData(@Nullable LazyPacketData value) { return value == null ? NULL : new Data(TYPE_LPD,value); }

        void encode(RegistryFriendlyByteBuf buffer)
        {
            //Normal Values
            if(this.type == TYPE_BOOLEAN)
                buffer.writeBoolean((boolean) this.value);
            if(this.type == TYPE_INT)
                buffer.writeInt((int)this.value);
            if(this.type == TYPE_LONG)
                buffer.writeLong((long)this.value);
            if(this.type == TYPE_FLOAT)
                buffer.writeFloat((float)this.value);
            if(this.type == TYPE_DOUBLE)
                buffer.writeDouble((double)this.value);
            if(this.type == TYPE_STRING)
            {
                int length = ((String)this.value).length();
                buffer.writeInt(length);
                buffer.writeUtf((String)this.value,length);
            }
            if(this.type == TYPE_UUID)
                buffer.writeUUID((UUID)this.value);

            //MC values
            if(this.type == TYPE_TEXT)
                ComponentSerialization.STREAM_CODEC.encode(buffer,(Component)this.value);
            if(this.type == TYPE_NBT)
                buffer.writeNbt((CompoundTag)this.value);

            //Custom values
            if(this.type == TYPE_CUSTOM)
            {
                CustomData<?> data = (CustomData<?>)this.value;
                data.encode(buffer);
            }
            //List
            if(this.type == TYPE_LIST)
            {
                List<Data> data = (List<Data>)this.value;
                buffer.writeInt(data.size());
                for(Data d : data)
                    d.encode(buffer);
            }
            if(this.type == TYPE_LPD)
                ((LazyPacketData)this.value).encode(buffer);
        }

        static Data decode(RegistryFriendlyByteBuf buffer)
        {
            byte type = buffer.readByte();
            //Normal Values
            if(type == TYPE_NULL)
                return ofNull();
            if(type == TYPE_BOOLEAN)
                return ofBoolean(buffer.readBoolean());
            if(type == TYPE_INT)
                return ofInt(buffer.readInt());
            if(type == TYPE_LONG)
                return ofLong(buffer.readLong());
            if(type == TYPE_FLOAT)
                return ofFloat(buffer.readFloat());
            if(type == TYPE_DOUBLE)
                return ofDouble(buffer.readDouble());
            if(type == TYPE_STRING)
            {
                int length = buffer.readInt();
                return ofString(buffer.readUtf(length));
            }
            if(type == TYPE_UUID)
                return ofUUID(buffer.readUUID());

            //Minecraft Values
            if(type == TYPE_TEXT)
                return ofText(ComponentSerialization.STREAM_CODEC.decode(buffer));
            if(type == TYPE_NBT)
                return ofNBT((CompoundTag)buffer.readNbt(NbtAccounter.unlimitedHeap()));
            if(type == TYPE_CUSTOM)
            {
                int codecID = buffer.readInt();
                LCRegistries.LAZY_PACKETS.getHolder(codecID).ifPresent(holder ->
                    ofCustom(new CustomData<>((LazyPacketType<Object>)holder.value(),holder.value().codec().decode(buffer))));
            }
            if(type == TYPE_LIST)
            {
                int size = buffer.readInt();
                List<Data> result = new ArrayList<>();

            }
            if(type == TYPE_LPD)
                return ofPacketData(LazyPacketData.decode(buffer));

            throw new RuntimeException("Could not decode entry of type " + type + "as it is not a valid data entry type!");
        }

        @Override
        public String toString() { return String.valueOf(this.value); }

    }

    private record CustomData<T>(LazyPacketType<T> type,T value)
    {
        private void encode(RegistryFriendlyByteBuf buffer) {
            buffer.writeInt(LCRegistries.LAZY_PACKETS.getId(this.type));
            this.type.codec().encode(buffer,this.value);
        }
    }

}
