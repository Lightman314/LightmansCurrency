package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetCustomName2 implements IMessage<MessageSetCustomName2> {
	
	UUID traderID;
	String customName;
	
	public MessageSetCustomName2()
	{
		
	}
	
	public MessageSetCustomName2(UUID traderID, String customName)
	{
		this.traderID = traderID;
		this.customName = customName;
	}
	
	public MessageSetCustomName2(UUID traderID, CompoundNBT customNameData)
	{
		this.traderID = traderID;
		this.customName = customNameData.getString("CustomName");
	}
	
	private CompoundNBT getCustomNameCompound()
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putString("CustomName", this.customName);
		return compound;
	}
	
	@Override
	public void encode(MessageSetCustomName2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeCompoundTag(message.getCustomNameCompound());
	}

	@Override
	public MessageSetCustomName2 decode(PacketBuffer buffer) {
		return new MessageSetCustomName2(buffer.readUniqueId(), buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageSetCustomName2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data = TradingOffice.getData(message.traderID);
			if(data != null)
			{
				data.setName(message.customName);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
