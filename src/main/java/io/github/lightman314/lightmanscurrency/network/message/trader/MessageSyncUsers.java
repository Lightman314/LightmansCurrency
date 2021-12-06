package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSyncUsers {
	
	BlockPos pos;
	int userCount;
	
	public MessageSyncUsers(BlockPos pos, int userCount)
	{
		this.pos = pos;
		this.userCount = userCount;
	}
	
	public static void encode(MessageSyncUsers message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.userCount);
	}

	public static MessageSyncUsers decode(FriendlyByteBuf buffer) {
		return new MessageSyncUsers(buffer.readBlockPos(), buffer.readInt());
	}

	public static void handle(MessageSyncUsers message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				BlockEntity blockEntity = minecraft.player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof TraderBlockEntity)
				{
					TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
					trader.setUserCount(message.userCount);
				}
			}
			
		});
		supplier.get().setPacketHandled(true);
	}

}
