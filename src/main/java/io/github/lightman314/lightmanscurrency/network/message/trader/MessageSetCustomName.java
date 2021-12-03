package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetCustomName implements IMessage<MessageSetCustomName> {
	
	BlockPos pos;
	String customName;
	
	public MessageSetCustomName()
	{
		
	}
	
	public MessageSetCustomName(BlockPos pos, String customName)
	{
		this.pos = pos;
		this.customName = customName;
	}
	
	public MessageSetCustomName(BlockPos pos, CompoundNBT customNameData)
	{
		this.pos = pos;
		this.customName = customNameData.getString("CustomName");
	}
	
	private CompoundNBT getCustomNameCompound()
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putString("CustomName", this.customName);
		return compound;
	}
	
	@Override
	public void encode(MessageSetCustomName message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeCompoundTag(message.getCustomNameCompound());
	}

	@Override
	public MessageSetCustomName decode(PacketBuffer buffer) {
		return new MessageSetCustomName(buffer.readBlockPos(), buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageSetCustomName message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				World world = entity.world;
				if(world != null)
				{
					TileEntity tileEntity = world.getTileEntity(message.pos);
					if(tileEntity instanceof TraderTileEntity)
					{
						TraderTileEntity traderEntity = (TraderTileEntity)tileEntity;
						traderEntity.setCustomName(message.customName);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
