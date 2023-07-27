package io.github.lightman314.lightmanscurrency.network.message.tax;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageUpdateClientTax {

    private final CompoundTag updateTag;
    public MessageUpdateClientTax(CompoundTag updateTag) { this.updateTag = updateTag; }

    public static void encode(MessageUpdateClientTax message, FriendlyByteBuf buffer) { buffer.writeNbt(message.updateTag); }

    public static MessageUpdateClientTax decode(FriendlyByteBuf buffer) { return new MessageUpdateClientTax(buffer.readAnySizeNbt()); }

    public static void handle(MessageUpdateClientTax message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTaxEntries(message.updateTag));
        supplier.get().setPacketHandled(true);
    }

}
