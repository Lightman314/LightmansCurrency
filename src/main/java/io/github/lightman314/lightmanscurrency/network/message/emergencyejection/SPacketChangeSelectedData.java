package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.TraderRecoveryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketChangeSelectedData {

	final int newSelection;
	
	public SPacketChangeSelectedData(int newSelection) { this.newSelection = newSelection; }
	
	public static void encode(SPacketChangeSelectedData message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.newSelection);
	}
	
	public static SPacketChangeSelectedData decode(FriendlyByteBuf buffer) {
		return new SPacketChangeSelectedData(buffer.readInt());
	}
	
	public static void handle(SPacketChangeSelectedData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if(mc != null && mc.player != null && mc.player.containerMenu instanceof TraderRecoveryMenu)
			{
				TraderRecoveryMenu menu = (TraderRecoveryMenu)mc.player.containerMenu;
				menu.changeSelection(message.newSelection);
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
