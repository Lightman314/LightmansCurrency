package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageInitializeClientTraders {
	
	CompoundTag compound;
	
	public MessageInitializeClientTraders(CompoundTag compound)
	{
		this.compound = compound;
	}
	
	public static void encode(MessageInitializeClientTraders message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.compound);
	}

	public static MessageInitializeClientTraders decode(FriendlyByteBuf buffer) {
		return new MessageInitializeClientTraders(buffer.readNbt());
	}

	public static void handle(MessageInitializeClientTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.initializeTraders(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
