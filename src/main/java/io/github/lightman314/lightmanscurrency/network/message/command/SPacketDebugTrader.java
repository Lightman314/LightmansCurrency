package io.github.lightman314.lightmanscurrency.network.message.command;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketDebugTrader extends ServerToClientPacket {

	private static final Type<SPacketDebugTrader> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_debug_trader_data"));
	public static final Handler<SPacketDebugTrader> HANDLER = new H();

	final long traderID;
	
	public SPacketDebugTrader(long traderID) { super(TYPE); this.traderID = traderID; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketDebugTrader message) { buffer.writeLong(message.traderID); }
	private static SPacketDebugTrader decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketDebugTrader(buffer.readLong()); }

	private static class H extends Handler<SPacketDebugTrader>
	{
		protected H() { super(TYPE, easyCodec(SPacketDebugTrader::encode,SPacketDebugTrader::decode)); }
		@Override
		protected void handle(@Nonnull SPacketDebugTrader message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			TraderData trader = TraderAPI.API.GetTrader(true, message.traderID);
			if(trader == null)
				LightmansCurrency.LogInfo("Client is missing trader with id " + message.traderID + "!");
			else
				LightmansCurrency.LogInfo("Client Trader NBT for trader " + message.traderID + ":\n" + trader.save(player.registryAccess()));
		}
	}
	
}
