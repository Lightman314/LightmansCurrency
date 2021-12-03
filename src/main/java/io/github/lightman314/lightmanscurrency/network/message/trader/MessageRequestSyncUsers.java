package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRequestSyncUsers implements IMessage<MessageRequestSyncUsers> {
	
	BlockPos pos;
	
	public MessageRequestSyncUsers()
	{
		
	}
	
	public MessageRequestSyncUsers(BlockPos pos)
	{
		this.pos = pos;
	}
	
	@Override
	public void encode(MessageRequestSyncUsers message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageRequestSyncUsers decode(PacketBuffer buffer) {
		return new MessageRequestSyncUsers(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageRequestSyncUsers message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				World world = entity.getEntityWorld();
				TileEntity tileEntity = world.getTileEntity(message.pos);
				if(tileEntity instanceof ItemTraderTileEntity)
				{
					ItemTraderTileEntity trader = (ItemTraderTileEntity)tileEntity;
					LightmansCurrencyPacketHandler.instance.reply(new MessageSyncUsers(message.pos, trader.getUserCount()), supplier.get());
					//LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(entity), new MessageSyncUsers(message.pos, trader.getUserCount()));
				}
			}
			
		});
		supplier.get().setPacketHandled(true);
	}

}
