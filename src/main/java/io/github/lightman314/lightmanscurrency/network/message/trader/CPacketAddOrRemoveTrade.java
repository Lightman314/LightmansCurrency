package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketAddOrRemoveTrade extends ClientToServerPacket {

	private static final Type<CPacketAddOrRemoveTrade> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_trader_add_remove_trade"));
	public static final Handler<CPacketAddOrRemoveTrade> HANDLER = new H();

	long traderID;
	boolean isTradeAdd;
	
	public CPacketAddOrRemoveTrade(long traderID, boolean isTradeAdd)
	{
		super(TYPE);
		this.traderID = traderID;
		this.isTradeAdd = isTradeAdd;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketAddOrRemoveTrade message) {
		buffer.writeLong(message.traderID);
		buffer.writeBoolean(message.isTradeAdd);
	}
	private static CPacketAddOrRemoveTrade decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketAddOrRemoveTrade(buffer.readLong(),buffer.readBoolean()); }

	private static class H extends Handler<CPacketAddOrRemoveTrade>
	{
		protected H() { super(TYPE, easyCodec(CPacketAddOrRemoveTrade::encode,CPacketAddOrRemoveTrade::decode)); }
		@Override
		protected void handle(@Nonnull CPacketAddOrRemoveTrade message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			TraderData trader = TraderSaveData.GetTrader(false, message.traderID);
			if(trader != null)
			{
				if(message.isTradeAdd)
					trader.addTrade(player);
				else
					trader.removeTrade(player);
			}
		}
	}

}
