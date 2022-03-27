package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetItemPrice {

	private BlockPos pos;
	private int tradeIndex;
	private CoinValue newPrice;
	private String customName;
	String newDirection;
	
	public MessageSetItemPrice(BlockPos pos, int tradeIndex, CoinValue newPrice, String customName, String newDirection)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	public static void encode(MessageSetItemPrice message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.newPrice.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeUtf(message.customName);
		buffer.writeUtf(message.newDirection);
	}

	public static MessageSetItemPrice decode(FriendlyByteBuf buffer) {
		return new MessageSetItemPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readNbt()), buffer.readUtf(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readUtf(ItemTradeData.MaxTradeTypeStringLength()));
	}

	public static void handle(MessageSetItemPrice message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					if(blockEntity instanceof ItemTraderBlockEntity)
					{
						ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)blockEntity;
						traderEntity.getTrade(message.tradeIndex).setCost(message.newPrice);
						traderEntity.getTrade(message.tradeIndex).setCustomName(0, message.customName);
						traderEntity.getTrade(message.tradeIndex).setTradeType(ItemTradeData.loadTradeType(message.newDirection));
						
						traderEntity.markTradesDirty();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
