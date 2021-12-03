package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageWalletConvertCoins implements IMessage<MessageWalletConvertCoins> {
	
	public MessageWalletConvertCoins()
	{
		
	}
	
	
	@Override
	public void encode(MessageWalletConvertCoins message, PacketBuffer buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageWalletConvertCoins decode(PacketBuffer buffer) {
		return new MessageWalletConvertCoins();
	}

	@Override
	public void handle(MessageWalletConvertCoins message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof WalletContainer)
				{
					WalletContainer container = (WalletContainer) entity.openContainer;
					container.ConvertCoins();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
