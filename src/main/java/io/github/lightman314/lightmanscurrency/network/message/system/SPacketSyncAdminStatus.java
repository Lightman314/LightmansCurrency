package io.github.lightman314.lightmanscurrency.network.message.system;

import io.github.lightman314.lightmanscurrency.client.features.admin.ClientAdminMode;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncAdminStatus extends ServerToClientPacket {

    public static final Type<SPacketSyncAdminStatus> TYPE = sType("sync_admin_status");
    public static final StreamCodec<ByteBuf,SPacketSyncAdminStatus> STREAM_CODEC = ByteBufCodecs.BOOL.map(SPacketSyncAdminStatus::new,p -> p.isAdmin);

    private final boolean isAdmin;
    public SPacketSyncAdminStatus(boolean isAdmin) { super(TYPE); this.isAdmin = isAdmin; }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        ClientAdminMode.isClientAdmin = this.isAdmin;
    }

}