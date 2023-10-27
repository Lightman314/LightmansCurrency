package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncUsers extends ServerToClientPacket {

	public static final Handler<SPacketSyncUsers> HANDLER = new H();

	long traderID;
	int userCount;
	
	public SPacketSyncUsers(long traderID, int userCount)
	{
		this.traderID = traderID;
		this.userCount = userCount;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeLong(this.traderID);
		buffer.writeInt(this.userCount);
	}

	private static class H extends Handler<SPacketSyncUsers>
	{
		@Nonnull
		@Override
		public SPacketSyncUsers decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncUsers(buffer.readLong(), buffer.readInt()); }
		@Override
		protected void handle(@Nonnull SPacketSyncUsers message, @Nullable ServerPlayer sender) {
			TraderData trader = TraderSaveData.GetTrader(true, message.traderID);
			if(trader != null)
				trader.updateUserCount(message.userCount);
		}
	}

}
