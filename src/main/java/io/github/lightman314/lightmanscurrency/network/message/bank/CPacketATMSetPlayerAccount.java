package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketATMSetPlayerAccount extends ClientToServerPacket {

	public static final Handler<CPacketATMSetPlayerAccount> HANDLER = new H();

	private final String playerName;
	
	public CPacketATMSetPlayerAccount(String playerName) { this.playerName = playerName; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeUtf(this.playerName); }

	private static class H extends Handler<CPacketATMSetPlayerAccount>
	{
		
		@Override
		public CPacketATMSetPlayerAccount decode(FriendlyByteBuf buffer) { return new CPacketATMSetPlayerAccount(buffer.readUtf()); }
		@Override
		protected void handle(CPacketATMSetPlayerAccount message, Player player) {
            if(player.containerMenu instanceof ATMMenu menu)
            {
                MutableComponent response = menu.SetPlayerAccount(message.playerName);
                new SPacketATMPlayerAccountResponse(response).sendTo(player);
            }
		}
	}

}
