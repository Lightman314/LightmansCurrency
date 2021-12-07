package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketOpenVanillaResponse {
	
	public static void encode(SPacketOpenVanillaResponse message, FriendlyByteBuf buffer) { }

	public static SPacketOpenVanillaResponse decode(FriendlyByteBuf buffer) {
		return new SPacketOpenVanillaResponse();
	}

	public static void handle(SPacketOpenVanillaResponse message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				Player player = minecraft.player;
				LightmansCurrency.PROXY.openInventoryScreen(player);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
