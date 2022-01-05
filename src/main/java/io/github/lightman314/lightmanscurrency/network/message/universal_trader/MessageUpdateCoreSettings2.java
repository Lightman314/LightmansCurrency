package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateCoreSettings2 implements IMessage<MessageUpdateCoreSettings2> {
	
	UUID traderID;
	CompoundNBT settingsTag;
	
	public MessageUpdateCoreSettings2()
	{
		
	}
	
	public MessageUpdateCoreSettings2(UUID traderID, CompoundNBT settingsTag)
	{
		this.traderID = traderID;
		this.settingsTag = settingsTag;
	}
	
	@Override
	public void encode(MessageUpdateCoreSettings2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeCompoundTag(message.settingsTag);
	}

	@Override
	public MessageUpdateCoreSettings2 decode(PacketBuffer buffer) {
		return new MessageUpdateCoreSettings2(buffer.readUniqueId(), buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageUpdateCoreSettings2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data != null)
			{
				data.loadCoreSettings(message.settingsTag);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
