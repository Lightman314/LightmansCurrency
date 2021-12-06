package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.tileentity.IPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageAddOrRemoveAlly {
	
	BlockPos pos;
	boolean isAllyAdd;
	String ally;
	
	public MessageAddOrRemoveAlly(BlockPos pos, boolean isAllyAdd, String ally)
	{
		this.pos = pos;
		this.isAllyAdd = isAllyAdd;
		this.ally = ally;
	}
	
	public static void encode(MessageAddOrRemoveAlly message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeBoolean(message.isAllyAdd);
		buffer.writeUtf(message.ally);
	}

	public static MessageAddOrRemoveAlly decode(FriendlyByteBuf buffer) {
		return new MessageAddOrRemoveAlly(buffer.readBlockPos(), buffer.readBoolean(), buffer.readUtf());
	}

	public static void handle(MessageAddOrRemoveAlly message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof IPermissions)
				{
					IPermissions permissionEntity = (IPermissions)blockEntity;
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
