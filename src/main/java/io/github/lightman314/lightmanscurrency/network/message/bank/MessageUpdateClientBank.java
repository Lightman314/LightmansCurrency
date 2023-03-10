package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageUpdateClientBank {
	
	CompoundNBT traderData;
	
	public MessageUpdateClientBank(CompoundNBT traderData)
	{
		this.traderData = traderData;
	}
	
	public static void encode(MessageUpdateClientBank message, PacketBuffer buffer) {
		buffer.writeNbt(message.traderData);
	}

	public static MessageUpdateClientBank decode(PacketBuffer buffer) {
		return new MessageUpdateClientBank(buffer.readNbt());
	}

	public static void handle(MessageUpdateClientBank message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateBankAccount(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
