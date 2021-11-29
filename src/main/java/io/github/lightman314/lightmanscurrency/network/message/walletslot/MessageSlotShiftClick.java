package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.EventHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSlotShiftClick implements IMessage<MessageSlotShiftClick> {
	
	int slotIndex;
	
	public MessageSlotShiftClick()
	{
		
	}
	
	public MessageSlotShiftClick(int slotIndex)
	{
		this.slotIndex = slotIndex;
	}
	
	@Override
	public void encode(MessageSlotShiftClick message, PacketBuffer buffer) {
		buffer.writeInt(message.slotIndex);
	}

	@Override
	public MessageSlotShiftClick decode(PacketBuffer buffer) {
		return new MessageSlotShiftClick(buffer.readInt());
	}

	@Override
	public void handle(MessageSlotShiftClick message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
				EventHandler.onSlotShiftClick(player, message.slotIndex);
		});
		supplier.get().setPacketHandled(true);
	}

}
