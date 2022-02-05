package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageATMConversion implements IMessage<MessageATMConversion> {
	
	private int buttonHit;
	
	public MessageATMConversion()
	{
		
	}
	
	public MessageATMConversion(int buttonHit)
	{
		this.buttonHit = buttonHit;
	}
	
	
	@Override
	public void encode(MessageATMConversion message, PacketBuffer buffer) {
		buffer.writeInt(message.buttonHit);
	}

	@Override
	public MessageATMConversion decode(PacketBuffer buffer) {
		return new MessageATMConversion(buffer.readInt());
	}

	@Override
	public void handle(MessageATMConversion message, Supplier<Context> supplier) {
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
