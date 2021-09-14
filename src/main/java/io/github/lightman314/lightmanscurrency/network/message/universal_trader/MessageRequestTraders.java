package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRequestTraders implements IMessage<MessageRequestTraders> {
	
	
	public MessageRequestTraders()
	{
		
	}
	
	
	@Override
	public void encode(MessageRequestTraders message, PacketBuffer buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageRequestTraders decode(PacketBuffer buffer) {
		return new MessageRequestTraders();
	}

	@Override
	public void handle(MessageRequestTraders message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				List<UniversalTraderData> traders = TradingOffice.getTraders(entity);
				CompoundNBT compound = new CompoundNBT();
				ListNBT traderList = new ListNBT();
				traders.forEach(trader -> traderList.add(trader.write(new CompoundNBT())));
				compound.put("Traders", traderList);
				LightmansCurrencyPacketHandler.instance.reply(new MessageUpdateTraders(compound), supplier.get());
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
