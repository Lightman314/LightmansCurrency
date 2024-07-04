package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenWallet extends ClientToServerPacket {

	private static final Type<CPacketOpenWallet> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_wallet_open"));
	public static final Handler<CPacketOpenWallet> HANDLER = new H();

	private final int walletStackIndex;
	
	public CPacketOpenWallet(int walletStackIndex) { super(TYPE); this.walletStackIndex = walletStackIndex;  }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketOpenWallet message) { buffer.writeInt(message.walletStackIndex); }
	private static CPacketOpenWallet decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenWallet(buffer.readInt()); }

	private static class H extends Handler<CPacketOpenWallet>
	{
		protected H() { super(TYPE, easyCodec(CPacketOpenWallet::encode,CPacketOpenWallet::decode)); }
		@Override
		protected void handle(@Nonnull CPacketOpenWallet message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player instanceof ServerPlayer sp)
				WalletMenuBase.SafeOpenWalletMenu(sp, message.walletStackIndex);
		}
	}

}
