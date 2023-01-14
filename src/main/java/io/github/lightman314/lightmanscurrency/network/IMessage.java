package io.github.lightman314.lightmanscurrency.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

@Deprecated
public interface IMessage<T> {

	void encode(T message, FriendlyByteBuf buffer);
	
	T decode(FriendlyByteBuf buffer);
	
	void handle(T message, Supplier<Context> supplier);
	
}
