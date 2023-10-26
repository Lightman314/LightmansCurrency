package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketMessageRemoveClientTrader extends ServerToClientPacket {

	public static final Handler<SPacketMessageRemoveClientTrader> HANDLER = new H();

	long traderID;
	
	public SPacketMessageRemoveClientTrader(long traderID)
	{
		this.traderID = traderID;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<SPacketMessageRemoveClientTrader>
	{
		@Nonnull
		@Override
		public SPacketMessageRemoveClientTrader decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketMessageRemoveClientTrader(buffer.readLong()); }
		@Override
		protected void handle(@Nonnull SPacketMessageRemoveClientTrader message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.removeTrader(message.traderID);
		}
	}

}
