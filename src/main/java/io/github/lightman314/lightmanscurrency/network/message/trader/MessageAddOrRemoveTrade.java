package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.interfaces.ICreativeTraderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade {
	
	public boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade(boolean isTradeAdd)
	{
		this.isTradeAdd = isTradeAdd;
	}
	
	public static void encode(MessageAddOrRemoveTrade message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.isTradeAdd);
	}

	public static MessageAddOrRemoveTrade decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveTrade(buffer.readBoolean());
	}

	public static void handle(MessageAddOrRemoveTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				if(entity.containerMenu instanceof ICreativeTraderMenu)
				{
					ICreativeTraderMenu menu = (ICreativeTraderMenu)entity.containerMenu;
					if(message.isTradeAdd)
						menu.AddTrade();
					else
						menu.RemoveTrade();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
