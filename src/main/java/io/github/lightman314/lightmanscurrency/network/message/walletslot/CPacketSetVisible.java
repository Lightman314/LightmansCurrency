package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketSetVisible extends ClientToServerPacket {

	public static final Handler<CPacketSetVisible> HANDLER = new H();

	private final boolean visible;
	
	public CPacketSetVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.visible);
	}

	private static class H extends Handler<CPacketSetVisible>
	{
		
		@Override
		public CPacketSetVisible decode(FriendlyByteBuf buffer) { return new CPacketSetVisible(buffer.readBoolean()); }
		@Override
		protected void handle(CPacketSetVisible message, Player player) {
            IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
            if(walletHandler != null)
                walletHandler.setVisible(message.visible);
		}
	}

}
