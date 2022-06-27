package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountAdvancedMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageBankTransferResponse {
	
	MutableComponent responseMessage;
	
	public MessageBankTransferResponse(MutableComponent responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public static void encode(MessageBankTransferResponse message, FriendlyByteBuf buffer) {
		String json = Component.Serializer.toJson(message.responseMessage);
		buffer.writeInt(json.length());
		buffer.writeUtf(json);
	}

	public static MessageBankTransferResponse decode(FriendlyByteBuf buffer) {
		int length = buffer.readInt();
		return new MessageBankTransferResponse(Component.Serializer.fromJson(buffer.readUtf(length)));
	}

	public static void handle(MessageBankTransferResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu)
				{
					IBankAccountAdvancedMenu menu = (IBankAccountAdvancedMenu)player.containerMenu;
					menu.setTransferMessage(message.responseMessage);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
