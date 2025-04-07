package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenWalletBank extends ClientToServerPacket {

	private static final Type<CPacketOpenWalletBank> TYPE = new Type<>(VersionUtil.lcResource("c_wallet_open_bank"));
	public static final Handler<CPacketOpenWalletBank> HANDLER = new H();

	private final int walletStackIndex;
	
	public CPacketOpenWalletBank(int walletStackIndex) { super(TYPE); this.walletStackIndex = walletStackIndex;  }
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketOpenWalletBank message) { buffer.writeInt(message.walletStackIndex); }
	private static CPacketOpenWalletBank decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenWalletBank(buffer.readInt()); }

	private static class H extends Handler<CPacketOpenWalletBank>
	{
		protected H() { super(TYPE, easyCodec(CPacketOpenWalletBank::encode,CPacketOpenWalletBank::decode)); }
		@Override
		protected void handle(@Nonnull CPacketOpenWalletBank message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player instanceof ServerPlayer sp)
				WalletMenuBase.SafeOpenWalletBankMenu(sp, message.walletStackIndex);
		}
	}

}
