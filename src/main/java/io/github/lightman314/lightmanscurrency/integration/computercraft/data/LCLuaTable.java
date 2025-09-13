package io.github.lightman314.lightmanscurrency.integration.computercraft.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaTable;
import dan200.computercraft.api.lua.LuaValues;


/**
 * Copied from {@link com.simibubi.create.compat.computercraft.implementation.CreateLuaTable CreateLuaTable} with added LC utility constructors
 */
public class LCLuaTable implements LuaTable<Object, Object> {

    private final Map<Object, Object> map;

    public LCLuaTable() {
        this.map = new HashMap<>();
    }

    public LCLuaTable(Map<?, ?> map) {
        this.map = new HashMap<>(map);
    }

    //Unique to LC
    public static LCLuaTable fromTag(CompoundTag compound)
    {
        LCLuaTable table = new LCLuaTable();
        for(String key : compound.getAllKeys())
        {
            Tag tag = compound.get(key);
            table.put(key,parseTag(compound.get(key)));
        }
        return table;
    }

    public static Object parseTag(Tag tag)
    {
        if(tag instanceof ByteArrayTag t)
            return t.getAsByteArray();
        if(tag instanceof ByteTag t)
            return t.getAsByte();
        if(tag instanceof CompoundTag t)
            return fromTag(t);
        if(tag instanceof DoubleTag t)
            return t.getAsDouble();
        if(tag instanceof FloatTag t)
            return t.getAsFloat();
        if(tag instanceof IntArrayTag t)
            return t.getAsIntArray();
        if(tag instanceof IntTag t)
            return t.getAsInt();
        if(tag instanceof ListTag t)
        {
            List<Object> result = new ArrayList<>();
            for (Tag value : t)
                result.add(parseTag(value));
            return result.toArray(Object[]::new);
        }
        if(tag instanceof LongArrayTag t)
            return t.getAsLongArray();
        if(tag instanceof LongTag t)
            return t.getAsLong();
        if(tag instanceof ShortTag t)
            return t.getAsShort();
        if(tag instanceof StringTag t)
            return t.getAsString();
        return null;
    }

    public static LCLuaTable fromMoney(MoneyValue value) {
        LCLuaTable table = new LCLuaTable();
        table.put("numeric",value.getCoreValue());
        table.put("text",value.getText("Empty"));
        table.put("data",fromTag(value.save()));
        return table;
    }

    public static LCLuaTable fromPlayer(PlayerReference player) {
        LCLuaTable table = new LCLuaTable();
        table.put("Name",player.getName(false));
        table.put("ID",player.id);
        return table;
    }

    public boolean getBoolean(String key) throws LuaException {
        Object value = get(key);

        if (!(value instanceof Boolean))
            throw LuaValues.badField(key, "boolean", LuaValues.getType(value));

        return (Boolean) value;
    }

    public String getString(String key) throws LuaException {
        Object value = get(key);

        if (!(value instanceof String))
            throw LuaValues.badField(key, "string", LuaValues.getType(value));

        return (String) value;
    }

    public LCLuaTable getTable(String key) throws LuaException {
        Object value = get(key);

        if (!(value instanceof Map<?, ?>))
            throw LuaValues.badField(key, "table", LuaValues.getType(value));

        return new LCLuaTable((Map<?, ?>) value);
    }

    public Optional<Boolean> getOptBoolean(String key) throws LuaException {
        Object value = get(key);

        if (value == null)
            return Optional.empty();

        if (!(value instanceof Boolean))
            throw LuaValues.badField(key, "boolean", LuaValues.getType(value));

        return Optional.of((Boolean) value);
    }

    public Set<String> stringKeySet() throws LuaException {
        Set<String> stringSet = new HashSet<>();

        for (Object key : keySet()) {
            if (!(key instanceof String))
                throw new LuaException("key " + key + " is not string (got " + LuaValues.getType(key) + ")");

            stringSet.add((String) key);
        }

        return Collections.unmodifiableSet(stringSet);
    }

    public Collection<LCLuaTable> tableValues() throws LuaException {
        List<LCLuaTable> tables = new ArrayList<>();

        for (int i = 1; i <= size(); i++) {
            Object value = get((double) i);

            if (!(value instanceof Map<?, ?>))
                throw new LuaException("value " + value + " is not table (got " + LuaValues.getType(value) + ")");

            tables.add(new LCLuaTable((Map<?, ?>) value));
        }

        return Collections.unmodifiableList(tables);
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    @Nullable
    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public void putBoolean(String key, boolean value) {
        map.put(key, value);
    }

    public void putDouble(String key, double value) {
        map.put(key, value);
    }

    public void putString(String key, String value) {
        map.put(key, value);
    }

    public void putTable(String key, LCLuaTable value) {
        map.put(key, value);
    }

    public void putTable(int i, LCLuaTable value) {
        map.put(i, value);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public Object get(Object o) {
        return map.get(o);
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

}