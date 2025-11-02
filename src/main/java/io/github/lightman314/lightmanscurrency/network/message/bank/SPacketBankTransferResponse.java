package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketBankTransferResponse extends ServerToClientPacket {

	public static final Handler<SPacketBankTransferResponse> HANDLER = new H();

	MutableComponent responseMessage;
	
	public SPacketBankTransferResponse(MutableComponent responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		String json = Component.Serializer.toJson(this.responseMessage);
		buffer.writeInt(json.length());
		buffer.writeUtf(json);
	}

	private static class H extends Handler<SPacketBankTransferResponse>
	{
		@Override
		public SPacketBankTransferResponse decode(FriendlyByteBuf buffer) {
			int length = buffer.readInt();
			return new SPacketBankTransferResponse(Component.Serializer.fromJson(buffer.readUtf(length)));
		}
		@Override
		protected void handle(SPacketBankTransferResponse message, Player player) {
            if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
                menu.setTransferMessage(message.responseMessage);
		}
	}

}
