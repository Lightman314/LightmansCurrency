package io.github.lightman314.lightmanscurrency.network.message.enchantments;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketMoneyMendingClink extends ServerToClientPacket.Simple {

	public static final Handler<SPacketMoneyMendingClink> HANDLER = new H();
	public static final SPacketMoneyMendingClink INSTANCE = new SPacketMoneyMendingClink();

	private SPacketMoneyMendingClink() {}

	private static class H extends SimpleHandler<SPacketMoneyMendingClink>
	{
		protected H() { super(INSTANCE); }
		@Override
		protected void handle(SPacketMoneyMendingClink message, Player player) {
			LightmansCurrency.getProxy().playCoinSound();
		}
	}
	
}
