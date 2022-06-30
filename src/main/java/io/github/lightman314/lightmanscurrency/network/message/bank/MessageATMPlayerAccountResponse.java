package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.SelectionTab;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageATMPlayerAccountResponse {
	
	final MutableComponent message;
	
	public MessageATMPlayerAccountResponse(MutableComponent message)
	{
		this.message = message;
	}
	
	
	public static void encode(MessageATMPlayerAccountResponse message, FriendlyByteBuf buffer) {
		buffer.writeUtf(Component.Serializer.toJson(message.message));
	}

	public static MessageATMPlayerAccountResponse decode(FriendlyByteBuf buffer) {
		return new MessageATMPlayerAccountResponse(Component.Serializer.fromJson(buffer.readUtf()));
	}

	public static void handle(MessageATMPlayerAccountResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof ATMScreen)
			{
				ATMScreen screen = (ATMScreen)mc.screen;
				if(screen.currentTab() instanceof SelectionTab)
					((SelectionTab)screen.currentTab()).ReceiveSelectPlayerResponse(message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
