package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageSetTraderRules {

	private BlockPos pos;
	List<TradeRule> rules;
	int tradeIndex;
	
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
	
	public static void encode(MessageSetTraderRules message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeNbt(TradeRule.writeRules(new CompoundTag(), message.rules));
		buffer.writeInt(message.tradeIndex);
	}

	public static MessageSetTraderRules decode(FriendlyByteBuf buffer) {
		return new MessageSetTraderRules(buffer.readBlockPos(), TradeRule.readRules(buffer.readNbt()), buffer.readInt());
	}

	public static void handle(MessageSetTraderRules message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof ItemTraderBlockEntity)
				{
					ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)blockEntity;
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
