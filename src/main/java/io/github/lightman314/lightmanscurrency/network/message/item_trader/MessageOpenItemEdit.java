package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemEditCapable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageOpenItemEdit {
	
	int tradeIndex;
	
	public MessageOpenItemEdit(int tradeIndex)
	{
		this.tradeIndex = tradeIndex;
	}
	
	public static void encode(MessageOpenItemEdit message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.tradeIndex);
	}

	public static MessageOpenItemEdit decode(FriendlyByteBuf buffer) {
		return new MessageOpenItemEdit(buffer.readInt());
	}

	public static void handle(MessageOpenItemEdit message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof IItemEditCapable)
				{
					IItemEditCapable menu = (IItemEditCapable)entity.containerMenu;
					menu.openItemEditScreenForTrade(message.tradeIndex);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
