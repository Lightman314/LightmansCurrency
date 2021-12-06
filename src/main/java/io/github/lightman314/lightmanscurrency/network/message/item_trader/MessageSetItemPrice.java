package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.events.TradeEditEvent.TradePriceEditEvent;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetItemPrice {

	private BlockPos pos;
	private int tradeIndex;
	private CoinValue newPrice;
	private boolean isFree;
	private String customName;
	String newDirection;
	
	public MessageSetItemPrice(BlockPos pos, int tradeIndex, CoinValue newPrice, boolean isFree, String customName, String newDirection)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		this.isFree = isFree;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	public static void encode(MessageSetItemPrice message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeNbt(message.newPrice.writeToNBT(new CompoundTag(), CoinValue.DEFAULT_KEY));
		buffer.writeBoolean(message.isFree);
		buffer.writeUtf(message.customName);
		buffer.writeUtf(message.newDirection);
	}

	public static MessageSetItemPrice decode(FriendlyByteBuf buffer) {
		return new MessageSetItemPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readNbt()), buffer.readBoolean(), buffer.readUtf(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readUtf(ItemTradeData.MaxTradeTypeStringLength()));
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
					if(blockEntity instanceof ItemTraderTileEntity)
					{
						ItemTraderTileEntity traderEntity = (ItemTraderTileEntity)blockEntity;
						CoinValue oldPrice = traderEntity.getTrade(message.tradeIndex).getCost();
						boolean wasFree = traderEntity.getTrade(message.tradeIndex).isFree();
						traderEntity.getTrade(message.tradeIndex).setCost(message.newPrice);
						traderEntity.getTrade(message.tradeIndex).setFree(message.isFree);
						traderEntity.getTrade(message.tradeIndex).setCustomName(message.customName);
						traderEntity.getTrade(message.tradeIndex).setTradeType(ItemTradeData.loadTradeType(message.newDirection));
						
						if(oldPrice.getRawValue() != message.newPrice.getRawValue() || wasFree != message.isFree)
						{
							//Throw price change event
							TradePriceEditEvent e = new TradePriceEditEvent(() -> {
								//Create safe supplier, just in case the event saves it for later
								BlockEntity be = player.level.getBlockEntity(message.pos);
								if(be instanceof ItemTraderTileEntity)
									return (ItemTraderTileEntity)be;
								return null;
							}, message.tradeIndex, oldPrice, wasFree);
							MinecraftForge.EVENT_BUS.post(e);
						}
						
						traderEntity.markTradesDirty();
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
