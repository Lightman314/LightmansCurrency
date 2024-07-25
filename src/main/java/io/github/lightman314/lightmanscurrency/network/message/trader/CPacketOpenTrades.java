package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketOpenTrades extends ClientToServerPacket {

	public static final Handler<CPacketOpenTrades> HANDLER = new H();

	private final long traderID;

	public CPacketOpenTrades(long traderID) { this.traderID = traderID; }

	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<CPacketOpenTrades>
	{
		@Nonnull
		@Override
		public CPacketOpenTrades decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenTrades(buffer.readLong()); }
		@Override
		protected void handle(@Nonnull CPacketOpenTrades message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				MenuValidator validator = SimpleValidator.NULL;
				if(sender.containerMenu instanceof IValidatedMenu tm)
					validator = tm.getValidator();
				if(message.traderID < 0) //If trader ID is -1, open all network traders
					NetworkHooks.openScreen(sender, TraderData.getTraderMenuForAllNetworkTraders(validator), EasyMenu.encoder(validator));
				else
				{
					TraderData data = TraderSaveData.GetTrader(false, message.traderID);
					if(data != null)
						data.openTraderMenu(sender, validator);
				}
			}
		}
	}

}
