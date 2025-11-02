package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenStorage extends ClientToServerPacket {

	public static final Handler<CPacketOpenStorage> HANDLER = new H();

	private final long traderID;
	
	public CPacketOpenStorage(long traderID) { this.traderID = traderID; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<CPacketOpenStorage>
	{
		
		@Override
		public CPacketOpenStorage decode(FriendlyByteBuf buffer) { return new CPacketOpenStorage(buffer.readLong()); }
		@Override
		protected void handle(CPacketOpenStorage message, Player player) {
            if(player.containerMenu instanceof IValidatedMenu tm)
            {
                MenuValidator validator = tm.getValidator();
                TraderData trader = TraderAPI.getApi().GetTrader(false, message.traderID);
                if(trader != null)
                    trader.openStorageMenu(player, validator);
            }
		}
	}

}
