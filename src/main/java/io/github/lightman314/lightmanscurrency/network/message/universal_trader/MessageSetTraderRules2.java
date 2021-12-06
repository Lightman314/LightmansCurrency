package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetTraderRules2 {

	private UUID traderID;
	List<TradeRule> rules;
	int tradeIndex;
	
	public MessageSetTraderRules2(UUID traderID, List<TradeRule> rules)
	{
		this.traderID = traderID;
		this.rules = rules;
		this.tradeIndex = -1;
	}
	
	public MessageSetTraderRules2(UUID traderID, List<TradeRule> rules, int tradeIndex)
	{
		this.traderID = traderID;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	public static void encode(MessageSetTraderRules2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeNbt(TradeRule.writeRules(new CompoundTag(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	public static MessageSetTraderRules2 decode(FriendlyByteBuf buffer) {
		return new MessageSetTraderRules2(buffer.readUUID(), TradeRule.readRules(buffer.readNbt()), buffer.readInt());
	}

	public static void handle(MessageSetTraderRules2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data1 = TradingOffice.getData(message.traderID);
			if(data1 instanceof UniversalItemTraderData)
			{
				UniversalItemTraderData data2 = (UniversalItemTraderData)data1;
				
				if(message.tradeIndex >= 0)
				{
					ITradeRuleHandler trade = data2.getTrade(message.tradeIndex);
					if(trade != null)
						trade.setRules(message.rules);
				}
				else
					data2.setRules(message.rules);
				//Mark the trader as dirty
				TradingOffice.MarkDirty(message.traderID);
				
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
