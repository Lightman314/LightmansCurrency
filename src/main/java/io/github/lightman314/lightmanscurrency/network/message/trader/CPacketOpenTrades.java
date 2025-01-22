package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenTrades extends ClientToServerPacket {

	private static final Type<CPacketOpenTrades> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_trader_menu_trades"));
	public static final Handler<CPacketOpenTrades> HANDLER = new H();

	private final long traderID;

	public CPacketOpenTrades(long traderID) { super(TYPE); this.traderID = traderID; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketOpenTrades message) { buffer.writeLong(message.traderID); }
	private static CPacketOpenTrades decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenTrades(buffer.readLong()); }

	private static class H extends Handler<CPacketOpenTrades>
	{
		protected H() { super(TYPE, easyCodec(CPacketOpenTrades::encode,CPacketOpenTrades::decode)); }
		@Override
		protected void handle(@Nonnull CPacketOpenTrades message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			MenuValidator validator = SimpleValidator.NULL;
			if(player.containerMenu instanceof IValidatedMenu tm)
				validator = tm.getValidator();
			if(message.traderID < 0) //If trader ID is -1, open all network traders
				player.openMenu(TraderData.getTraderMenuForAllNetworkTraders(validator), EasyMenu.encoder(validator));
			else
			{
				TraderData data = TraderAPI.API.GetTrader(false, message.traderID);
				if(data != null)
					data.openTraderMenu(player, validator);
			}
		}
	}

}
