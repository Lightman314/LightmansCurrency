package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
//import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageWalletToggleAutoConvert implements IMessage<MessageWalletToggleAutoConvert> {
	
	public MessageWalletToggleAutoConvert()
	{
		
	}
	
	
	@Override
	public void encode(MessageWalletToggleAutoConvert message, FriendlyByteBuf buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageWalletToggleAutoConvert decode(FriendlyByteBuf buffer) {
		return new MessageWalletToggleAutoConvert();
	}

	@Override
	public void handle(MessageWalletToggleAutoConvert message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof WalletContainer)
				{
					WalletContainer container = (WalletContainer) entity.containerMenu;
					container.ToggleAutoConvert();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
