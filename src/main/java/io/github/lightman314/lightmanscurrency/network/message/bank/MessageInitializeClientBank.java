package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageInitializeClientBank {
	
	CompoundTag compound;
	
	public MessageInitializeClientBank(CompoundTag compound)
	{
		this.compound = compound;
	}
	
	public static void encode(MessageInitializeClientBank message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.compound);
	}

	public static MessageInitializeClientBank decode(FriendlyByteBuf buffer) {
		return new MessageInitializeClientBank(buffer.readNbt());
	}

	public static void handle(MessageInitializeClientBank message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.initializeBankAccounts(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
