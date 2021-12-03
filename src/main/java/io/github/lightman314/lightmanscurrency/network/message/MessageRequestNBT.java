package io.github.lightman314.lightmanscurrency.network.message;

import java.util.function.Supplier;

//import io.github.lightman314.currencymod.core.CurrencyMod;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageRequestNBT implements IMessage<MessageRequestNBT> {

	private BlockPos pos;
	//private int dimension;
	
	public MessageRequestNBT()
	{
		
	}
	
	public MessageRequestNBT(TileEntity tileEntity)
	{
		this.pos = tileEntity.getPos();
	}
	
	public MessageRequestNBT(BlockPos pos)
	{
		this.pos = pos;
		//this.dimension = dimension;
	}
	
	
	@Override
	public void encode(MessageRequestNBT message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageRequestNBT decode(PacketBuffer buffer) {
		return new MessageRequestNBT(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageRequestNBT message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("NBT Update Request received.");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				World world = entity.getEntityWorld();
				TileEntity tileEntity = world.getTileEntity(message.pos);
				if(tileEntity != null)
				{
					TileEntityUtil.sendUpdatePacket(tileEntity);
					//CurrencyMod.LOGGER.info("NBT Update Packet sent.");
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
