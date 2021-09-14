package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageSyncUsers implements IMessage<MessageSyncUsers> {
	
	BlockPos pos;
	int userCount;
	
	
	public MessageSyncUsers()
	{
		
	}
	
	public MessageSyncUsers(BlockPos pos, int userCount)
	{
		this.pos = pos;
		this.userCount = userCount;
	}
	
	@Override
	public void encode(MessageSyncUsers message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.userCount);
	}

	@Override
	public MessageSyncUsers decode(FriendlyByteBuf buffer) {
		return new MessageSyncUsers(buffer.readBlockPos(), buffer.readInt());
	}

	@Override
	public void handle(MessageSyncUsers message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				Level world = minecraft.player.level;
				if(world != null)
				{
					BlockEntity blockEntity = world.getBlockEntity(message.pos);
					if(blockEntity instanceof TraderBlockEntity)
					{
						TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
						trader.setUserCount(message.userCount);
					}
				}
			}
			
		});
		supplier.get().setPacketHandled(true);
	}

}
