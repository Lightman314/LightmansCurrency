package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.ItemTradeData;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageSetItemPrice implements IMessage<MessageSetItemPrice> {

	private BlockPos pos;
	private int tradeIndex;
	private CoinValue newPrice;
	private boolean isFree;
	private String customName;
	String newDirection;
	
	public MessageSetItemPrice()
	{
		
	}
	
	public MessageSetItemPrice(BlockPos pos, int tradeIndex, CoinValue newPrice, boolean isFree, String customName, String newDirection)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		this.isFree = isFree;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	
	@Override
	public void encode(MessageSetItemPrice message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.newPrice.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeBoolean(message.isFree);
		buffer.writeUtf(message.customName);
		buffer.writeUtf(message.newDirection);
	}

	@Override
	public MessageSetItemPrice decode(FriendlyByteBuf buffer) {
		return new MessageSetItemPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readNbt()), buffer.readBoolean(), buffer.readUtf(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readUtf(ItemTradeData.MaxTradeDirectionStringLength()));
	}

	@Override
	public void handle(MessageSetItemPrice message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer entity = supplier.get().getSender();
			if(entity != null)
			{
				BlockEntity blockEntity = entity.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					if(blockEntity instanceof ItemTraderBlockEntity)
					{
						ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)blockEntity;
						traderEntity.getTrade(message.tradeIndex).setCost(message.newPrice);
						traderEntity.getTrade(message.tradeIndex).setFree(message.isFree);
						traderEntity.getTrade(message.tradeIndex).setCustomName(message.customName);
						traderEntity.getTrade(message.tradeIndex).setTradeDirection(ItemTradeData.loadTradeDirection(message.newDirection));
						//Send update packet to the clients
						CompoundTag compound = traderEntity.writeTrades(new CompoundTag());
						TileEntityUtil.sendUpdatePacket(blockEntity, traderEntity.superWrite(compound));
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
