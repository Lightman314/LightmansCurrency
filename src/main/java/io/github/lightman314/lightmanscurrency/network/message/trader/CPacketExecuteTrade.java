package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class CPacketExecuteTrade extends ClientToServerPacket {

	public static final Handler<CPacketExecuteTrade> HANDLER = new H();

	private final int trader;
	private final int tradeIndex;
	
	public CPacketExecuteTrade(int trader, int tradeIndex)
	{
		this.trader = trader;
		this.tradeIndex = tradeIndex;
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(this.trader);
		buffer.writeInt(this.tradeIndex);
	}

	private static class H extends Handler<CPacketExecuteTrade>
	{
		
		@Override
		public CPacketExecuteTrade decode(FriendlyByteBuf buffer) { return new CPacketExecuteTrade(buffer.readInt(), buffer.readInt()); }
		@Override
		protected void handle(CPacketExecuteTrade message, Player player) {
            if(player.containerMenu instanceof TraderMenu menu)
                menu.ExecuteTrade(message.trader, message.tradeIndex);
		}
	}

}
