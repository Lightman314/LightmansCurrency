package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageUpdatePaygateData implements IMessage<MessageUpdatePaygateData> {

	private BlockPos pos;
	private CoinValue newPrice;
	private int newDuration;
	
	public MessageUpdatePaygateData()
	{
		
	}
	
	public MessageUpdatePaygateData(BlockPos pos, CoinValue newPrice, int newDuration)
	{
		this.pos = pos;
		this.newPrice = newPrice;
		this.newDuration = newDuration;
	}
	
	
	@Override
	public void encode(MessageUpdatePaygateData message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeNbt(message.newPrice.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeInt(message.newDuration);
	}

	@Override
	public MessageUpdatePaygateData decode(FriendlyByteBuf buffer) {
		return new MessageUpdatePaygateData(buffer.readBlockPos(), new CoinValue(buffer.readNbt()), buffer.readInt());
	}

	@Override
	public void handle(MessageUpdatePaygateData message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				BlockEntity blockEntity = entity.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					if(blockEntity instanceof PaygateBlockEntity)
					{
						PaygateBlockEntity paygateEntity = (PaygateBlockEntity)blockEntity;
						paygateEntity.setPrice(message.newPrice);
						paygateEntity.setDuration(message.newDuration);
						
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
