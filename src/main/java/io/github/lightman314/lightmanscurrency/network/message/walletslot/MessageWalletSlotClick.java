package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.EventHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageWalletSlotClick implements IMessage<MessageWalletSlotClick> {
	
	boolean hasShiftDown;
	
	public MessageWalletSlotClick()
	{
		
	}
	
	public MessageWalletSlotClick(boolean hasShiftDown)
	{
		this.hasShiftDown = hasShiftDown;
	}
	
	@Override
	public void encode(MessageWalletSlotClick message, PacketBuffer buffer) {
		buffer.writeBoolean(message.hasShiftDown);
	}

	@Override
	public MessageWalletSlotClick decode(PacketBuffer buffer) {
		return new MessageWalletSlotClick(buffer.readBoolean());
	}

	@Override
	public void handle(MessageWalletSlotClick message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
				EventHandler.onWalletSlotClick(player, message.hasShiftDown);
		});
		supplier.get().setPacketHandled(true);
	}

}
