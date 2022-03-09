package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.IBankAccountTransferMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageBankTransferResponse {
	
	ITextComponent responseMessage;
	
	public MessageBankTransferResponse(ITextComponent responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public static void encode(MessageBankTransferResponse message, PacketBuffer buffer) {
		String json = ITextComponent.Serializer.toJson(message.responseMessage);
		buffer.writeInt(json.length());
		buffer.writeString(json);
	}

	public static MessageBankTransferResponse decode(PacketBuffer buffer) {
		int length = buffer.readInt();
		return new MessageBankTransferResponse(ITextComponent.Serializer.getComponentFromJson(buffer.readString(length)));
	}

	public static void handle(MessageBankTransferResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			ClientPlayerEntity player = minecraft.player;
			if(player != null)
			{
				if(player.openContainer instanceof IBankAccountTransferMenu)
				{
					IBankAccountTransferMenu menu = (IBankAccountTransferMenu)player.openContainer;
					menu.setMessage(message.responseMessage);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
