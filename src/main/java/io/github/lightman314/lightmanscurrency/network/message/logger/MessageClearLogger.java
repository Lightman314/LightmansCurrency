package io.github.lightman314.lightmanscurrency.network.message.logger;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageClearLogger {

	private BlockPos pos;
	
	public MessageClearLogger(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public static void encode(MessageClearLogger message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	public static MessageClearLogger decode(FriendlyByteBuf buffer) {
		return new MessageClearLogger(buffer.readBlockPos());
	}

	public static void handle(MessageClearLogger message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof ILoggerSupport<?>)
				{
					ILoggerSupport<?> loggerEntity = (ILoggerSupport<?>)blockEntity;
					loggerEntity.clearLogger();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
