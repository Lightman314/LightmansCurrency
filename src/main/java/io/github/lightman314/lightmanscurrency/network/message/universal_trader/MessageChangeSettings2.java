package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageChangeSettings2 implements IMessage<MessageChangeSettings2> {
	
	private final int MAX_TYPE_LENGTH = 100;
	
	UUID traderID;
	ResourceLocation type;
	CompoundNBT updateInfo;
	
	public MessageChangeSettings2()
	{
		
	}
	
	public MessageChangeSettings2(UUID traderID, ResourceLocation type, CompoundNBT updateInfo)
	{
		this.traderID = traderID;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	@Override
	public void encode(MessageChangeSettings2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeString(message.type.toString(), MAX_TYPE_LENGTH);
		buffer.writeCompoundTag(message.updateInfo);
	}

	@Override
	public MessageChangeSettings2 decode(PacketBuffer buffer) {
		return new MessageChangeSettings2(buffer.readUniqueId(), new ResourceLocation(buffer.readString(MAX_TYPE_LENGTH)), buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageChangeSettings2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			PlayerEntity requestor = supplier.get().getSender();
			if(requestor != null)
			{
				UniversalTraderData trader = TradingOffice.getData(message.traderID);
				if(trader != null)
				{
					trader.changeSettings(message.type, requestor, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
