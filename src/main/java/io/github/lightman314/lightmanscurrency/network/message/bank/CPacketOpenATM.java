package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.items.PortableATMItem;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPacketOpenATM extends ClientToServerPacket.Simple {

	private static final CPacketOpenATM INSTANCE = new CPacketOpenATM();

	public static Handler<CPacketOpenATM> HANDLER = new H();

	public static void sendToServer() { INSTANCE.send(); }

	private CPacketOpenATM() {}

	private static class H extends SimpleHandler<CPacketOpenATM>
	{
		protected H() { super(INSTANCE); }

		@Override
		public void handle(@Nonnull CPacketOpenATM message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(QuarantineAPI.IsDimensionQuarantined(sender))
					EasyText.sendMessage(sender, LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
				else
					NetworkHooks.openScreen(sender, PortableATMItem.getMenuProvider(), EasyMenu.nullEncoder());
			}
		}
	}

}
