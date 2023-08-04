package io.github.lightman314.lightmanscurrency.network.message.tax;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageRemoveClientTax {

    private final long id;
    public MessageRemoveClientTax(long id) { this.id = id; }

    public static void encode(MessageRemoveClientTax message, FriendlyByteBuf buffer) { buffer.writeLong(message.id); }
    public static MessageRemoveClientTax decode(FriendlyByteBuf buffer) { return new MessageRemoveClientTax(buffer.readLong()); }

    public static void handle(MessageRemoveClientTax message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.removeTaxEntry(message.id));
        supplier.get().setPacketHandled(true);
    }

}