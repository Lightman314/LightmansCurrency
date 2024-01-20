package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketBankTransferResponse extends ServerToClientPacket {

	public static final Handler<SPacketBankTransferResponse> HANDLER = new H();

	MutableComponent responseMessage;
	
	public SPacketBankTransferResponse(MutableComponent responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		String json = Component.Serializer.toJson(this.responseMessage);
		buffer.writeInt(json.length());
		buffer.writeUtf(json);
	}

	private static class H extends Handler<SPacketBankTransferResponse>
	{
		@Nonnull
		@Override
		public SPacketBankTransferResponse decode(@Nonnull FriendlyByteBuf buffer) {
			int length = buffer.readInt();
			return new SPacketBankTransferResponse(Component.Serializer.fromJson(buffer.readUtf(length)));
		}
		@Override
		protected void handle(@Nonnull SPacketBankTransferResponse message, @Nullable ServerPlayer sender) {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if(player != null)
			{
				if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
					menu.setTransferMessage(message.responseMessage);
			}
		}
	}

}
