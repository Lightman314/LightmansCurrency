package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenStorage extends ClientToServerPacket {

	private static final Type<CPacketOpenStorage> TYPE = new Type<>(VersionUtil.lcResource("c_trader_menu_storage"));
	public static final Handler<CPacketOpenStorage> HANDLER = new H();

	private final long traderID;
	
	public CPacketOpenStorage(long traderID) { super(TYPE); this.traderID = traderID; }
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketOpenStorage message) { buffer.writeLong(message.traderID); }
	private static CPacketOpenStorage decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenStorage(buffer.readLong()); }

	private static class H extends Handler<CPacketOpenStorage>
	{
		protected H() { super(TYPE, easyCodec(CPacketOpenStorage::encode,CPacketOpenStorage::decode)); }
		@Override
		protected void handle(@Nonnull CPacketOpenStorage message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			MenuValidator validator = SimpleValidator.NULL;
			if(player.containerMenu instanceof IValidatedMenu tm)
				validator = tm.getValidator();
			TraderData trader = TraderAPI.getApi().GetTrader(false, message.traderID);
			if(trader != null)
				trader.openStorageMenu(player, validator);
		}
	}

}
