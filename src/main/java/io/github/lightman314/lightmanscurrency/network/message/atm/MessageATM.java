package io.github.lightman314.lightmanscurrency.network.message.atm;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
//import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageATM message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonHit);
	}

	@Override
	public MessageATM decode(FriendlyByteBuf buffer) {
		return new MessageATM(buffer.readInt());
	}

	@Override
	public void handle(MessageATM message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ATMContainer)
				{
					ATMContainer container = (ATMContainer) entity.containerMenu;
					container.ConvertCoins(message.buttonHit);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
