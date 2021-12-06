package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.events.TradeEditEvent.TradePriceEditEvent;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetItemPrice2 {

	private UUID traderID;
	private int tradeIndex;
	private CoinValue newPrice;
	private boolean isFree;
	private String customName;
	private String newDirection;
	
	public MessageSetItemPrice2(UUID traderID, int tradeIndex, CoinValue newPrice, boolean isFree, String customName, String newDirection)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		this.isFree = isFree;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	
	public static void encode(MessageSetItemPrice2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.newPrice.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeBoolean(message.isFree);
		buffer.writeUtf(message.customName);
		buffer.writeUtf(message.newDirection);
	}

	public static MessageSetItemPrice2 decode(FriendlyByteBuf buffer) {
		return new MessageSetItemPrice2(buffer.readUUID(), buffer.readInt(), new CoinValue(buffer.readNbt()), buffer.readBoolean(), buffer.readUtf(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readUtf(ItemTradeData.MaxTradeTypeStringLength()));
	}

	public static void handle(MessageSetItemPrice2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			UniversalTraderData data1 = TradingOffice.getData(message.traderID);
			if(data1 != null && data1 instanceof UniversalItemTraderData)
			{
				UniversalItemTraderData data2 = (UniversalItemTraderData)data1;
				CoinValue oldPrice = data2.getTrade(message.tradeIndex).getCost();
				boolean wasFree = data2.getTrade(message.tradeIndex).isFree();
				data2.getTrade(message.tradeIndex).setCost(message.newPrice);
				data2.getTrade(message.tradeIndex).setFree(message.isFree);
				data2.getTrade(message.tradeIndex).setCustomName(message.customName);
				data2.getTrade(message.tradeIndex).setTradeType(ItemTradeData.loadTradeType(message.newDirection));
				
				if(oldPrice.getRawValue() != message.newPrice.getRawValue() || wasFree != message.isFree)
				{
					//Throw price change event
					TradePriceEditEvent e = new TradePriceEditEvent(() -> {
						//Create safe supplier, just in case the event saves it for later
						UniversalTraderData d = TradingOffice.getData(message.traderID);
						if(d instanceof UniversalItemTraderData)
							return (UniversalItemTraderData)d;
						return null;
					}, message.tradeIndex, oldPrice, wasFree);
					MinecraftForge.EVENT_BUS.post(e);
				}
				
				//Mark the trader as dirty
				TradingOffice.MarkDirty(message.traderID);
				
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
