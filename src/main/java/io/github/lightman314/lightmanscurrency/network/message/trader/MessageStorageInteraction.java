package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageStorageInteraction {

	CompoundNBT message;
	
	public MessageStorageInteraction(CompoundNBT message) { this.message = message; }
	
	public static void encode(MessageStorageInteraction message, PacketBuffer buffer) {
		buffer.writeNbt(message.message);
	}
	
	public static MessageStorageInteraction decode(PacketBuffer buffer) {
		return new MessageStorageInteraction(buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageStorageInteraction message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->{
			PlayerEntity player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderStorageMenu)
			{
				TraderStorageMenu menu = (TraderStorageMenu)player.containerMenu;
				menu.receiveMessage(message.message);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
