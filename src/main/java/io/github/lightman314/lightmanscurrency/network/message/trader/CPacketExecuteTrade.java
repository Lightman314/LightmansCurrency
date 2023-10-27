package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketExecuteTrade extends ClientToServerPacket {

	public static final Handler<CPacketExecuteTrade> HANDLER = new H();

	private final int trader;
	private final int tradeIndex;
	
	public CPacketExecuteTrade(int trader, int tradeIndex)
	{
		this.trader = trader;
		this.tradeIndex = tradeIndex;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeInt(this.trader);
		buffer.writeInt(this.tradeIndex);
	}

	private static class H extends Handler<CPacketExecuteTrade>
	{
		@Nonnull
		@Override
		public CPacketExecuteTrade decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketExecuteTrade(buffer.readInt(), buffer.readInt()); }
		@Override
		protected void handle(@Nonnull CPacketExecuteTrade message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof TraderMenu menu)
					menu.ExecuteTrade(message.trader, message.tradeIndex);
			}
		}
	}

}
