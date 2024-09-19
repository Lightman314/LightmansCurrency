package io.github.lightman314.lightmanscurrency.network.message.data.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketUpdateClientTrader extends ServerToClientPacket {

	private static final Type<SPacketUpdateClientTrader> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_trader_update_client"));
	public static final Handler<SPacketUpdateClientTrader> HANDLER = new H();

	CompoundTag traderData;
	
	public SPacketUpdateClientTrader(CompoundTag traderData) { super(TYPE); this.traderData = traderData; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketUpdateClientTrader message) { buffer.writeNbt(message.traderData); }
	private static SPacketUpdateClientTrader decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdateClientTrader(readNBT(buffer)); }

	private static class H extends Handler<SPacketUpdateClientTrader>
	{
		protected H() { super(TYPE, easyCodec(SPacketUpdateClientTrader::encode,SPacketUpdateClientTrader::decode)); }
		@Override
		protected void handle(@Nonnull SPacketUpdateClientTrader message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().updateTrader(message.traderData);
		}
	}

}
