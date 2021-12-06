package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageRequestSyncUsers {
	
	BlockPos pos;
	
	public MessageRequestSyncUsers(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public static void encode(MessageRequestSyncUsers message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	public static MessageRequestSyncUsers decode(FriendlyByteBuf buffer) {
		return new MessageRequestSyncUsers(buffer.readBlockPos());
	}

	public static void handle(MessageRequestSyncUsers message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof ItemTraderBlockEntity)
				{
					ItemTraderBlockEntity trader = (ItemTraderBlockEntity)blockEntity;
					LightmansCurrencyPacketHandler.instance.reply(new MessageSyncUsers(message.pos, trader.getUserCount()), supplier.get());
				}
			}
			
		});
		supplier.get().setPacketHandled(true);
	}

}
