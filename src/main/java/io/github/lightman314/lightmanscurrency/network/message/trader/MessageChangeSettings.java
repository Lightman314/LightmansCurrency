package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageChangeSettings implements IMessage<MessageChangeSettings> {
	
	private final int MAX_TYPE_LENGTH = 100;
	
	BlockPos pos;
	ResourceLocation type;
	CompoundNBT updateInfo;
	
	public MessageChangeSettings()
	{
		
	}
	
	public MessageChangeSettings(BlockPos pos, ResourceLocation type, CompoundNBT updateInfo)
	{
		this.pos = pos;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	@Override
	public void encode(MessageChangeSettings message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeString(message.type.toString(), MAX_TYPE_LENGTH);
		buffer.writeCompoundTag(message.updateInfo);
	}

	@Override
	public MessageChangeSettings decode(PacketBuffer buffer) {
		return new MessageChangeSettings(buffer.readBlockPos(), new ResourceLocation(buffer.readString(MAX_TYPE_LENGTH)), buffer.readCompoundTag());
	}

	@Override
	public void handle(MessageChangeSettings message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			PlayerEntity requestor = supplier.get().getSender();
			if(requestor != null)
			{
				TileEntity tileEntity = requestor.world.getTileEntity(message.pos);
				if(tileEntity instanceof TraderTileEntity)
				{
					TraderTileEntity trader = (TraderTileEntity)tileEntity;
					trader.changeSettings(message.type, requestor, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
