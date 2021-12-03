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
	public void encode(MessageOpenTrades message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageOpenTrades decode(PacketBuffer buffer) {
		return new MessageOpenTrades(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageOpenTrades message, Supplier<Context> supplier) {
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
						traderEntity.openTradeMenu(entity);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
