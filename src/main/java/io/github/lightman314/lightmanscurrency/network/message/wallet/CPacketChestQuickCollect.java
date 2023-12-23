package io.github.lightman314.lightmanscurrency.network.message.wallet;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketChestQuickCollect extends ClientToServerPacket {

	public static final Handler<CPacketChestQuickCollect> HANDLER = new H();

	private final boolean allowHidden;

	private CPacketChestQuickCollect(boolean allowHidden) { this.allowHidden = allowHidden; }

	public static void sendToServer() { new CPacketChestQuickCollect(Config.CLIENT.chestButtonAllowHidden.get()).send(); }

	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeBoolean(this.allowHidden); }

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
					WalletItem.QuickCollect(sender, menu.getContainer(), message.allowHidden);
			}
		}
	}

}
