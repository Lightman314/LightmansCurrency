package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessagePlayPickupSound {
	
	public static void encode(MessagePlayPickupSound message, FriendlyByteBuf buffer) { }

	public static MessagePlayPickupSound decode(FriendlyByteBuf buffer) {
		return new MessagePlayPickupSound();
	}

	public static void handle(MessagePlayPickupSound message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft minecraft = Minecraft.getInstance();
			if(minecraft != null)
			{
				minecraft.player.level.playSound(minecraft.player, minecraft.player.blockPosition(), CurrencySoundEvents.COINS_CLINKING, SoundSource.PLAYERS, 0.4f, 1f);
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
