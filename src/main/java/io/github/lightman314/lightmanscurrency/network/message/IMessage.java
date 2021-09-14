package io.github.lightman314.lightmanscurrency.network.message;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public interface IMessage<T> {
	
	void encode(T message, FriendlyByteBuf buffer);
	
	T decode(FriendlyByteBuf buffer);
	
	void handle(T message, Supplier<NetworkEvent.Context> supplier);

}
