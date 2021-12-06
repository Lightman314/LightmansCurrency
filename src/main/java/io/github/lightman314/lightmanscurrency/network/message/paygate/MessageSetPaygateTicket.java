package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetPaygateTicket {

	private BlockPos pos;
	private UUID ticketID;
	
	public MessageSetPaygateTicket(BlockPos pos, UUID ticketID)
	{
		this.pos = pos;
		this.ticketID = ticketID;
	}
	
	public static void encode(MessageSetPaygateTicket message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUUID(message.ticketID);
	}

	public static MessageSetPaygateTicket decode(FriendlyByteBuf buffer) {
		return new MessageSetPaygateTicket(buffer.readBlockPos(), buffer.readUUID());
	}

	public static void handle(MessageSetPaygateTicket message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					if(blockEntity instanceof PaygateTileEntity)
					{
						
						PaygateTileEntity paygateEntity = (PaygateTileEntity)blockEntity;
						
						paygateEntity.SetTicketID(message.ticketID);
						
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
