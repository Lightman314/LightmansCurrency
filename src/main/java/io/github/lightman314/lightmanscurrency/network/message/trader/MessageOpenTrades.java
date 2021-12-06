package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageOpenTrades {
	
	BlockPos pos;
	
	public MessageOpenTrades(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public static void encode(MessageOpenTrades message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	public static MessageOpenTrades decode(FriendlyByteBuf buffer) {
		return new MessageOpenTrades(buffer.readBlockPos());
	}

	public static void handle(MessageOpenTrades message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof TraderBlockEntity)
				{
					TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
					traderEntity.openTradeMenu(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
