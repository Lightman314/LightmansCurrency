package io.github.lightman314.lightmanscurrency.api.helpers.network;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PacketList<T> implements Iterable<T> {

    public static final StreamCodec<RegistryFriendlyByteBuf,PacketList<?>> STREAM_CODEC = StreamCodec.of((buf,list) -> {
        buf.writeInt(list.list.size());
        buf.writeInt(LCRegistries.Network.PACKET_TYPE.getId(list.listType));
        list.encodeList(buf);
    },(buf) -> {
        int count = buf.readInt();
        List<Object> list = new ArrayList<>();
        FancyPacketType<?> type = LCRegistries.Network.PACKET_TYPE.byId(buf.readInt());
        if(count > 0)
        {
            for(int i = 0; i < count; ++i)
                list.add(type.codec().decode(buf));
        }
        return parse(type,list);
    });

    public final FancyPacketType<T> listType;
    public final List<T> list;
    public PacketList(FancyPacketType<T> type,List<T> list)
    {
        this.listType = type;
        this.list = new ArrayList<>(list);
    }

    public boolean isEmpty() { return this.list.isEmpty(); }

    public void add(T entry) { this.list.add(entry); }

    private void encodeList(RegistryFriendlyByteBuf buf)
    {
        for(T entry : this.list)
            this.listType.codec().encode(buf,entry);
    }

    public PacketList<T> copy() { return new PacketList<>(this.listType,list); }

    public <X> PacketList<X> forceType(FancyPacketType<X> type)
    {
        //If the type matches, return this
        if(this.listType == type)
            return (PacketList<X>)this;
        //Otherwise return an empty list
        return new PacketList<>(type,new ArrayList<>());
    }

    @Override
    public Iterator<T> iterator() { return this.list.iterator(); }

    private static <T> PacketList<T> parse(FancyPacketType<T> type,List<?> list) { return new PacketList<>(type,forceCastList(type,list)); }
    private static <T> List<T> forceCastList(FancyPacketType<T> type, List<?> list)
    {
        List<T> newList = new ArrayList<>();
        try {
            for(Object o : list)
                newList.add((T)o);
        } catch (ClassCastException e) { throw new DecoderException(e); }
        return newList;
    }

}