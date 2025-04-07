package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncUsers extends ServerToClientPacket {

	private static final Type<SPacketSyncUsers> TYPE = new Type<>(VersionUtil.lcResource("s_trader_sync_users"));
	public static final Handler<SPacketSyncUsers> HANDLER = new H();

	long traderID;
	int userCount;
	
	public SPacketSyncUsers(long traderID, int userCount)
	{
		super(TYPE);
		this.traderID = traderID;
		this.userCount = userCount;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncUsers message) {
		buffer.writeLong(message.traderID);
		buffer.writeInt(message.userCount);
	}
	private static SPacketSyncUsers decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncUsers(buffer.readLong(), buffer.readInt()); }

	private static class H extends Handler<SPacketSyncUsers>
	{
		protected H() { super(TYPE, easyCodec(SPacketSyncUsers::encode,SPacketSyncUsers::decode)); }
		@Override
		protected void handle(@Nonnull SPacketSyncUsers message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			TraderData trader = TraderAPI.API.GetTrader(true, message.traderID);
			if(trader != null)
				trader.updateUserCount(message.userCount);
		}
	}

}
