package io.github.lightman314.lightmanscurrency.network.message;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageRequestNBT {

	private final BlockPos pos;
	
	public MessageRequestNBT(TileEntity tileEntity)
	{
		this.pos = tileEntity.getBlockPos();
	}
	
	public MessageRequestNBT(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public static void encode(MessageRequestNBT message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}

	public static MessageRequestNBT decode(PacketBuffer buffer) {
		return new MessageRequestNBT(buffer.readBlockPos());
	}

	public static void handle(MessageRequestNBT message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("NBT Update Request received.");
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				TileEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					BlockEntityUtil.sendUpdatePacket(blockEntity);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
