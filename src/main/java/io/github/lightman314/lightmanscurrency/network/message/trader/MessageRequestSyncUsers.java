package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageRequestSyncUsers implements IMessage<MessageRequestSyncUsers> {
	
	BlockPos pos;
	
	public MessageRequestSyncUsers()
	{
		
	}
	
	public MessageRequestSyncUsers(BlockPos pos)
	{
		this.pos = pos;
	}
	
	@Override
	public void encode(MessageRequestSyncUsers message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageRequestSyncUsers decode(FriendlyByteBuf buffer) {
		return new MessageRequestSyncUsers(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageRequestSyncUsers message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				Level world = entity.level;
				BlockEntity blockEntity = world.getBlockEntity(message.pos);
				if(blockEntity instanceof ItemTraderBlockEntity)
				{
					ItemTraderBlockEntity trader = (ItemTraderBlockEntity)blockEntity;
					LightmansCurrencyPacketHandler.instance.reply(new MessageSyncUsers(message.pos, trader.getUserCount()), supplier.get());
					//LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(entity), new MessageSyncUsers(message.pos, trader.getUserCount()));
				}
			}
			
		});
		supplier.get().setPacketHandled(true);
	}

}
