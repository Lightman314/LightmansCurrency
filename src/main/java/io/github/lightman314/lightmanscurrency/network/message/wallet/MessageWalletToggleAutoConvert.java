package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageWalletToggleAutoConvert implements IMessage<MessageWalletToggleAutoConvert> {
	
	public MessageWalletToggleAutoConvert()
	{
		
	}
	
	
	@Override
	public void encode(MessageWalletToggleAutoConvert message, PacketBuffer buffer) {
		//buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageWalletToggleAutoConvert decode(PacketBuffer buffer) {
		return new MessageWalletToggleAutoConvert();
	}

	@Override
	public void handle(MessageWalletToggleAutoConvert message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.openContainer instanceof WalletContainer)
				{
					WalletContainer container = (WalletContainer) entity.openContainer;
					container.ToggleAutoConvert();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
