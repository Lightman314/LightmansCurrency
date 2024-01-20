package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketChestQuickCollect extends ClientToServerPacket {

	public static final Handler<CPacketChestQuickCollect> HANDLER = new H();

	private final boolean allowSideChains;

	private CPacketChestQuickCollect(boolean allowSideChains) { this.allowSideChains = allowSideChains; }

	public static void sendToServer() { new CPacketChestQuickCollect(LCConfig.CLIENT.chestButtonAllowSideChains.get()).send(); }

	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeBoolean(this.allowSideChains); }

	private static class H extends Handler<CPacketChestQuickCollect>
	{
		@Nonnull
		@Override
		public CPacketChestQuickCollect decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketChestQuickCollect(buffer.readBoolean()); }
		@Override
		protected void handle(@Nonnull CPacketChestQuickCollect message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof ChestMenu menu)
					WalletItem.QuickCollect(sender, menu.getContainer(), message.allowSideChains);
			}
		}
	}

}
