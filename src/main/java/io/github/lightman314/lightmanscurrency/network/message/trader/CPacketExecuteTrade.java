package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketExecuteTrade extends ClientToServerPacket {

	private static final Type<CPacketExecuteTrade> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_trader_execute_trade"));
	public static final Handler<CPacketExecuteTrade> HANDLER = new H();

	private final int trader;
	private final int tradeIndex;
	
	public CPacketExecuteTrade(int trader, int tradeIndex)
	{
		super(TYPE);
		this.trader = trader;
		this.tradeIndex = tradeIndex;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketExecuteTrade message) {
		buffer.writeInt(message.trader);
		buffer.writeInt(message.tradeIndex);
	}

	private static CPacketExecuteTrade decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketExecuteTrade(buffer.readInt(), buffer.readInt()); }

	private static class H extends Handler<CPacketExecuteTrade>
	{
		protected H() { super(TYPE, easyCodec(CPacketExecuteTrade::encode,CPacketExecuteTrade::decode)); }
		@Override
		protected void handle(@Nonnull CPacketExecuteTrade message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.containerMenu instanceof TraderMenu menu)
				menu.ExecuteTrade(message.trader, message.tradeIndex);
		}
	}

}
