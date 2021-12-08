package io.github.lightman314.lightmanscurrency.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public interface IMessage<T> {

	public void encode(T message, FriendlyByteBuf buffer);
	
	public T decode(FriendlyByteBuf buffer);
	
	public void handle(T message, Supplier<Context> supplier);
	
}
