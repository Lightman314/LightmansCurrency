package io.github.lightman314.lightmanscurrency.network.message.teams;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageInitializeClientTeams {
	
	CompoundTag compound;
	
	public MessageInitializeClientTeams(CompoundTag compound)
	{
		this.compound = compound;
	}
	
	public static void encode(MessageInitializeClientTeams message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.compound);
	}

	public static MessageInitializeClientTeams decode(FriendlyByteBuf buffer) {
		return new MessageInitializeClientTeams(buffer.readNbt());
	}

	public static void handle(MessageInitializeClientTeams message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.initializeTeams(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
