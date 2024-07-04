package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketATMSetPlayerAccount extends ClientToServerPacket {

	private static final Type<CPacketATMSetPlayerAccount> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_atm_select_account_player"));
	public static final Handler<CPacketATMSetPlayerAccount> HANDLER = new H();

	private final String playerName;
	
	public CPacketATMSetPlayerAccount(String playerName) { super(TYPE); this.playerName = playerName; }
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketATMSetPlayerAccount message) { buffer.writeUtf(message.playerName); }
	private static CPacketATMSetPlayerAccount decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketATMSetPlayerAccount(buffer.readUtf()); }

	private static class H extends Handler<CPacketATMSetPlayerAccount>
	{
		protected H() { super(TYPE, easyCodec(CPacketATMSetPlayerAccount::encode,CPacketATMSetPlayerAccount::decode)); }

		@Override
		protected void handle(@Nonnull CPacketATMSetPlayerAccount message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof ATMMenu menu)
			{
				Component response = menu.SetPlayerAccount(message.playerName);
				context.reply(new SPacketATMPlayerAccountResponse(response));
			}
		}
	}

}
