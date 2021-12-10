package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageSetCustomName {
	
	BlockPos pos;
	String customName;
	
	public MessageSetCustomName(BlockPos pos, String customName)
	{
		this.pos = pos;
		this.customName = customName;
	}
	
	public static void encode(MessageSetCustomName message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUtf(message.customName);
	}

	public static MessageSetCustomName decode(FriendlyByteBuf buffer) {
		return new MessageSetCustomName(buffer.readBlockPos(), buffer.readUtf());
	}

	public static void handle(MessageSetCustomName message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof TraderBlockEntity)
				{
					TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
					traderEntity.setCustomName(message.customName);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
