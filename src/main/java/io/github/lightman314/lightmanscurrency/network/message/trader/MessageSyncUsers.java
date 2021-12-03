package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public void encode(MessageSyncUsers message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.userCount);
	}

	@Override
	public MessageSyncUsers decode(PacketBuffer buffer) {
		return new MessageSyncUsers(buffer.readBlockPos(), buffer.readInt());
	}

	@Override
	public void handle(MessageSyncUsers message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				World world = minecraft.player.world;
				if(world != null)
				{
					TileEntity tileEntity = world.getTileEntity(message.pos);
					if(tileEntity instanceof TraderTileEntity)
					{
						TraderTileEntity trader = (TraderTileEntity)tileEntity;
						trader.setUserCount(message.userCount);
					}
				}
			}
			
		});
		supplier.get().setPacketHandled(true);
	}

}
