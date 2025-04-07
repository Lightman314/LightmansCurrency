package io.github.lightman314.lightmanscurrency.network.message.event;

import io.github.lightman314.lightmanscurrency.common.attachments.EventUnlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SPacketSyncEventUnlocks extends ServerToClientPacket {

    private static final Type<SPacketSyncEventUnlocks> TYPE = new Type<>(VersionUtil.lcResource("s_sync_event_unlocks"));
    public static final Handler<SPacketSyncEventUnlocks> HANDLER = new H();

    final List<String> unlocks;
    public SPacketSyncEventUnlocks(@Nonnull List<String> unlocks) { super(TYPE); this.unlocks = unlocks; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketSyncEventUnlocks message) {
        buffer.writeInt(message.unlocks.size());
        for(String v : message.unlocks)
            buffer.writeUtf(v);
    }

    private static SPacketSyncEventUnlocks decode(@Nonnull FriendlyByteBuf buffer)
    {
        int count = buffer.readInt();
        List<String> result = new ArrayList<>();
        for(int i = 0; i < count; ++i)
            result.add(buffer.readUtf());
        return new SPacketSyncEventUnlocks(result);
    }

    private static class H extends Handler<SPacketSyncEventUnlocks>
    {
        protected H() { super(TYPE, easyCodec(SPacketSyncEventUnlocks::encode,SPacketSyncEventUnlocks::decode)); }
        @Override
        protected void handle(@Nonnull SPacketSyncEventUnlocks message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            EventUnlocks unlocks = player.getData(ModAttachmentTypes.EVENT_UNLOCKS);
            unlocks.sync(message.unlocks);
        }
    }

}
