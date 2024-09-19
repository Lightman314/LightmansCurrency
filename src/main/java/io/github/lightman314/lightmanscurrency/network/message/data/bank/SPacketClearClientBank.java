package io.github.lightman314.lightmanscurrency.network.message.data.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketClearClientBank extends ServerToClientPacket {

	private static final Type<SPacketClearClientBank> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_data_clear_bank"));
	public static final SPacketClearClientBank INSTANCE = new SPacketClearClientBank();
	public static final Handler<SPacketClearClientBank> HANDLER = new H();
	
	private SPacketClearClientBank() { super(TYPE); }

	private static class H extends SimpleHandler<SPacketClearClientBank>
	{
		protected H() { super(TYPE, INSTANCE); }
		@Override
		protected void handle(@Nonnull SPacketClearClientBank message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().clearBankAccounts();
		}
	}

}
