package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetTraderRules implements IMessage<MessageSetTraderRules> {

	private BlockPos pos;
	List<TradeRule> rules;
	int tradeIndex;
	
	public MessageSetTraderRules()
	{
		
	}
	
	public MessageSetTraderRules(BlockPos pos, List<TradeRule> rules)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = -1;
	}
	
	public MessageSetTraderRules(BlockPos pos, List<TradeRule> rules, int tradeIndex)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	
	@Override
	public void encode(MessageSetTraderRules message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeCompoundTag(TradeRule.writeRules(new CompoundNBT(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageSetTraderRules decode(PacketBuffer buffer) {
		return new MessageSetTraderRules(buffer.readBlockPos(), TradeRule.readRules(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void handle(MessageSetTraderRules message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity instanceof ItemTraderTileEntity)
				{
					ItemTraderTileEntity traderEntity = (ItemTraderTileEntity)tileEntity;
					if(message.tradeIndex >= 0)
					{
						ITradeRuleHandler trade = traderEntity.getTrade(message.tradeIndex);
						if(trade != null)
							trade.setRules(message.rules);
						traderEntity.markTradesDirty();
					}
					else
					{
						traderEntity.setRules(message.rules);
						traderEntity.markRulesDirty();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
