package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageOpenTrades implements IMessage<MessageOpenTrades> {
	
	
	BlockPos pos;
	
	
	public MessageOpenTrades()
	{
		
	}
	
	public MessageOpenTrades(BlockPos pos)
	{
		this.pos = pos;
	}
	
	
	@Override
	public void encode(MessageOpenTrades message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageOpenTrades decode(FriendlyByteBuf buffer) {
		return new MessageOpenTrades(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageOpenTrades message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				Level level = entity.level;
				if(level != null)
				{
					BlockEntity blockEntity = level.getBlockEntity(message.pos);
					if(blockEntity instanceof TraderBlockEntity)
					{
						TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
						traderEntity.openTradeMenu(entity);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
