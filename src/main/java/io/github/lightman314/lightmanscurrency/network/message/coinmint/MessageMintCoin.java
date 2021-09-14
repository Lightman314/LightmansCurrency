package io.github.lightman314.lightmanscurrency.network.message.coinmint;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageMintCoin implements IMessage<MessageMintCoin> {

	private boolean fullStack;
	
	public MessageMintCoin()
	{
		
	}
	
	public MessageMintCoin(boolean fullStack)
	{
		this.fullStack = fullStack;
	}
	
	
	@Override
	public void encode(MessageMintCoin message, PacketBuffer buffer) {
		buffer.writeBoolean(message.fullStack);
	}

	@Override
	public MessageMintCoin decode(PacketBuffer buffer) {
		return new MessageMintCoin(buffer.readBoolean());
	}

	@Override
	public void handle(MessageMintCoin message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof MintContainer)
				{
					MintContainer container = (MintContainer) entity.openContainer;
					if(container.validMintOutput() > 0)
					{
						container.mintCoins(message.fullStack);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
