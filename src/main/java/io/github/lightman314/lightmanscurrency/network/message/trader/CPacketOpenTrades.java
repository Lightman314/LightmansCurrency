package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenTrades extends ClientToServerPacket {

	public static final Handler<CPacketOpenTrades> HANDLER = new H();

	private final long traderID;

	public CPacketOpenTrades(long traderID) { this.traderID = traderID; }

	public void encode(FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<CPacketOpenTrades>
	{
		@Override
		public CPacketOpenTrades decode(FriendlyByteBuf buffer) { return new CPacketOpenTrades(buffer.readLong()); }
		@Override
		protected void handle(CPacketOpenTrades message, Player player) {
            if(player.containerMenu instanceof IValidatedMenu tm)
            {
                MenuValidator validator = tm.getValidator();
                if(message.traderID < 0 && player instanceof ServerPlayer sp) //If trader ID is -1, open all network traders
                    NetworkHooks.openScreen(sp, TraderData.getTraderMenuForAllNetworkTraders(validator), EasyMenu.encoder(validator));
                else
                {
                    TraderData data = TraderAPI.getApi().GetTrader(false, message.traderID);
                    if(data != null)
                        data.openTraderMenu(player, validator);
                }
            }
		}
	}

}
