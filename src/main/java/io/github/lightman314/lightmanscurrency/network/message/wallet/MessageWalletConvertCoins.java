package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
//import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageWalletConvertCoins implements IMessage<MessageWalletConvertCoins> {
	
	public MessageWalletConvertCoins()
	{
		
	}
	
	
	@Override
	public void encode(MessageWalletConvertCoins message, FriendlyByteBuf buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageWalletConvertCoins decode(FriendlyByteBuf buffer) {
		return new MessageWalletConvertCoins();
	}

	@Override
	public void handle(MessageWalletConvertCoins message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof WalletContainer)
				{
					WalletContainer container = (WalletContainer) entity.containerMenu;
					container.ConvertCoins();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
