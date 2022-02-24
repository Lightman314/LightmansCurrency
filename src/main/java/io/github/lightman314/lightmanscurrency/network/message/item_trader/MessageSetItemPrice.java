package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.events.TradeEditEvent.TradePriceEditEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetItemPrice implements IMessage<MessageSetItemPrice> {

	private BlockPos pos;
	private int tradeIndex;
	private CoinValue newPrice;
	//private boolean isFree;
	private String customName;
	String newDirection;
	
	public MessageSetItemPrice()
	{
		
	}
	
	public MessageSetItemPrice(BlockPos pos, int tradeIndex, CoinValue newPrice, String customName, String newDirection)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		//this.isFree = isFree;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	
	@Override
	public void encode(MessageSetItemPrice message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.newPrice.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		//buffer.writeBoolean(message.isFree);
		buffer.writeString(message.customName);
		buffer.writeString(message.newDirection);
	}

	@Override
	public MessageSetItemPrice decode(PacketBuffer buffer) {
		return new MessageSetItemPrice(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readCompoundTag()), buffer.readString(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readString(ItemTradeData.MaxTradeTypeStringLength()));
	}

	@Override
	public void handle(MessageSetItemPrice message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity != null)
				{
					if(tileEntity instanceof ItemTraderTileEntity)
					{
						ItemTraderTileEntity traderEntity = (ItemTraderTileEntity)tileEntity;
						CoinValue oldPrice = traderEntity.getTrade(message.tradeIndex).getCost();
						traderEntity.getTrade(message.tradeIndex).setCost(message.newPrice);
						//traderEntity.getTrade(message.tradeIndex).setFree(message.isFree);
						traderEntity.getTrade(message.tradeIndex).setCustomName(message.customName);
						traderEntity.getTrade(message.tradeIndex).setTradeType(ItemTradeData.loadTradeType(message.newDirection));
						
						if(oldPrice.getRawValue() != message.newPrice.getRawValue() || oldPrice.isFree() !=message.newPrice.isFree())
						{
							//Throw price change event
							TradePriceEditEvent e = new TradePriceEditEvent(() -> {
								//Create safe supplier, just in case the event saves it for later
								TileEntity te = entity.world.getTileEntity(message.pos);
								if(te instanceof ItemTraderTileEntity)
									return (ItemTraderTileEntity)te;
								return null;
							}, message.tradeIndex, oldPrice);
							MinecraftForge.EVENT_BUS.post(e);
						}
						
						//Send update packet to the clients
						CompoundNBT compound = traderEntity.writeTrades(new CompoundNBT());
						TileEntityUtil.sendUpdatePacket(tileEntity, traderEntity.superWrite(compound));
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
