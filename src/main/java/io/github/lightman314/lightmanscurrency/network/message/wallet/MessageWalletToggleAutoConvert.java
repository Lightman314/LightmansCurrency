package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageWalletToggleAutoConvert {
	
	public static void encode(MessageWalletToggleAutoConvert message, PacketBuffer buffer) { }

	public static MessageWalletToggleAutoConvert decode(PacketBuffer buffer) {
		return new MessageWalletToggleAutoConvert();
	}

	public static void handle(MessageWalletToggleAutoConvert message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				if(player.containerMenu instanceof WalletMenuBase)
				{
					WalletMenuBase menu = (WalletMenuBase) player.containerMenu;
					menu.ToggleAutoConvert();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
