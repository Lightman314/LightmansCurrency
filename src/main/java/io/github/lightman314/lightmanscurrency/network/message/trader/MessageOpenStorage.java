package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageOpenStorage {
	
	BlockPos pos;
	
	public MessageOpenStorage(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public static void encode(MessageOpenStorage message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	public static MessageOpenStorage decode(FriendlyByteBuf buffer) {
		return new MessageOpenStorage(buffer.readBlockPos());
	}

	public static void handle(MessageOpenStorage message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof TraderBlockEntity)
				{
					TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
					traderEntity.openStorageMenu(player);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
