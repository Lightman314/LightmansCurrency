package io.github.lightman314.lightmanscurrency.network.message.player;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketSendPlayerList {

	final CompoundTag data;
	
	public SPacketSendPlayerList(CompoundTag data) { this.data = data; }
	
	public static void encode(SPacketSendPlayerList message, FriendlyByteBuf buffer) { buffer.writeNbt(message.data); }
	
	public static SPacketSendPlayerList decode(FriendlyByteBuf buffer) { return new SPacketSendPlayerList(buffer.readAnySizeNbt()); }
	
	public static void handle(SPacketSendPlayerList message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			LightmansCurrency.PROXY.processPlayerList(message.data);
		});
		supplier.get().setPacketHandled(true);
	}
	
}
