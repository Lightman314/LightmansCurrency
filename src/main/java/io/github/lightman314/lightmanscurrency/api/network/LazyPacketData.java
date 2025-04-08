package io.github.lightman314.lightmanscurrency.api.network;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.util.TriConsumer;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public final class LazyPacketData {

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

    private final ImmutableMap<String,Data> dataMap;

    private LazyPacketData(Map<String,Data> data) { this.dataMap = ImmutableMap.copyOf(data); }

    @Nonnull
    private Data getData(String key) { return this.dataMap.getOrDefault(key, Data.NULL); }

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
            return VersionUtil.parseResource((String)d.value);
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

    public CompoundTag getNBT(String key) { return this.getNBT(key, new CompoundTag()); }
    public CompoundTag getNBT(String key, CompoundTag defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return (CompoundTag)d.value;
        return defaultValue;
    }

    public ItemStack getItem(String key) { return this.getItem(key,ItemStack.EMPTY); }
    public ItemStack getItem(String key, ItemStack defaultValue) {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return ItemStack.of((CompoundTag)d.value);
        return defaultValue;
    }

    public MoneyValue getMoneyValue(String key) { return this.getMoneyValue(key, MoneyValue.empty()); }
    public MoneyValue getMoneyValue(String key, MoneyValue defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return MoneyValue.load((CompoundTag)d.value);
        return defaultValue;
    }

    public Owner getOwner(String key) { return this.getOwner(key,null); }
    public Owner getOwner(String key, Owner defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return Owner.load((CompoundTag)d.value);
        return defaultValue;
    }

    public <T> List<T> getList(String key, BiFunction<LazyPacketData,String,T> reader) { return this.getList(key,new ArrayList<>(),reader); }
    public <T> List<T> getList(String key, List<T> defaultValue, BiFunction<LazyPacketData,String,T> reader)
    {
        //Check if original key is present, as we set a flag with that key to confirm that the list was written
        if(!this.contains(key))
            return defaultValue;
        List<T> list = new ArrayList<>();
        int index = 0;
        while(true)
        {
            String thisKey = key + "_" + index++;
            if(this.contains(thisKey))
                list.add(reader.apply(this,thisKey));
            else
                return list;
        }
    }

    public void encode(FriendlyByteBuf buffer)
    {
        //Write the entry count
        buffer.writeInt(this.dataMap.entrySet().size());
        //Write each entry
        this.dataMap.forEach((key,data) -> {
            buffer.writeUtf(key, 32);
            buffer.writeByte(data.type);
            data.encode(buffer);
        });
    }

    public static LazyPacketData decode(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        HashMap<String,Data> dataMap = new HashMap<>();
        for(int i = 0; i < count; ++i)
        {
            String key = buffer.readUtf(32);
            Data data = Data.decode(buffer);
            dataMap.put(key, data);
        }
        return new LazyPacketData(dataMap);
    }

    public Builder copyToBuilder()
    {
        Builder b = new Builder();
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

    public static Builder builder() { return new Builder(); }
    public static Builder simpleFlag(String key) { return simpleBoolean(key, true); }
    public static Builder simpleBoolean(String key, boolean value) { return builder().setBoolean(key, value); }
    public static Builder simpleInt(String key, int value) { return builder().setInt(key, value); }
    public static Builder simpleLong(String key, long value) { return builder().setLong(key, value); }
    public static Builder simpleFloat(String key, float value) { return builder().setFloat(key, value); }
    public static Builder simpleDouble(String key, double value) { return builder().setDouble(key, value); }
    public static Builder simpleString(String key, String value) { return builder().setString(key, value); }
    public static Builder simpleText(String key, Component value) { return builder().setText(key, value); }
    public static Builder simpleTag(String key, CompoundTag value) { return builder().setCompound(key, value); }
    public static Builder simpleMoneyValue(String key, MoneyValue value) { return builder().setMoneyValue(key, value); }

    public static final class Builder
    {
        private Builder() {}
        Map<String,Data> data = new HashMap<>();

        private void addData(@Nonnull String key, @Nonnull Data data) { this.data.put(key, data); }
        public Builder setFlag(@Nonnull String key) { return this.setBoolean(key,true); }
        public Builder setBoolean(@Nonnull String key, boolean value) { this.data.put(key, Data.ofBoolean(value)); return this; }
        public Builder setInt(@Nonnull String key, int value) { this.data.put(key, Data.ofInt(value)); return this; }
        public Builder setLong(@Nonnull String key, long value) { this.data.put(key, Data.ofLong(value)); return this; }
        public Builder setFloat(@Nonnull String key, float value) { this.data.put(key, Data.ofFloat(value)); return this; }
        public Builder setDouble(@Nonnull String key, double value) { this.data.put(key, Data.ofDouble(value)); return this; }
        public Builder setString(@Nonnull String key, String value) { this.data.put(key, Data.ofString(value)); return this; }

        public Builder setUUID(@Nonnull String key, UUID uuid) { this.data.put(key,Data.ofUUID(uuid)); return this; }

        public Builder setResourceLocation(String key, ResourceLocation value) { this.data.put(key, Data.ofString(value.toString())); return this; }
        public Builder setText(String key, Component value) { this.data.put(key, Data.ofText(value)); return this; }
        public Builder setCompound(String key, CompoundTag value) { this.data.put(key, Data.ofNBT(value)); return this; }
        public Builder setItem(@Nonnull String key, ItemStack value) { this.data.put(key, Data.ofItem(value)); return this; }
        public Builder setMoneyValue(String key, MoneyValue value) { this.data.put(key, Data.ofMoneyValue(value)); return this; }
        public Builder setOwner(String key, Owner value) { this.data.put(key, Data.ofOwner(value)); return this; }

        /**
         * Addes a list to the data builder
         * @param key The root key of the data entry. Will be present in the resulting data as a flag to make it easy to confirm this lists presence.
         * @param list The list to write to the data builder
         * @param writer The normal Builder#setDATA_TYPE method used to write the values to the builder, e.g. LazyPacketData.Builder::setInt for an integer list
         */
        public <T> Builder setList(@Nonnull String key, @Nonnull List<T> list, @Nonnull TriConsumer<Builder,String,T> writer)
        {
            //Set flag at original key to make it known that the list was written even if it's empty
            this.setFlag(key);
            for(int i = 0; i < list.size(); ++i)
                writer.accept(this,key + "_" + i,list.get(i));
            return this;
        }

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

        public LazyPacketData build() { return new LazyPacketData(this.data); }

    }

    public interface IBuilderProvider
    {
        @Nonnull
        Builder builder();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("LazyPacketData[");
        AtomicBoolean notFirst = new AtomicBoolean(false);
        this.dataMap.forEach((key,val) -> {
            if(notFirst.get())
                builder.append(",");
            builder.append(key).append(":");
            builder.append(val.toString());
            notFirst.set(true);
        });
        return builder.append("]").toString();
    }

    private record Data(byte type, Object value) {

        static final Data NULL = new Data(TYPE_NULL, null);

        static Data ofNull() { return NULL; }
        static Data ofBoolean(boolean value) { return new Data(TYPE_BOOLEAN, value); }
        static Data ofInt(int value) { return new Data(TYPE_INT, value); }
        static Data ofLong(long value) { return new Data(TYPE_LONG, value); }
        static Data ofFloat(float value) { return new Data(TYPE_FLOAT, value); }
        static Data ofDouble(double value) { return new Data(TYPE_DOUBLE, value); }
        static Data ofString(@Nullable String value) { return value == null ? NULL : new Data(TYPE_STRING, value); }
        static Data ofUUID(@Nullable UUID value) { return value == null ? NULL : new Data(TYPE_UUID,value); }
        static Data ofResourceLocation(@Nullable ResourceLocation value) { return value == null ? NULL : new Data(TYPE_STRING, value.toString()); }
        static Data ofText(@Nullable Component value) { return value == null ? NULL : new Data(TYPE_TEXT, value); }
        static Data ofNBT(@Nullable CompoundTag value) { return value == null ? NULL : new Data(TYPE_NBT, value); }
        static Data ofItem(@Nullable ItemStack value) {
            return value == null ? NULL : ofNBT(value.save(new CompoundTag()));
        }
        static Data ofMoneyValue(@Nullable MoneyValue value) {
            //return value == null ? NULL : ofNBT(value.save());
            if(value == null)
                return NULL;
            CompoundTag tag = value.save();
            LightmansCurrency.LogDebug("Saving Money Value to tag!\n" + tag.getAsString());
            return ofNBT(tag);
        }
        static Data ofOwner(@Nullable Owner value) {
            if(value == null)
                return NULL;
            CompoundTag tag = value.save();
            return ofNBT(tag);
        }

        void encode(FriendlyByteBuf buffer)
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
                buffer.writeUtf(Component.Serializer.toJson((Component)this.value));
            if(this.type == TYPE_NBT)
                buffer.writeNbt((CompoundTag)this.value);
        }

        static Data decode(FriendlyByteBuf buffer)
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
                return ofText(Component.Serializer.fromJson(buffer.readUtf()));
            if(type == TYPE_NBT)
                return ofNBT(buffer.readAnySizeNbt());
            throw new RuntimeException("Could not decode entry of type " + type + "as it is not a valid data entry type!");
        }

        @Override
        public String toString() { return String.valueOf(this.value); }

    }

}
