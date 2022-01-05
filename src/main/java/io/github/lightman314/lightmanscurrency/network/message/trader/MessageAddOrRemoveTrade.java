package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageAddOrRemoveTrade implements IMessage<MessageAddOrRemoveTrade> {
	
	BlockPos pos;
	boolean isTradeAdd;
	
	public MessageAddOrRemoveTrade()
	{
		
	}
	
	public MessageAddOrRemoveTrade(BlockPos pos, boolean isTradeAdd)
	{
		this.pos = pos;
		this.isTradeAdd = isTradeAdd;
	}
	
	
	@Override
	public void encode(MessageAddOrRemoveTrade message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeBoolean(message.isTradeAdd);
	}

	@Override
	public MessageAddOrRemoveTrade decode(PacketBuffer buffer) {
		return new MessageAddOrRemoveTrade(buffer.readBlockPos(), buffer.readBoolean());
	}

	@Override
	public void handle(MessageAddOrRemoveTrade message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity instanceof TraderTileEntity)
				{
					TraderTileEntity trader = (TraderTileEntity)tileEntity;
					if(message.isTradeAdd)
						trader.addTrade(entity);
					else
						trader.removeTrade(entity);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
