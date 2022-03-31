package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

@Deprecated
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
					if(blockEntity instanceof ItemTraderBlockEntity)
					{
						ItemTraderBlockEntity traderEntity = (ItemTraderBlockEntity)blockEntity;
						if(!traderEntity.hasPermission(player, Permissions.EDIT_TRADES))
						{
							Settings.PermissionWarning(player, "change trade item", Permissions.EDIT_TRADES);
							return;
						}
						
						traderEntity.getTrade(message.tradeIndex).setItem(message.newItem, message.slot);
						
						traderEntity.markTradesDirty();
						
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
