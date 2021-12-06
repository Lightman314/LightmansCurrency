package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.events.ItemTradeEditEvent.ItemTradeItemEditEvent;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetTradeItem {

	private BlockPos pos;
	private int tradeIndex;
	private ItemStack newItem;
	int slot;
	
	public MessageSetTradeItem(BlockPos pos, int tradeIndex, ItemStack newItem, int slot)
	{
		this.pos = pos;
		this.tradeIndex = tradeIndex;
		this.newItem = newItem;
		this.slot = slot;
	}
	
	public static void encode(MessageSetTradeItem message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.tradeIndex);
		buffer.writeItemStack(message.newItem, false);
		buffer.writeInt(message.slot);
	}

	public static MessageSetTradeItem decode(FriendlyByteBuf buffer) {
		return new MessageSetTradeItem(buffer.readBlockPos(), buffer.readInt(), buffer.readItem(), buffer.readInt());
	}

	public static void handle(MessageSetTradeItem message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity != null)
				{
					if(blockEntity instanceof ItemTraderTileEntity)
					{
						ItemTraderTileEntity traderEntity = (ItemTraderTileEntity)blockEntity;
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
							BlockEntity te = player.level.getBlockEntity(message.pos);
							if(te instanceof ItemTraderTileEntity)
								return (ItemTraderTileEntity)te;
							return null;
						}, message.tradeIndex, oldItem, message.slot);
						MinecraftForge.EVENT_BUS.post(e);
						
						traderEntity.markTradesDirty();
						
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
