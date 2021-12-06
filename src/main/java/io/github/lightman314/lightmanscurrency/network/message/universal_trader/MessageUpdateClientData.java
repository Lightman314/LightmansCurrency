package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateClientData {
	
	CompoundTag traderData;
	
	public MessageUpdateClientData(CompoundTag traderData)
	{
		this.traderData = traderData;
	}
	
	public static void encode(MessageUpdateClientData message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.traderData);
	}

	public static MessageUpdateClientData decode(FriendlyByteBuf buffer) {
		return new MessageUpdateClientData(buffer.readNbt());
	}

	public static void handle(MessageUpdateClientData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTrader(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
