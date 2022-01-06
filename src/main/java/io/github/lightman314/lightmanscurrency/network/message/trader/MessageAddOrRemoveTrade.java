package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade {
	
	BlockPos pos;
	boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade(BlockPos pos, boolean isTradeAdd)
	{
		this.pos = pos;
		this.isTradeAdd = isTradeAdd;
	}
	
	public static void encode(MessageAddOrRemoveTrade message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeBoolean(message.isTradeAdd);
	}

	public static MessageAddOrRemoveTrade decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveTrade(buffer.readBlockPos(), buffer.readBoolean());
	}

	public static void handle(MessageAddOrRemoveTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof TraderBlockEntity)
				{
					TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
					if(message.isTradeAdd)
						trader.addTrade(player);
					else
						trader.removeTrade(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
