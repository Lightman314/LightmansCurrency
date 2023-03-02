package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageStorageInteractionC {

	CompoundTag message;
	
	public MessageStorageInteractionC(CompoundTag message) { this.message = message; }
	
	public static void encode(MessageStorageInteractionC message, FriendlyByteBuf buffer) { 
		buffer.writeNbt(message.message);
	}
	
	public static MessageStorageInteractionC decode(FriendlyByteBuf buffer) {
		return new MessageStorageInteractionC(buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageStorageInteractionC message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Minecraft mc = Minecraft.getInstance();
			if(mc != null && mc.player != null && mc.player.containerMenu instanceof TraderStorageMenu)
			{
				TraderStorageMenu menu = (TraderStorageMenu)mc.player.containerMenu;
				menu.receiveMessage(message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
