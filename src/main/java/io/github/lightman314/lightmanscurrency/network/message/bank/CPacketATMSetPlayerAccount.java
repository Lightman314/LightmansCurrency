package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketATMSetPlayerAccount extends ClientToServerPacket {

	public static final Handler<CPacketATMSetPlayerAccount> HANDLER = new H();

	private final String playerName;
	
	public CPacketATMSetPlayerAccount(String playerName) { this.playerName = playerName; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeUtf(this.playerName); }

	private static class H extends Handler<CPacketATMSetPlayerAccount>
	{
		@Nonnull
		@Override
		public CPacketATMSetPlayerAccount decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketATMSetPlayerAccount(buffer.readUtf()); }
		@Override
		protected void handle(@Nonnull CPacketATMSetPlayerAccount message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				if(sender.containerMenu instanceof ATMMenu menu)
				{
					MutableComponent response = menu.SetPlayerAccount(message.playerName);
				 	new SPacketATMPlayerAccountResponse(response).sendTo(sender);
				}
			}
		}
	}

}
