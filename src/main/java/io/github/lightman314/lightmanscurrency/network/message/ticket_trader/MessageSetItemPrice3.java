package io.github.lightman314.lightmanscurrency.network.message.ticket_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.TicketTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.tradedata.TicketTradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetItemPrice3 implements IMessage<MessageSetItemPrice3> {

	private BlockPos pos;
	private int tradeIndex;
	private CoinValue newPrice;
	private boolean isFree;
	private String customName;
	String newDirection;
	
	public MessageSetItemPrice3()
	{
		
	}
	
	public MessageSetItemPrice3(BlockPos pos, int tradeIndex, CoinValue newPrice, boolean isFree, String customName, String newDirection)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newPrice = newPrice;
		this.isFree = isFree;
		this.customName = customName;
		this.newDirection = newDirection;
	}
	
	
	@Override
	public void encode(MessageSetItemPrice3 message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.newPrice.writeToNBT(new CompoundNBT(), CoinValue.DEFAULT_KEY));
		buffer.writeBoolean(message.isFree);
		buffer.writeString(message.customName);
		buffer.writeString(message.newDirection);
	}

	@Override
	public MessageSetItemPrice3 decode(PacketBuffer buffer) {
		return new MessageSetItemPrice3(buffer.readBlockPos(), buffer.readInt(), new CoinValue(buffer.readCompoundTag()), buffer.readBoolean(), buffer.readString(ItemTradeData.MAX_CUSTOMNAME_LENGTH), buffer.readString(ItemTradeData.MaxTradeDirectionStringLength()));
	}

	@Override
	public void handle(MessageSetItemPrice3 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity != null)
				{
					if(tileEntity instanceof TicketTraderTileEntity)
					{
						TicketTraderTileEntity traderEntity = (TicketTraderTileEntity)tileEntity;
						traderEntity.getTrade(message.tradeIndex).setCost(message.newPrice);
						traderEntity.getTrade(message.tradeIndex).setFree(message.isFree);
						traderEntity.getTrade(message.tradeIndex).setCustomName(message.customName);
						traderEntity.getTrade(message.tradeIndex).setTradeDirection(TicketTradeData.loadTradeDirection(message.newDirection));
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
