package io.github.lightman314.lightmanscurrency.integration.computercraft.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Implementation of {@link DynamicOps} but encoding to/from
 */
public class TableOps implements DynamicOps<Object> {

    public static final DynamicOps<Object> INSTANCE = new TableOps();

    private TableOps() {}

    @Override
    public Object empty() { return new LCLuaTable(); }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Object input) {
        if(input instanceof Number n)
            return outOps.createNumeric(n);
        if(input instanceof String s)
            return outOps.createString(s);
        if(input instanceof Map<?,?> table)
        {
            LCLuaTable t = new LCLuaTable(table);
            //If length is greater than 0, turn it into a list
            if(t.length() > 0)
            {
                List<U> list = new ArrayList<>();
                for(int i = 1; i <= t.length(); ++i)
                    list.add(this.convertTo(outOps,t.get(i)));
                return outOps.createList(list.stream());
            }
            //Otherwise it's just a map
            else
            {
                Map<U,U> map = new HashMap<>();
                t.forEach((key,value) -> map.put(this.convertTo(outOps,key),this.convertTo(outOps,input)));
                return outOps.createMap(map);
            }
        }
        if(input instanceof Object[] array)
        {
            List<U> list = new ArrayList<>();
            for (Object o : array)
                list.add(this.convertTo(outOps,o));
            return outOps.createList(list.stream());
        }
        return outOps.empty();
    }

    @Override
    public DataResult<Number> getNumberValue(Object input) {
        if(input instanceof Number n)
            return DataResult.success(n);
        return DataResult.error(() -> "Not a number");
    }

    @Override
    public Object createNumeric(Number i) { return i; }

    @Override
    public DataResult<String> getStringValue(Object input) {
        if(input instanceof String s)
            return DataResult.success(s);
        return DataResult.error(() -> "Not a string");
    }

    @Override
    public Object createString(String value) { return value; }

    @Override
    public DataResult<Object> mergeToList(Object list, Object value) {
        if(list instanceof Map<?,?> table)
        {
            LCLuaTable newTable = new LCLuaTable(table);
            newTable.add(value);
            return DataResult.success(newTable);
        }
        return DataResult.error(() -> "Not a list");
    }

    @Override
    public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
        if(map instanceof Map<?,?> table)
        {
            LCLuaTable newTable = new LCLuaTable(table);
            newTable.put(key,value);
            return DataResult.success(newTable);
        }
        return DataResult.error(() -> "Not a map");
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
        if(input instanceof Map<?,?> table)
            return DataResult.success(table.entrySet().stream().map(e -> Pair.of(e.getKey(),e.getValue())));
        return DataResult.error(() -> "Not a map");
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> map) {
        LCLuaTable table = new LCLuaTable();
        for(var entry : map.toList())
            table.put(entry.getFirst(),entry.getSecond());
        return table;
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object input) {
        if(input instanceof Map<?,?> table)
        {
            List<Object> list = new ArrayList<>();
            for(int i = 1; table.containsKey(i); ++i)
                list.add(table.get(i));
            return DataResult.success(list.stream());
        }

        return DataResult.error(() -> "Not a list");
    }

    @Override
    public Object createList(Stream<Object> input) { return new LCLuaTable(); }

    @Override
    public Object remove(Object input, String key) {
        if(input instanceof Map<?,?> map)
        {
            LCLuaTable table = new LCLuaTable(map);
            table.remove(key);
            return table;
        }
        return input;
    }

    @Override
    public String toString() { return "LuaTable"; }

}
