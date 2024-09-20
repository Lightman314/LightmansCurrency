package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.message.data.team.SPacketClearClientTeams;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SPacketClearClientBank extends ServerToClientPacket.Simple {

	public static final SPacketClearClientBank INSTANCE = new SPacketClearClientBank();
	public static final Handler<SPacketClearClientBank> HANDLER = new H();

	
	private SPacketClearClientBank() { }

	private static class H extends SimpleHandler<SPacketClearClientBank>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketClearClientBank message, @Nullable ServerPlayer sender) {
			LightmansCurrency.getProxy().clearBankAccounts();
		}
	}

}
