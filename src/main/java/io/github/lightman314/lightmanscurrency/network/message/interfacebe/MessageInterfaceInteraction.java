package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageInterfaceInteraction {

	CompoundTag message;
	
	public MessageInterfaceInteraction(CompoundTag message) { this.message = message; }
	
	public static void encode(MessageInterfaceInteraction message, FriendlyByteBuf buffer) { 
		buffer.writeNbt(message.message);
	}
	
	public static MessageInterfaceInteraction decode(FriendlyByteBuf buffer) {
		return new MessageInterfaceInteraction(buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageInterfaceInteraction message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Player player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderInterfaceMenu)
			{
				TraderInterfaceMenu menu = (TraderInterfaceMenu)player.containerMenu;
				menu.receiveMessage(message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
