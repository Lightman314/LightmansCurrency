package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageStorageInteraction {

	CompoundTag message;
	
	public MessageStorageInteraction(CompoundTag message) { this.message = message; }
	
	public static void encode(MessageStorageInteraction message, FriendlyByteBuf buffer) { 
		buffer.writeNbt(message.message);
	}
	
	public static MessageStorageInteraction decode(FriendlyByteBuf buffer) {
		return new MessageStorageInteraction(buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageStorageInteraction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Player player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderStorageMenu)
			{
				TraderStorageMenu menu = (TraderStorageMenu)player.containerMenu;
				menu.receiveMessage(message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
