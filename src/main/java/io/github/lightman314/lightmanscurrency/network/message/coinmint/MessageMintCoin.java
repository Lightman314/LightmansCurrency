package io.github.lightman314.lightmanscurrency.network.message.coinmint;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
//import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageMintCoin message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.fullStack);
	}

	@Override
	public MessageMintCoin decode(FriendlyByteBuf buffer) {
		return new MessageMintCoin(buffer.readBoolean());
	}

	@Override
	public void handle(MessageMintCoin message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof MintContainer)
				{
					MintContainer container = (MintContainer) entity.containerMenu;
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
