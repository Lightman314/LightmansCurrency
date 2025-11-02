package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncUsers extends ServerToClientPacket {

	public static final Handler<SPacketSyncUsers> HANDLER = new H();

	long traderID;
	int userCount;
	
	public SPacketSyncUsers(long traderID, int userCount)
	{
		this.traderID = traderID;
		this.userCount = userCount;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeLong(this.traderID);
		buffer.writeInt(this.userCount);
	}

	private static class H extends Handler<SPacketSyncUsers>
	{
		@Override
		public SPacketSyncUsers decode(FriendlyByteBuf buffer) { return new SPacketSyncUsers(buffer.readLong(), buffer.readInt()); }
		@Override
		protected void handle(SPacketSyncUsers message, Player player) {
			TraderData trader = TraderAPI.getApi().GetTrader(true, message.traderID);
			if(trader != null)
				trader.updateUserCount(message.userCount);
		}
	}

}
