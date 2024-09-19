package io.github.lightman314.lightmanscurrency.network.message.emergencyejection;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncEjectionData extends ServerToClientPacket {

	private static final Type<SPacketSyncEjectionData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_ejection_data_sync"));
	public static final Handler<SPacketSyncEjectionData> HANDLER = new H();

	final CompoundTag data;
	
	public SPacketSyncEjectionData(CompoundTag data) { super(TYPE); this.data = data; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncEjectionData message) { buffer.writeNbt(message.data); }
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeNbt(this.data); }
	private static SPacketSyncEjectionData decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketSyncEjectionData(readNBT(buffer)); }

	private static class H extends Handler<SPacketSyncEjectionData>
	{
		protected H() { super(TYPE, easyCodec(SPacketSyncEjectionData::encode,SPacketSyncEjectionData::decode)); }
		@Override
		protected void handle(@Nonnull SPacketSyncEjectionData message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().receiveEmergencyEjectionData(message.data);
		}
	}
	
}
