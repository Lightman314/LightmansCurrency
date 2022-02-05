package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateClientBank {
	
	CompoundNBT compound;
	
	public MessageUpdateClientBank(CompoundNBT compound)
	{
		this.compound = compound;
	}
	
	public static void encode(MessageUpdateClientBank message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.compound);
	}

	public static MessageUpdateClientBank decode(PacketBuffer buffer) {
		return new MessageUpdateClientBank(buffer.readCompoundTag());
	}

	public static void handle(MessageUpdateClientBank message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateBankAccount(message.compound));
		supplier.get().setPacketHandled(true);
	}

}
