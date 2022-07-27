package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.TraderRecoveryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class CPacketChangeSelectedData {

	final int newSelection;
	
	public CPacketChangeSelectedData(int newSelection) { this.newSelection = newSelection; }
	
	public static void encode(CPacketChangeSelectedData message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.newSelection);
	}
	
	public static CPacketChangeSelectedData decode(FriendlyByteBuf buffer) {
		return new CPacketChangeSelectedData(buffer.readInt());
	}
	
	public static void handle(CPacketChangeSelectedData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			ServerPlayer player = supplier.get().getSender();
			if(player != null && player.containerMenu instanceof TraderRecoveryMenu)
			{
				TraderRecoveryMenu menu = (TraderRecoveryMenu)player.containerMenu;
				menu.changeSelection(message.newSelection);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
