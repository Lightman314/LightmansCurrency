package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.SelectionTab;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageATMPlayerAccountResponse {
	
	final IFormattableTextComponent message;
	
	public MessageATMPlayerAccountResponse(IFormattableTextComponent message)
	{
		this.message = message;
	}
	
	
	public static void encode(MessageATMPlayerAccountResponse message, PacketBuffer buffer) {
		buffer.writeUtf(ITextComponent.Serializer.toJson(message.message));
	}

	public static MessageATMPlayerAccountResponse decode(PacketBuffer buffer) {
		return new MessageATMPlayerAccountResponse(ITextComponent.Serializer.fromJson(buffer.readUtf()));
	}

	public static void handle(MessageATMPlayerAccountResponse message, Supplier<NetworkEvent.Context> supplier) {
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
