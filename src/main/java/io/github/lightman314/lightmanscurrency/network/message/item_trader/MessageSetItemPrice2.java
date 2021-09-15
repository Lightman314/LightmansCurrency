package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetItemPrice2 implements IMessage<MessageSetItemPrice2> {

	private UUID traderID;
	private int tradeIndex;
	private CoinValue newPrice;
	private boolean isFree;
	private String customName;
	private String newDirection;
	
	public MessageSetItemPrice2()
	{
		
	}
	
	public MessageSetItemPrice2(UUID traderID, int tradeIndex, CoinValue newPrice, boolean isFree, String customName, String newDirection)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		this.isFree = isFree;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	
	@Override
	public void encode(MessageSetItemPrice2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.newPrice.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		buffer.writeBoolean(message.isFree);
		buffer.writeString(message.customName);
		buffer.writeString(message.newDirection);
	}

	@Override
	public MessageSetItemPrice2 decode(PacketBuffer buffer) {
		return new MessageSetItemPrice2(buffer.readUniqueId(), buffer.readInt(), new CoinValue(buffer.readCompoundTag()), buffer.readBoolean(), buffer.readString(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readString(ItemTradeData.MaxTradeDirectionStringLength()));
	}

	@Override
	public void handle(MessageSetItemPrice2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			UniversalTraderData data1 = TradingOffice.getData(message.traderID);
			if(data1 != null && data1 instanceof UniversalItemTraderData)
			{
				UniversalItemTraderData data2 = (UniversalItemTraderData)data1;
				data2.getTrade(message.tradeIndex).setCost(message.newPrice);
				data2.getTrade(message.tradeIndex).setFree(message.isFree);
				data2.getTrade(message.tradeIndex).setCustomName(message.customName);
				data2.getTrade(message.tradeIndex).setTradeDirection(ItemTradeData.loadTradeDirection(message.newDirection));
				
				//Mark the trader as dirty
				TradingOffice.MarkDirty(message.traderID);
				
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
