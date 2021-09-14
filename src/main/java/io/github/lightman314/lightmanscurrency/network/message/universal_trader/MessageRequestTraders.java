package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageRequestTraders implements IMessage<MessageRequestTraders> {
	
	
	public MessageRequestTraders()
	{
		
	}
	
	
	@Override
	public void encode(MessageRequestTraders message, FriendlyByteBuf buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageRequestTraders decode(FriendlyByteBuf buffer) {
		return new MessageRequestTraders();
	}

	@Override
	public void handle(MessageRequestTraders message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				List<UniversalTraderData> traders = TradingOffice.getTraders(entity);
				CompoundTag compound = new CompoundTag();
				ListTag traderList = new ListTag();
				traders.forEach(trader -> traderList.add(trader.write(new CompoundTag())));
				compound.put("Traders", traderList);
				LightmansCurrencyPacketHandler.instance.reply(new MessageUpdateTraders(compound), supplier.get());
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
