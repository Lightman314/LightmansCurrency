package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetPaygateTicket implements IMessage<MessageSetPaygateTicket> {

	private BlockPos pos;
	private UUID ticketID;
	
	public MessageSetPaygateTicket()
	{
		
	}
	
	public MessageSetPaygateTicket(BlockPos pos, UUID ticketID)
	{
		this.pos = pos;
		this.ticketID = ticketID;
	}
	
	
	@Override
	public void encode(MessageSetPaygateTicket message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUniqueId(message.ticketID);
	}

	@Override
	public MessageSetPaygateTicket decode(PacketBuffer buffer) {
		return new MessageSetPaygateTicket(buffer.readBlockPos(), buffer.readUniqueId());
	}

	@Override
	public void handle(MessageSetPaygateTicket message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity != null)
				{
					if(tileEntity instanceof PaygateTileEntity)
					{
						
						PaygateTileEntity paygateEntity = (PaygateTileEntity)tileEntity;
						
						paygateEntity.SetTicketID(message.ticketID);
						
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
