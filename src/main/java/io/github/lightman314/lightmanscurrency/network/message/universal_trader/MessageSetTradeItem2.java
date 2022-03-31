package io.github.lightman314.lightmanscurrency.network.message.universal_trader;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalItemTraderData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

@Deprecated
public class MessageSetTradeItem2 {

	private UUID traderID;
	private int tradeIndex;
	ItemStack newItem;
	int slot;
	
	public MessageSetTradeItem2(UUID traderID, int tradeIndex, ItemStack newItem, int slot)
	{
		this.traderID = traderID;
		this.tradeIndex = tradeIndex;
		this.newItem = newItem;
		this.slot = slot;
	}
	
	public static void encode(MessageSetTradeItem2 message, FriendlyByteBuf buffer) {
		buffer.writeUUID(message.traderID);
		buffer.writeInt(message.tradeIndex);
		buffer.writeItemStack(message.newItem, false);
		buffer.writeInt(message.slot);
	}

	public static MessageSetTradeItem2 decode(FriendlyByteBuf buffer) {
		return new MessageSetTradeItem2(buffer.readUUID(), buffer.readInt(), buffer.readItem(), buffer.readInt());
	}

	public static void handle(MessageSetTradeItem2 message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			UniversalTraderData data1 = TradingOffice.getData(message.traderID);
			if(data1 != null && data1 instanceof UniversalItemTraderData)
			{
				UniversalItemTraderData data2 = (UniversalItemTraderData)data1;
				if(!data2.hasPermission(supplier.get().getSender(), Permissions.EDIT_TRADES))
				{
					Settings.PermissionWarning(supplier.get().getSender(), "change trade item", Permissions.EDIT_TRADES);
					return;
				}
				data2.getTrade(message.tradeIndex).setItem(message.newItem, message.slot);
					
				//Mark the trader as dirty
				TradingOffice.MarkDirty(message.traderID);
				
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
