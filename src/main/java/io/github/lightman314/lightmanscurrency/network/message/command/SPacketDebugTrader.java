package io.github.lightman314.lightmanscurrency.network.message.command;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketDebugTrader extends ServerToClientPacket {

	public static final Handler<SPacketDebugTrader> HANDLER = new H();

	final long traderID;
	
	public SPacketDebugTrader(long traderID) { this.traderID = traderID; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<SPacketDebugTrader>
	{
		@Nonnull
		@Override
		public SPacketDebugTrader decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketDebugTrader(buffer.readLong());}
		@Override
		protected void handle(@Nonnull SPacketDebugTrader message, @Nullable ServerPlayer sender) {
			TraderData trader = TraderSaveData.GetTrader(true, message.traderID);
			if(trader == null)
				LightmansCurrency.LogInfo("Client is missing trader with id " + message.traderID + "!");
			else
				LightmansCurrency.LogInfo("Client Trader NBT for trader " + message.traderID + ":\n" + trader.save());
		}
	}
	
}
