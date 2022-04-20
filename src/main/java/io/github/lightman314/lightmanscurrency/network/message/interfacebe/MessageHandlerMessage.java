package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageHandlerMessage {
	
	private static final int MAX_TYPE_LENGTH = 100;
	
	BlockPos pos;
	ResourceLocation type;
	CompoundTag updateInfo;
	
	public MessageHandlerMessage(BlockPos pos, ResourceLocation type, CompoundTag updateInfo)
	{
		this.pos = pos;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageHandlerMessage message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUtf(message.type.toString(), MAX_TYPE_LENGTH);
		buffer.writeNbt(message.updateInfo);
	}

	public static MessageHandlerMessage decode(FriendlyByteBuf buffer) {
		return new MessageHandlerMessage(buffer.readBlockPos(), new ResourceLocation(buffer.readUtf(MAX_TYPE_LENGTH)), buffer.readAnySizeNbt());
	}

	public static void handle(MessageHandlerMessage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
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
