package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageHandlerMessage {
	
	private static final int MAX_TYPE_LENGTH = 100;
	
	BlockPos pos;
	ResourceLocation type;
	CompoundNBT updateInfo;
	
	public MessageHandlerMessage(BlockPos pos, ResourceLocation type, CompoundNBT updateInfo)
	{
		this.pos = pos;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageHandlerMessage message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUtf(message.type.toString(), MAX_TYPE_LENGTH);
		buffer.writeNbt(message.updateInfo);
	}

	public static MessageHandlerMessage decode(PacketBuffer buffer) {
		return new MessageHandlerMessage(buffer.readBlockPos(), new ResourceLocation(buffer.readUtf(MAX_TYPE_LENGTH)), buffer.readAnySizeNbt());
	}

	public static void handle(MessageHandlerMessage message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				TileEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof TraderInterfaceBlockEntity)
				{
					TraderInterfaceBlockEntity interfaceBE = (TraderInterfaceBlockEntity)blockEntity;
					interfaceBE.receiveHandlerMessage(message.type, player, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
