package io.github.lightman314.lightmanscurrency.network.message.logger;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageClearLogger implements IMessage<MessageClearLogger> {

	private BlockPos pos;
	
	public MessageClearLogger()
	{
		
	}
	
	public MessageClearLogger(BlockPos pos)
	{
		this.pos = pos;
	}
	
	
	@Override
	public void encode(MessageClearLogger message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageClearLogger decode(PacketBuffer buffer) {
		return new MessageClearLogger(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageClearLogger message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity != null)
				{
					if(tileEntity instanceof ILoggerSupport<?>)
					{
						ILoggerSupport<?> loggerEntity = (ILoggerSupport<?>)tileEntity;
						loggerEntity.clearLogger();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
