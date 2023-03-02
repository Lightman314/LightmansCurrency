package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessagePlayPickupSound {

	public static final MessagePlayPickupSound INSTANCE = new MessagePlayPickupSound();

	private MessagePlayPickupSound() {}

	public static void encode(MessagePlayPickupSound message, FriendlyByteBuf buffer) { }

	public static MessagePlayPickupSound decode(FriendlyByteBuf buffer) {
		return INSTANCE;
	}

	public static void handle(MessagePlayPickupSound message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(LightmansCurrency.PROXY::playCoinSound);
		supplier.get().setPacketHandled(true);
	}

}
