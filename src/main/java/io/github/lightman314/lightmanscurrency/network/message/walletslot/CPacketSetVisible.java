package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketSetVisible extends ClientToServerPacket {

	public static final Handler<CPacketSetVisible> HANDLER = new H();

	int entityID;
	boolean visible;
	
	public CPacketSetVisible(int entityID, boolean visible) {
		this.entityID = entityID;
		this.visible = visible;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeInt(this.entityID);
		buffer.writeBoolean(this.visible);
	}

	private static class H extends Handler<CPacketSetVisible>
	{
		@Nonnull
		@Override
		public CPacketSetVisible decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketSetVisible(buffer.readInt(), buffer.readBoolean()); }
		@Override
		protected void handle(@Nonnull CPacketSetVisible message, @Nullable ServerPlayer sender) {
			if(sender == null)
				return;
			Entity entity = sender.level.getEntity(message.entityID);
			if(entity != null)
			{
				IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
				if(walletHandler != null)
					walletHandler.setVisible(message.visible);
			}
		}
	}

}
