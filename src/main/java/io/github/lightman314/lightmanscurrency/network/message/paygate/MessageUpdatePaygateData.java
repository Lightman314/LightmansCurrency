package io.github.lightman314.lightmanscurrency.network.message.paygate;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageUpdatePaygateData {

	private BlockPos pos;
	private CoinValue newPrice;
	private int newDuration;
	
	public MessageUpdatePaygateData(BlockPos pos, CoinValue newPrice, int newDuration)
	{
		this.pos = pos;
		this.newPrice = newPrice;
		this.newDuration = newDuration;
	}
	
	public static void encode(MessageUpdatePaygateData message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeNbt(message.newPrice.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeInt(message.newDuration);
	}

	public static MessageUpdatePaygateData decode(FriendlyByteBuf buffer) {
		return new MessageUpdatePaygateData(buffer.readBlockPos(), new CoinValue(buffer.readNbt()), buffer.readInt());
	}

	public static void handle(MessageUpdatePaygateData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
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
