package io.github.lightman314.lightmanscurrency.network.packet;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class LazyPacketData {

    //Normal Types
    public static final byte TYPE_NULL = 0;
    public static final byte TYPE_BOOLEAN = 1;
    public static final byte TYPE_INT = 2;
    public static final byte TYPE_LONG = 3;
    public static final byte TYPE_FLOAT = 4;
    public static final byte TYPE_DOUBLE = 5;
    public static final byte TYPE_STRING = 6;

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

    public CoinValue getCoinValue(String key) { return this.getCoinValue(key, CoinValue.EMPTY); }
    public CoinValue getCoinValue(String key, CoinValue defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return CoinValue.load((CompoundTag)d.value);
        return defaultValue;
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

    public static Builder builder() { return new Builder(); }

    public static final class Builder
    {
        private Builder() {}
        Map<String,Data> data = new HashMap<>();

        public Builder setBoolean(String key, boolean value) { this.data.put(key, Data.ofBoolean(value)); return this; }
        public Builder setInt(String key, int value) { this.data.put(key, Data.ofInt(value)); return this; }
        public Builder setLong(String key, long value) { this.data.put(key, Data.ofLong(value)); return this; }
        public Builder setFloat(String key, float value) { this.data.put(key, Data.ofFloat(value)); return this; }
        public Builder setDouble(String key, double value) { this.data.put(key, Data.ofDouble(value)); return this; }
        public Builder setString(String key, String value) { this.data.put(key, Data.ofString(value)); return this; }

        public Builder setText(String key, Component value) { this.data.put(key, Data.ofText(value)); return this; }
        public Builder setCompound(String key, CompoundTag value) { this.data.put(key, Data.ofNBT(value)); return this; }
        public Builder setCoinValue(String key, CoinValue value) { this.data.put(key, Data.ofCoinValue(value)); return this; }


        public LazyPacketData build() { return new LazyPacketData(this.data); }

    }

    private record Data(byte type, Object value) {

        static final Data NULL = new Data(TYPE_NULL, null);

        static Data ofNull() { return NULL; }
        static Data ofBoolean(boolean value) { return new Data(TYPE_BOOLEAN, value); }
        static Data ofInt(int value) { return new Data(TYPE_INT, value); }
        static Data ofLong(long value) { return new Data(TYPE_LONG, value); }
        static Data ofFloat(float value) { return new Data(TYPE_FLOAT, value); }
        static Data ofDouble(double value) { return new Data(TYPE_DOUBLE, value); }
        static Data ofString(String value) { return value == null ? NULL : new Data(TYPE_STRING, value); }
        static Data ofText(Component value) { return value == null ? NULL : new Data(TYPE_TEXT, value); }
        static Data ofNBT(CompoundTag value) { return value == null ? NULL : new Data(TYPE_NBT, value); }
        static Data ofCoinValue(CoinValue value) { return value == null ? NULL : ofNBT(value.save()); }

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
                buffer.writeUtf((String)this.value);
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
                return ofString(buffer.readUtf());
            //Minecraft Values
            if(type == TYPE_TEXT)
                return ofText(Component.Serializer.fromJson(buffer.readUtf()));
            if(type == TYPE_NBT)
                return ofNBT(buffer.readNbt());
            throw new RuntimeException("Could not decode entry of type " + type + "as it is not a valid data entry type!");
        }

    }

}
