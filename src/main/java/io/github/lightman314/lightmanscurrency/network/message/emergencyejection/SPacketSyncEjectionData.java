package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SPacketSyncEjectionData {

	final CompoundNBT data;
	
	public SPacketSyncEjectionData(CompoundNBT data) { this.data = data; }
	
	public static void encode(SPacketSyncEjectionData message, PacketBuffer buffer) {
		buffer.writeNbt(message.data);
	}
	
	public static SPacketSyncEjectionData decode(PacketBuffer buffer) {
		return new SPacketSyncEjectionData(buffer.readAnySizeNbt());
	}
	
	public static void handle(SPacketSyncEjectionData message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() -> {
			LightmansCurrency.PROXY.receiveEmergencyEjectionData(message.data);
		});
		supplier.get().setPacketHandled(true);
	}
	
}