package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.tradedata.rules.TradeRule;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetTraderRules2 implements IMessage<MessageSetTraderRules2> {

	private UUID traderID;
	List<TradeRule> rules;
	int tradeIndex;
	
	public MessageSetTraderRules2()
	{
		
	}
	
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
	
	
	@Override
	public void encode(MessageSetTraderRules2 message, PacketBuffer buffer) {
		buffer.writeUniqueId(message.traderID);
		buffer.writeCompoundTag(TradeRule.writeRules(new CompoundNBT(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageSetTraderRules2 decode(PacketBuffer buffer) {
		return new MessageSetTraderRules2(buffer.readUniqueId(), TradeRule.readRules(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void handle(MessageSetTraderRules2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			UniversalTraderData data1 = TradingOffice.getData(message.traderID);
			if(data1 != null && data1 instanceof UniversalItemTraderData)
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
