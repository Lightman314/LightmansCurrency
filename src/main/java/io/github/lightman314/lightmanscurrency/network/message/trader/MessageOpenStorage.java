package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public void encode(MessageOpenStorage message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageOpenStorage decode(PacketBuffer buffer) {
		return new MessageOpenStorage(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageOpenStorage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				World world = entity.getEntityWorld();
				if(world != null)
				{
					TileEntity tileEntity = world.getTileEntity(message.pos);
					if(tileEntity instanceof TraderTileEntity)
					{
						TraderTileEntity traderEntity = (TraderTileEntity)tileEntity;
						traderEntity.openStorageMenu(entity);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
