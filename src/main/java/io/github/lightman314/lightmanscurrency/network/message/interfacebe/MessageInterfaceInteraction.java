package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageInterfaceInteraction {

	CompoundNBT message;
	
	public MessageInterfaceInteraction(CompoundNBT message) { this.message = message; }
	
	public static void encode(MessageInterfaceInteraction message, PacketBuffer buffer) {
		buffer.writeNbt(message.message);
	}
	
	public static MessageInterfaceInteraction decode(PacketBuffer buffer) {
		return new MessageInterfaceInteraction(buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageInterfaceInteraction message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->{
			PlayerEntity player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderInterfaceMenu)
			{
				TraderInterfaceMenu menu = (TraderInterfaceMenu)player.containerMenu;
				menu.receiveMessage(message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
