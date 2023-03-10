package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageInitializeClientBank {
	
	CompoundNBT compound;
	
	public MessageInitializeClientBank(CompoundNBT compound)
	{
		this.compound = compound;
	}
	
	public static void encode(MessageInitializeClientBank message, PacketBuffer buffer) {
		buffer.writeNbt(message.compound);
	}

	public static MessageInitializeClientBank decode(PacketBuffer buffer) {
		return new MessageInitializeClientBank(buffer.readNbt());
	}

	public static void handle(MessageInitializeClientBank message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.initializeBankAccounts(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
