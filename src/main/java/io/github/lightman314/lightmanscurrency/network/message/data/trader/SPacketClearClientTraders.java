package io.github.lightman314.lightmanscurrency.network.message.data.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketClearClientTraders extends ServerToClientPacket {

	private static final Type<SPacketClearClientTraders> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_traders_clear_client"));
	public static final SPacketClearClientTraders INSTANCE = new SPacketClearClientTraders();
	public static final Handler<SPacketClearClientTraders> HANDLER = new H();

	private SPacketClearClientTraders() { super(TYPE); }

	private static class H extends SimpleHandler<SPacketClearClientTraders>
	{
		protected H() { super(TYPE,INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketClearClientTraders message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().clearClientTraders();
		}
	}
	
}
