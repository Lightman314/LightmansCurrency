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

public class MessageOpenStorage implements IMessage<MessageOpenStorage> {
	
	BlockPos pos;
	
	public MessageOpenStorage()
	{
		
	}
	
	public MessageOpenStorage(BlockPos pos)
	{
		this.pos = pos;
	}
	
	
	@Override
	public void encode(MessageOpenStorage message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageOpenStorage decode(FriendlyByteBuf buffer) {
		return new MessageOpenStorage(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageOpenStorage message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				Level world = entity.level;
				if(world != null)
				{
					BlockEntity blockEntity = world.getBlockEntity(message.pos);
					if(blockEntity instanceof TraderBlockEntity)
					{
						TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
						traderEntity.openStorageMenu(entity);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
