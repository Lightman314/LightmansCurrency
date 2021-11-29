package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.providers.WalletContainerProvider;
import io.github.lightman314.lightmanscurrency.items.WalletItem.DataWriter;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageOpenWallet implements IMessage<MessageOpenWallet> {
	
	public MessageOpenWallet()
	{
		
	}
	
	@Override
	public void encode(MessageOpenWallet message, PacketBuffer buffer) {
		
	}

	@Override
	public MessageOpenWallet decode(PacketBuffer buffer) {
		return new MessageOpenWallet();
	}

	@Override
	public void handle(MessageOpenWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider) new WalletContainerProvider(-1), new DataWriter(-1));
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
