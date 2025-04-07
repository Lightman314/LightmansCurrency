package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.api.money.bank.menu.IBankAccountAdvancedMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketBankTransferResponse extends ServerToClientPacket {

	private static final Type<SPacketBankTransferResponse> TYPE = new Type<>(VersionUtil.lcResource("s_bank_transfer_reply"));
	public static final Handler<SPacketBankTransferResponse> HANDLER = new H();

	Component responseMessage;
	
	public SPacketBankTransferResponse(Component responseMessage) {
		super(TYPE);
		this.responseMessage = responseMessage;
	}
	
	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketBankTransferResponse message) {
		ComponentSerialization.STREAM_CODEC.encode(buffer, message.responseMessage);
	}
	private static SPacketBankTransferResponse decode(@Nonnull RegistryFriendlyByteBuf buffer) { return  new SPacketBankTransferResponse(ComponentSerialization.STREAM_CODEC.decode(buffer)); }

	private static class H extends Handler<SPacketBankTransferResponse>
	{
		protected H() { super(TYPE, fancyCodec(SPacketBankTransferResponse::encode,SPacketBankTransferResponse::decode)); }
		@Override
		protected void handle(@Nonnull SPacketBankTransferResponse message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof IBankAccountAdvancedMenu menu)
				menu.setTransferMessage(message.responseMessage);
		}
	}

}
