package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CPacketChestQuickCollect extends ClientToServerPacket {

	public static final Handler<CPacketChestQuickCollect> HANDLER = new H();

	private final boolean allowSideChains;

	private CPacketChestQuickCollect(boolean allowSideChains) { this.allowSideChains = allowSideChains; }

	public static void sendToServer() { new CPacketChestQuickCollect(LCConfig.CLIENT.chestButtonAllowSideChains.get()).send(); }

	public void encode(FriendlyByteBuf buffer) { buffer.writeBoolean(this.allowSideChains); }

	private static class H extends Handler<CPacketChestQuickCollect>
	{
		@Override
		public CPacketChestQuickCollect decode(FriendlyByteBuf buffer) { return new CPacketChestQuickCollect(buffer.readBoolean()); }
		@Override
		protected void handle(CPacketChestQuickCollect message, Player player) {
            if(player.containerMenu instanceof ChestMenu menu)
                WalletItem.QuickCollect(player, menu.getContainer(), message.allowSideChains);
		}
	}

}
