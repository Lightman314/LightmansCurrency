package io.github.lightman314.lightmanscurrency.network.message.atm;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageATM implements IMessage<MessageATM> {
	
	private int buttonHit;
	
	public MessageATM()
	{
		
	}
	
	public MessageATM(int buttonHit)
	{
		this.buttonHit = buttonHit;
	}
	
	
	@Override
	public void encode(MessageATM message, PacketBuffer buffer) {
		buffer.writeInt(message.buttonHit);
	}

	@Override
	public MessageATM decode(PacketBuffer buffer) {
		return new MessageATM(buffer.readInt());
	}

	@Override
	public void handle(MessageATM message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof ATMContainer)
				{
					ATMContainer container = (ATMContainer) entity.openContainer;
					container.ConvertCoins(message.buttonHit);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
