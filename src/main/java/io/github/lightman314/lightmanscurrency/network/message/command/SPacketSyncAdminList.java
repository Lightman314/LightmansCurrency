package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncAdminList extends ServerToClientPacket {

	private static final Type<SPacketSyncAdminList> TYPE = new Type<>(LightmansCurrency.id("s_sync_admin_list"));
    private static final StreamCodec<ByteBuf,SPacketSyncAdminList> STREAM_CODEC = UUIDUtil.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(SPacketSyncAdminList::new,p -> p.adminList);
	public static final Handler<SPacketSyncAdminList> HANDLER = new H();

	List<UUID> adminList;
	
	public SPacketSyncAdminList(List<UUID> adminList) { super(TYPE); this.adminList = adminList; }

	private static class H extends Handler<SPacketSyncAdminList>
	{
		protected H() { super(TYPE,STREAM_CODEC); }

		@Override
		protected void handle(SPacketSyncAdminList message, IPayloadContext context, Player player) {
			LightmansCurrency.getProxy().loadAdminPlayers(message.adminList);
		}
	}

}
