package io.github.lightman314.lightmanscurrency.network.message.misc;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.widget.lockableslot.LockableSlotInterface.ILockableSlotInteractableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageLockableSlotInteraction {
	
	String key;
	int index;
	ItemStack carriedStack;
	
	public MessageLockableSlotInteraction(String key, int index, ItemStack carriedStack)
	{
		this.key = key;
		this.index = index;
		this.carriedStack = carriedStack;
	}
	
	public static void encode(MessageLockableSlotInteraction message, FriendlyByteBuf buffer) {
		buffer.writeUtf(message.key);
		buffer.writeInt(message.index);
		buffer.writeItemStack(message.carriedStack, false);
	}

	public static MessageLockableSlotInteraction decode(FriendlyByteBuf buffer) {
		return new MessageLockableSlotInteraction(buffer.readUtf(), buffer.readInt(), buffer.readItem());
	}

	public static void handle(MessageLockableSlotInteraction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof ILockableSlotInteractableMenu)
				{
					ILockableSlotInteractableMenu menu = (ILockableSlotInteractableMenu)player.containerMenu;
					menu.OnLockableSlotInteraction(message.key, message.index, message.carriedStack);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
