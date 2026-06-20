package io.github.lightman314.lightmanscurrency.api.money.values;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Objects;

public final class MoneyKey {

    public static final Codec<MoneyKey> CODEC = Codec.STRING.comapFlatMap(string -> {
        try {return DataResult.success(fromString(string));
        } catch (IdentifierException e) { return DataResult.error(e::getMessage); }
    },MoneyKey::toString);

    private static final Map<InternKey,MoneyKey> VALUES = new MapMaker().weakValues().makeMap();

    private final Identifier type;
    public Identifier getType() { return this.type; }
    public boolean isType(MoneyValueType<?> type) { return this.type.equals(LCRegistries.Money.VALUE_TYPE.getKey(type)); }
    private final String key;
    public String getKey() { return this.key; }

    private MoneyKey(Identifier type,String key) { this.type = type; this.key = key; }

    public static MoneyKey create(MoneyValueType<?> type) { return create(type,""); }
    public static MoneyKey create(MoneyValueType<?> type,String key) { return create(LCRegistries.Money.VALUE_TYPE.getKey(type),key); }
    public static MoneyKey create(Identifier type) { return create(type,""); }
    public static MoneyKey create(Identifier type,String key) { return VALUES.computeIfAbsent(new InternKey(type,key),k -> new MoneyKey(k.type,k.key)); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MoneyKey k)
            return this.type.equals(k.type) && this.key.equals(k.key);
        return false;
    }
    @Override
    public int hashCode() { return Objects.hash(this.type,this.key); }
    @Override
    public String toString() { return this.key.isEmpty() ? this.type.toString() : this.type + ";" + this.key; }

    public static MoneyKey fromString(String string) throws IdentifierException
    {
        String[] split = string.split(";",2);
        if(split.length == 1)
            return create(Identifier.parse(split[0]));
        else
            return create(Identifier.parse(split[0]),split[1]);
    }

    private record InternKey(Identifier type, String key) {}

}
