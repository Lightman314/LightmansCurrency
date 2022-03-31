package io.github.lightman314.lightmanscurrency.network.message.item_trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.ItemEditMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

@Deprecated
public class MessageItemEditSet {
	
	private ItemStack item;
	private int slot;
	
	public MessageItemEditSet(ItemStack item, int slot)
	{
		this.item = item;
		this.slot = slot;
	}
	
	public static void encode(MessageItemEditSet message, FriendlyByteBuf buffer) {
		buffer.writeItemStack(message.item, false);
		buffer.writeInt(message.slot);
	}

	public static MessageItemEditSet decode(FriendlyByteBuf buffer) {
		return new MessageItemEditSet(buffer.readItem(), buffer.readInt());
	}

	public static void handle(MessageItemEditSet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//CurrencyMod.LOGGER.info("Price Change Message Recieved");
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ItemEditMenu)
				{
					ItemEditMenu menu = (ItemEditMenu)player.containerMenu;
					menu.setItem(message.item, message.slot);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
