package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.events.ItemTradeEditEvent.ItemTradeItemEditEvent;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSetTradeItem implements IMessage<MessageSetTradeItem> {

	private BlockPos pos;
	private int tradeIndex;
	private ItemStack newItem;
	int slot;
	
	public MessageSetTradeItem()
	{
		
	}
	
	public MessageSetTradeItem(BlockPos pos, int tradeIndex, ItemStack newItem, int slot)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newItem = newItem;
		this.slot = slot;
	}
	
	
	@Override
	public void encode(MessageSetTradeItem message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeCompoundTag(message.newItem.write(new CompoundNBT()));
		buffer.writeInt(message.slot);
	}

	@Override
	public MessageSetTradeItem decode(PacketBuffer buffer) {
		return new MessageSetTradeItem(buffer.readBlockPos(), buffer.readInt(), ItemStack.read(buffer.readCompoundTag()), buffer.readInt());
	}

	@Override
	public void handle(MessageSetTradeItem message, Supplier<Context> supplier) {
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
						if(!traderEntity.hasPermission(entity, Permissions.EDIT_TRADES))
							return;
						ItemStack oldItem = ItemStack.EMPTY;
						if(message.slot == 1)
						{
							oldItem = traderEntity.getTrade(message.tradeIndex).getBarterItem();
							traderEntity.getTrade(message.tradeIndex).setBarterItem(message.newItem);
						}
						else
						{
							oldItem = traderEntity.getTrade(message.tradeIndex).getSellItem();
							traderEntity.getTrade(message.tradeIndex).setSellItem(message.newItem);
						}
						
						//Post ItemTradeEditEvent
						ItemTradeItemEditEvent e = new ItemTradeItemEditEvent(() -> {
							//Create safe supplier, just in case the event saves it for later
							TileEntity te = entity.world.getTileEntity(message.pos);
							if(te instanceof ItemTraderTileEntity)
								return (ItemTraderTileEntity)te;
							return null;
						}, message.tradeIndex, oldItem, message.slot);
						MinecraftForge.EVENT_BUS.post(e);
						
						
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
