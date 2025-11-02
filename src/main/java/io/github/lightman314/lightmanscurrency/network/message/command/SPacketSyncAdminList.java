package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncAdminList extends ServerToClientPacket {

	public static final Handler<SPacketSyncAdminList> HANDLER = new H();

	List<UUID> adminList;
	
	public SPacketSyncAdminList(List<UUID> adminList) { this.adminList = adminList; }
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeInt(this.adminList.size());
		for(UUID entry : this.adminList)
			buffer.writeUUID(entry);
	}

	private static class H extends Handler<SPacketSyncAdminList>
	{
		@Override
		public SPacketSyncAdminList decode(FriendlyByteBuf buffer) {
			int entryCount = buffer.readInt();
			List<UUID> entries = new ArrayList<>();
			for(int i = 0; i < entryCount; ++i)
				entries.add(buffer.readUUID());
			return new SPacketSyncAdminList(entries);
		}
		@Override
		protected void handle(SPacketSyncAdminList message, Player player) {
			LightmansCurrency.getProxy().loadAdminPlayers(message.adminList);
		}
	}

}
