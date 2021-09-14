package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
	public void encode(MessageUpdatePaygateData message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeCompoundTag(message.newPrice.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		buffer.writeInt(message.newDuration);
	}

	@Override
	public MessageUpdatePaygateData decode(PacketBuffer buffer) {
		return new MessageUpdatePaygateData(buffer.readBlockPos(), new CoinValue(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void handle(MessageUpdatePaygateData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity != null)
				{
					if(tileEntity instanceof PaygateTileEntity)
					{
						
						PaygateTileEntity paygateEntity = (PaygateTileEntity)tileEntity;
						
						paygateEntity.setPrice(message.newPrice);
						paygateEntity.setDuration(message.newDuration);
						
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
