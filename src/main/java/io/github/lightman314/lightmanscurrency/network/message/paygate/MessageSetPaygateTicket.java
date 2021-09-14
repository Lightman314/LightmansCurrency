package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
//import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	public void encode(MessageSetPaygateTicket message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUUID(message.ticketID);
	}

	@Override
	public MessageSetPaygateTicket decode(FriendlyByteBuf buffer) {
		return new MessageSetPaygateTicket(buffer.readBlockPos(), buffer.readUUID());
	}

	@Override
	public void handle(MessageSetPaygateTicket message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				BlockEntity blockEntity = entity.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					if(blockEntity instanceof PaygateBlockEntity)
					{
						PaygateBlockEntity paygateEntity = (PaygateBlockEntity)blockEntity;
						paygateEntity.SetTicketID(message.ticketID);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
