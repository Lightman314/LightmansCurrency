package io.github.lightman314.lightmanscurrency.network.message.data.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketMessageRemoveClientTrader extends ServerToClientPacket {

	private static final Type<SPacketMessageRemoveClientTrader> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_trader_delete_client"));
	public static final Handler<SPacketMessageRemoveClientTrader> HANDLER = new H();

	long traderID;
	
	public SPacketMessageRemoveClientTrader(long traderID) { super(TYPE); this.traderID = traderID; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketMessageRemoveClientTrader message) { buffer.writeLong(message.traderID); }
	private static SPacketMessageRemoveClientTrader decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketMessageRemoveClientTrader(buffer.readLong()); }

	private static class H extends Handler<SPacketMessageRemoveClientTrader>
	{
		protected H() { super(TYPE,easyCodec(SPacketMessageRemoveClientTrader::encode,SPacketMessageRemoveClientTrader::decode)); }
		@Override
		protected void handle(@Nonnull SPacketMessageRemoveClientTrader message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().removeTrader(message.traderID);
		}
	}

}
