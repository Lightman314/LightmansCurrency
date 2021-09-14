package io.github.lightman314.lightmanscurrency.network.message;

import java.util.function.Supplier;

//import io.github.lightman314.currencymod.core.CurrencyMod;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageRequestNBT implements IMessage<MessageRequestNBT> {

	private BlockPos pos;
	
	public MessageRequestNBT()
	{
		
	}
	
	public MessageRequestNBT(BlockEntity blockEntity)
	{
		this.pos = blockEntity.getBlockPos();
	}
	
	public MessageRequestNBT(BlockPos pos)
	{
		this.pos = pos;
	}
	
	
	@Override
	public void encode(MessageRequestNBT message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageRequestNBT decode(FriendlyByteBuf buffer) {
		return new MessageRequestNBT(buffer.readBlockPos());
	}

	@Override
	public void handle(MessageRequestNBT message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("NBT Update Request received.");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				Level level = entity.getLevel();
				BlockEntity blockEntity = level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					TileEntityUtil.sendUpdatePacket(blockEntity);
					//CurrencyMod.LOGGER.info("NBT Update Packet sent.");
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
