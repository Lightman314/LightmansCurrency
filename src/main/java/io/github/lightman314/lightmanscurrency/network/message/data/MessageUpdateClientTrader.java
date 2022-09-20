package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateClientTrader {
	
	CompoundTag traderData;
	
	public MessageUpdateClientTrader(CompoundTag traderData) { this.traderData = traderData; }
	
	public static void encode(MessageUpdateClientTrader message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.traderData);
	}

	public static MessageUpdateClientTrader decode(FriendlyByteBuf buffer) {
		return new MessageUpdateClientTrader(buffer.readAnySizeNbt());
	}

	public static void handle(MessageUpdateClientTrader message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTrader(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
