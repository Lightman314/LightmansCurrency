package io.github.lightman314.lightmanscurrency.network.message.data;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateClientTeam {
	
	CompoundTag traderData;
	
	public MessageUpdateClientTeam(CompoundTag traderData)
	{
		this.traderData = traderData;
	}
	
	public static void encode(MessageUpdateClientTeam message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.traderData);
	}

	public static MessageUpdateClientTeam decode(FriendlyByteBuf buffer) {
		return new MessageUpdateClientTeam(buffer.readNbt());
	}

	public static void handle(MessageUpdateClientTeam message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateTeam(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
