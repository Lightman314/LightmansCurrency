package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketSetVisible extends ClientToServerPacket {

	private static final Type<CPacketSetVisible> TYPE = new Type<>(VersionUtil.lcResource("c_wallet_set_visible"));
	public static final Handler<CPacketSetVisible> HANDLER = new H();

	boolean visible;
	
	public CPacketSetVisible(boolean visible) { super(TYPE); this.visible = visible; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketSetVisible message) { buffer.writeBoolean(message.visible); }
	private static CPacketSetVisible decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSetVisible(buffer.readBoolean()); }

	private static class H extends Handler<CPacketSetVisible>
	{
		protected H() { super(TYPE, easyCodec(CPacketSetVisible::encode,CPacketSetVisible::decode)); }
		@Override
		protected void handle(@Nonnull CPacketSetVisible message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			WalletHandler walletHandler = WalletHandler.get(player);
			if(walletHandler != null)
				walletHandler.setVisible(message.visible);
		}
	}

}
