package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateClientBank {
	
	CompoundTag traderData;
	
	public MessageUpdateClientBank(CompoundTag traderData)
	{
		this.traderData = traderData;
	}
	
	public static void encode(MessageUpdateClientBank message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.traderData);
	}

	public static MessageUpdateClientBank decode(FriendlyByteBuf buffer) {
		return new MessageUpdateClientBank(buffer.readNbt());
	}

	public static void handle(MessageUpdateClientBank message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.updateBankAccount(message.traderData));
		supplier.get().setPacketHandled(true);
	}

}
