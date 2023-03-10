package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageStorageInteractionC {

	CompoundNBT message;
	
	public MessageStorageInteractionC(CompoundNBT message) { this.message = message; }
	
	public static void encode(MessageStorageInteractionC message, PacketBuffer buffer) {
		buffer.writeNbt(message.message);
	}
	
	public static MessageStorageInteractionC decode(PacketBuffer buffer) {
		return new MessageStorageInteractionC(buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageStorageInteractionC message, Supplier<NetworkEvent.Context> supplier) {
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
