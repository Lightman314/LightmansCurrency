package io.github.lightman314.lightmanscurrency.network.message.bank;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.IBankAccountAdvancedMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageBankTransferResponse {
	
	IFormattableTextComponent responseMessage;
	
	public MessageBankTransferResponse(IFormattableTextComponent responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public static void encode(MessageBankTransferResponse message, PacketBuffer buffer) {
		String json = ITextComponent.Serializer.toJson(message.responseMessage);
		buffer.writeInt(json.length());
		buffer.writeUtf(json);
	}

	public static MessageBankTransferResponse decode(PacketBuffer buffer) {
		int length = buffer.readInt();
		return new MessageBankTransferResponse(ITextComponent.Serializer.fromJson(buffer.readUtf(length)));
	}

	public static void handle(MessageBankTransferResponse message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			ClientPlayerEntity player = minecraft.player;
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
