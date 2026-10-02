package io.github.lightman314.lightmanscurrency.api.helpers.keys;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.IdentifierException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Objects;

public final class DualKey {

    public static final Codec<DualKey> CODEC = Codec.STRING.comapFlatMap(string -> {
        try { return DataResult.success(parse(string));
        } catch (IdentifierException e) { return DataResult.error(e::getMessage); }
    }, DualKey::toString);
    public static StreamCodec<ByteBuf, DualKey> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(string -> {
        try{ return parse(string);
        } catch (IdentifierException e) { throw new DecoderException(e); }
    }, DualKey::toString);

    private static final Map<InternKey,DualKey> VALUES = new MapMaker().weakValues().makeMap();

    private final Identifier type;
    public Identifier getType() { return this.type; }
    public boolean isType(AbstractType<?> type) { return this.type.equals(type.getKey()); }
    public boolean isSameType(DualKey otherKey) { return this.type.equals(otherKey.type); }
    private final String key;
    public String getKey() { return this.key; }
    public boolean hasEmptyKey() { return this.key.isEmpty(); }
    public boolean hasSecondaryKey() { return !this.hasEmptyKey(); }

    public DualKey addToKey(String addition) { return create(this.type,this.key.isEmpty() ? addition : this.key + "/" + addition); }

    private DualKey(Identifier type, String key) { this.type = type; this.key = key; }

    public static DualKey create(AbstractType<?> type) { return create(type,""); }
    public static DualKey create(AbstractType<?> type,String key) { return create(type.getKey(),key); }
    public static DualKey create(Identifier type) { return create(type,""); }
    public static DualKey create(Identifier type,String key) { return VALUES.computeIfAbsent(new InternKey(type,key), k -> new DualKey(k.type,k.key)); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DualKey k)
            return this.type.equals(k.type) && this.key.equals(k.key);
        return false;
    }
    @Override
    public int hashCode() { return Objects.hash(this.type,this.key); }
    @Override
    public String toString() { return this.key.isEmpty() ? this.type.toString() : this.type + ";" + this.key; }

    public static DualKey parse(String string) throws IdentifierException
    {
        String[] split = string.split(";",2);
        if(split.length == 1)
            return create(Identifier.parse(split[0]));
        else
            return create(Identifier.parse(split[0]),split[1]);
    }

    private record InternKey(Identifier type, String key) {}

}
