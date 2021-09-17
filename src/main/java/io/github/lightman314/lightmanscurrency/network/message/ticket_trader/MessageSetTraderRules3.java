package io.github.lightman314.lightmanscurrency.network.message.ticket_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TicketTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.tradedata.rules.TradeRule;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetTraderRules3 implements IMessage<MessageSetTraderRules3> {

	private BlockPos pos;
	List<TradeRule> rules;
	int tradeIndex;
	
	public MessageSetTraderRules3()
	{
		
	}
	
	public MessageSetTraderRules3(BlockPos pos, List<TradeRule> rules)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = -1;
	}
	
	public MessageSetTraderRules3(BlockPos pos, List<TradeRule> rules, int tradeIndex)
	{
		this.pos = pos;
		this.rules = rules;
		this.tradeIndex = tradeIndex;
	}
	
	
	@Override
	public void encode(MessageSetTraderRules3 message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeCompoundTag(TradeRule.writeRules(new CompoundNBT(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	@Override
	public MessageSetTraderRules3 decode(PacketBuffer buffer) {
		return new MessageSetTraderRules3(buffer.readBlockPos(), TradeRule.readRules(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void handle(MessageSetTraderRules3 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity instanceof TicketTraderTileEntity)
				{
					TicketTraderTileEntity traderEntity = (TicketTraderTileEntity)tileEntity;
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
