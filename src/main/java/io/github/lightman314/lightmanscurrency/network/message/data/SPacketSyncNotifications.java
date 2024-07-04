package io.github.lightman314.lightmanscurrency.network.message.data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncNotifications extends ServerToClientPacket {

	private static final Type<SPacketSyncNotifications> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_notification_sync"));
	public static final Handler<SPacketSyncNotifications> HANDLER = new H();

	private final NotificationData data;
	
	public SPacketSyncNotifications(NotificationData data) { super(TYPE); this.data = data; }

	private static void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull SPacketSyncNotifications message) { buffer.writeNbt(message.data.save(buffer.registryAccess())); }
	private static SPacketSyncNotifications decode(@Nonnull RegistryFriendlyByteBuf buffer) { return new SPacketSyncNotifications(NotificationData.loadFrom(readNBT(buffer),buffer.registryAccess())); }

	private static class H extends Handler<SPacketSyncNotifications>
	{
		protected H() { super(TYPE, fancyCodec(SPacketSyncNotifications::encode,SPacketSyncNotifications::decode)); }
		@Override
		protected void handle(@Nonnull SPacketSyncNotifications message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.PROXY.updateNotifications(message.data);
		}
	}
	
}
