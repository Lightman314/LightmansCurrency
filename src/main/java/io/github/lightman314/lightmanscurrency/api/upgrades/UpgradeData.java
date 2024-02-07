package io.github.lightman314.lightmanscurrency.api.upgrades;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpgradeData
{

    public static final UpgradeData EMPTY = new UpgradeData();

    private final Map<String,Object> data = new HashMap<>();

    public Set<String> getKeys() { return data.keySet(); }

    public boolean hasKey(String tag)
    {
        return this.getKeys().contains(tag);
    }

    private UpgradeData() {}

    public UpgradeData(@Nonnull UpgradeType upgrade)
    {
        for(String tag : upgrade.getDataTags())
        {
            Object defaultValue = upgrade.defaultTagValue(tag);
            data.put(tag, defaultValue);
        }
    }

    public void setValue(String tag, Object value)
    {
        if(data.containsKey(tag))
            data.put(tag, value);
    }

    public Object getValue(String tag)
    {
        if(data.containsKey(tag))
            return data.get(tag);
        return null;
    }

    public boolean getBooleanValue(String tag)
    {
        if(getValue(tag) instanceof Boolean b)
            return b;
        return false;
    }

    public int getIntValue(String tag)
    {
        if(getValue(tag) instanceof Integer i)
            return i;
        return 0;
    }

    public long getLongValue(String tag)
    {
        if(getValue(tag) instanceof Long l)
            return l;
        return 0;
    }

    public float getFloatValue(String tag)
    {
        if(getValue(tag) instanceof Float f)
            return f;
        return 0f;
    }

    public String getStringValue(String tag)
    {
        if(getValue(tag) instanceof String s)
            return s;
        return "";
    }

    public CompoundTag getCompoundValue(String tag)
    {
        if(getValue(tag) instanceof CompoundTag c)
            return c;
        return new CompoundTag();
    }

    public void read(CompoundTag compound)
    {
        compound.getAllKeys().forEach(key ->{
            if(this.hasKey(key))
            {
                if(compound.contains(key, Tag.TAG_BYTE))
                    this.setValue(key, compound.getBoolean(key));
                else if(compound.contains(key, Tag.TAG_INT))
                    this.setValue(key, compound.getInt(key));
                else if(compound.contains(key, Tag.TAG_LONG))
                    this.setValue(key, compound.getLong(key));
                else if(compound.contains(key, Tag.TAG_FLOAT))
                    this.setValue(key, compound.getFloat(key));
                else if(compound.contains(key, Tag.TAG_STRING))
                    this.setValue(key, compound.getString(key));
                else if(compound.contains(key, Tag.TAG_COMPOUND))
                    this.setValue(key, compound.getCompound(key));
            }
        });
    }

    public CompoundTag writeToNBT() { return writeToNBT(null); }

    public CompoundTag writeToNBT(@Nullable UpgradeType source)
    {
        Map<String,Object> modifiedEntries = source == null ? this.data : getModifiedEntries(this,source);
        CompoundTag compound = new CompoundTag();
        modifiedEntries.forEach((key,value) ->{
            if(value instanceof Boolean)
                compound.putBoolean(key,(Boolean)value);
            if(value instanceof Integer)
                compound.putInt(key, (Integer)value);
            else if(value instanceof Float)
                compound.putFloat(key, (Float)value);
            else if(value instanceof Long)
                compound.putLong(key, (Long)value);
            else if(value instanceof String)
                compound.putString(key, (String)value);
            else if(value instanceof CompoundTag)
                compound.put(key, (CompoundTag)value);
        });
        return compound;
    }

    public static Map<String,Object> getModifiedEntries(UpgradeData queryData, UpgradeType source)
    {
        Map<String,Object> modifiedEntries = Maps.newHashMap();
        source.getDefaultData().data.forEach((key, value) -> {
            if(queryData.data.containsKey(key) && !Objects.equal(queryData.data.get(key), value))
                modifiedEntries.put(key, value);
        });
        return modifiedEntries;
    }

}