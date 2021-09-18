package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.IPermissions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageAddOrRemoveAlly implements IMessage<MessageAddOrRemoveAlly> {
	
	BlockPos pos;
	boolean isAllyAdd;
	String ally;
	
	public MessageAddOrRemoveAlly()
	{
		
	}
	
	public MessageAddOrRemoveAlly(BlockPos pos, boolean isAllyAdd, String ally)
	{
		this.pos = pos;
		this.isAllyAdd = isAllyAdd;
		this.ally = ally;
	}
	
	
	@Override
	public void encode(MessageAddOrRemoveAlly message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeBoolean(message.isAllyAdd);
		buffer.writeString(message.ally, 32);
	}

	@Override
	public MessageAddOrRemoveAlly decode(PacketBuffer buffer) {
		return new MessageAddOrRemoveAlly(buffer.readBlockPos(), buffer.readBoolean(), buffer.readString(32));
	}

	@Override
	public void handle(MessageAddOrRemoveAlly message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity instanceof IPermissions)
				{
					IPermissions permissionEntity = (IPermissions)tileEntity;
					if(message.isAllyAdd)
					{
						if(!permissionEntity.getAllies().contains(message.ally))
						{
							permissionEntity.getAllies().add(message.ally);
							permissionEntity.markAlliesDirty();
						}
					}
					else
					{
						if(permissionEntity.getAllies().contains(message.ally))
						{
							permissionEntity.getAllies().remove(message.ally);
							permissionEntity.markAlliesDirty();
						}
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
