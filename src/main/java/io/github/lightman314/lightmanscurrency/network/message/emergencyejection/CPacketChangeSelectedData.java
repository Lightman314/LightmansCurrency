package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderRecoveryMenu;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CPacketChangeSelectedData {

	final int newSelection;
	
	public CPacketChangeSelectedData(int newSelection) { this.newSelection = newSelection; }
	
	public static void encode(CPacketChangeSelectedData message, PacketBuffer buffer) {
		buffer.writeInt(message.newSelection);
	}
	
	public static CPacketChangeSelectedData decode(PacketBuffer buffer) {
		return new CPacketChangeSelectedData(buffer.readInt());
	}
	
	public static void handle(CPacketChangeSelectedData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderRecoveryMenu)
			{
				TraderRecoveryMenu menu = (TraderRecoveryMenu)player.containerMenu;
				menu.changeSelection(message.newSelection);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
