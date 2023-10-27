package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SPacketSyncEjectionData extends ServerToClientPacket {

	public static final Handler<SPacketSyncEjectionData> HANDLER = new H();

	final CompoundTag data;
	
	public SPacketSyncEjectionData(CompoundTag data) { this.data = data; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeNbt(this.data); }

	private static class H extends Handler<SPacketSyncEjectionData>
	{
		@Nonnull
		@Override
		public SPacketSyncEjectionData decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncEjectionData(buffer.readAnySizeNbt()); }
		@Override
		protected void handle(@Nonnull SPacketSyncEjectionData message, @Nullable ServerPlayer sender) {
			LightmansCurrency.PROXY.receiveEmergencyEjectionData(message.data);
		}
	}
	
}
