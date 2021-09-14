package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
		//LightmansCurrency.LogInfo("Setting custom name to " + customName + " (CLIENT)");
	}
	
	@Override
	public void encode(MessageSetCustomName message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUtf(message.customName);
	}

	@Override
	public MessageSetCustomName decode(FriendlyByteBuf buffer) {
		return new MessageSetCustomName(buffer.readBlockPos(), buffer.readUtf());
	}

	@Override
	public void handle(MessageSetCustomName message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				Level level = entity.level;
				if(level != null)
				{
					BlockEntity blockEntity = level.getBlockEntity(message.pos);
					if(blockEntity instanceof TraderBlockEntity)
					{
						TraderBlockEntity traderEntity = (TraderBlockEntity)blockEntity;
						traderEntity.setCustomName(message.customName);
						//LightmansCurrency.LogInfo("Setting custom name to " + message.customName + " (SERVER)");
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
