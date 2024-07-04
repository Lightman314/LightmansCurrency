package io.github.lightman314.lightmanscurrency.network.message.bank;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.SelectionTab;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketATMPlayerAccountResponse extends ServerToClientPacket {

	public static final Type<SPacketATMPlayerAccountResponse> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_atm_select_account_player_reply"));
	public static final Handler<SPacketATMPlayerAccountResponse> HANDLER = new H();

	final Component message;
	
	public SPacketATMPlayerAccountResponse(Component message) { super(TYPE); this.message = message; }
	
	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketATMPlayerAccountResponse message) {
		ComponentSerialization.STREAM_CODEC.encode(buffer, message.message);
	}

	private static SPacketATMPlayerAccountResponse decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketATMPlayerAccountResponse(ComponentSerialization.STREAM_CODEC.decode(buffer)); }

	private static class H extends Handler<SPacketATMPlayerAccountResponse>
	{
		protected H() { super(TYPE, fancyCodec(SPacketATMPlayerAccountResponse::encode,SPacketATMPlayerAccountResponse::decode)); }
		@Override
		protected void handle(@Nonnull SPacketATMPlayerAccountResponse message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen instanceof ATMScreen screen)
			{
				if(screen.currentTab() instanceof SelectionTab tab)
					tab.ReceiveSelectPlayerResponse(message.message);
			}
		}
	}

}
