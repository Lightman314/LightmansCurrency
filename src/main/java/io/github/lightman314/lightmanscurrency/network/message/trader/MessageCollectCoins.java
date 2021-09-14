package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainerPrimitive;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageCollectCoins implements IMessage<MessageCollectCoins> {
	
	public MessageCollectCoins()
	{
		
	}
	
	
	@Override
	public void encode(MessageCollectCoins message, FriendlyByteBuf buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageCollectCoins decode(FriendlyByteBuf buffer) {
		return new MessageCollectCoins();
	}

	@Override
	public void handle(MessageCollectCoins message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ITraderContainerPrimitive)
				{
					ITraderContainerPrimitive container = (ITraderContainerPrimitive) entity.containerMenu;
					container.CollectCoinStorage();
				}
				else
				{
					LightmansCurrency.LogWarning("MessageCollectCoins was sent from a client that does not have a trader container open.");
					if(entity.containerMenu != null)
						LightmansCurrency.LogWarning("OpenContainer: " + entity.containerMenu.getClass().getName());
					else
						LightmansCurrency.LogWarning("OpenContainer: NULL");
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
