package io.github.lightman314.lightmanscurrency.network.message.enchantments;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketMoneyMendingClink extends ServerToClientPacket.Simple {

	public static final Handler<SPacketMoneyMendingClink> HANDLER = new H();
	public static final SPacketMoneyMendingClink INSTANCE = new SPacketMoneyMendingClink();

	private SPacketMoneyMendingClink() {}

	private static class H extends SimpleHandler<SPacketMoneyMendingClink>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketMoneyMendingClink message, @Nullable ServerPlayer sender) {
			LightmansCurrency.getProxy().playCoinSound();
		}
	}
	
}
