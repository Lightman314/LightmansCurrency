package io.github.lightman314.lightmanscurrency.network.message.command;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.ClientLCAdminMode;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncAdminList extends ServerToClientPacket {

	private static final Type<SPacketSyncAdminList> TYPE = new Type<>(LightmansCurrency.id("s_sync_admin_list"));
    private static final StreamCodec<ByteBuf,SPacketSyncAdminList> STREAM_CODEC = ByteBufCodecs.BOOL
            .map(SPacketSyncAdminList::new,p -> p.isAdmin);
	public static final Handler<SPacketSyncAdminList> HANDLER = new H();

	private final boolean isAdmin;
	
	public SPacketSyncAdminList(boolean isAdmin) { super(TYPE); this.isAdmin = isAdmin; }

	private static class H extends Handler<SPacketSyncAdminList>
	{
		protected H() { super(TYPE,STREAM_CODEC); }

		@Override
		protected void handle(SPacketSyncAdminList message, IPayloadContext context, Player player) {
            ClientLCAdminMode.handleAdminSyncPacket(message.isAdmin);
		}
	}

}
