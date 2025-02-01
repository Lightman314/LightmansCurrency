package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketOpenStorage extends ClientToServerPacket {

	public static final Handler<CPacketOpenStorage> HANDLER = new H();

	private final long traderID;
	
	public CPacketOpenStorage(long traderID) { this.traderID = traderID; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

	private static class H extends Handler<CPacketOpenStorage>
	{
		@Nonnull
		@Override
		public CPacketOpenStorage decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenStorage(buffer.readLong()); }
		@Override
		protected void handle(@Nonnull CPacketOpenStorage message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				MenuValidator validator = SimpleValidator.NULL;
				if(sender.containerMenu instanceof IValidatedMenu tm)
					validator = tm.getValidator();
				TraderData trader = TraderAPI.API.GetTrader(false, message.traderID);
				if(trader != null)
					trader.openStorageMenu(sender, validator);
			}
		}
	}

}
