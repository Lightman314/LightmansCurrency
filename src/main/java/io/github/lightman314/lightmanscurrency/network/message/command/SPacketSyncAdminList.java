package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class SPacketSyncAdminList extends ServerToClientPacket {

	private static final Type<SPacketSyncAdminList> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"s_sync_admin_list"));
	public static final Handler<SPacketSyncAdminList> HANDLER = new H();

	List<UUID> adminList;
	
	public SPacketSyncAdminList(List<UUID> adminList) { super(TYPE); this.adminList = adminList; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncAdminList message) {
		buffer.writeInt(message.adminList.size());
		for(UUID id : message.adminList)
			buffer.writeUUID(id);
	}
	private static SPacketSyncAdminList decode(@Nonnull FriendlyByteBuf buffer) {
		int count = buffer.readInt();
		List<UUID> result = new ArrayList<>();
		for(int i = 0; i < count; ++i)
			result.add(buffer.readUUID());
		return new SPacketSyncAdminList(result);
	}

	private static class H extends Handler<SPacketSyncAdminList>
	{
		protected H() { super(TYPE, easyCodec(SPacketSyncAdminList::encode,SPacketSyncAdminList::decode)); }

		@Override
		protected void handle(@Nonnull SPacketSyncAdminList message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			LightmansCurrency.getProxy().loadAdminPlayers(message.adminList);
		}
	}

}
