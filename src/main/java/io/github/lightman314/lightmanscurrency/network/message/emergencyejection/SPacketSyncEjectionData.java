package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPacketSyncEjectionData {

	final CompoundTag data;
	
	public SPacketSyncEjectionData(CompoundTag data) { this.data = data; }
	
	public static void encode(SPacketSyncEjectionData message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.data);
	}
	
	public static SPacketSyncEjectionData decode(FriendlyByteBuf buffer) {
		return new SPacketSyncEjectionData(buffer.readAnySizeNbt());
	}
	
	public static void handle(SPacketSyncEjectionData message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			LightmansCurrency.PROXY.receiveEmergencyEjectionData(message.data);
		});
		supplier.get().setPacketHandled(true);
	}
	
}