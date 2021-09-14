package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.providers.WalletContainerProvider;
import io.github.lightman314.lightmanscurrency.items.WalletItem.DataWriter;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class MessageOpenWallet implements IMessage<MessageOpenWallet> {
	
	public MessageOpenWallet()
	{
		
	}
	
	@Override
	public void encode(MessageOpenWallet message, FriendlyByteBuf buffer) {
		//buffer.writeInt(message.playerId);
	}

	@Override
	public MessageOpenWallet decode(FriendlyByteBuf buffer) {
		return new MessageOpenWallet();
	}

	@Override
	public void handle(MessageOpenWallet message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				NetworkHooks.openGui(player, (MenuProvider)new WalletContainerProvider(-1), new DataWriter(-1));
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
