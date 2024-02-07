package io.github.lightman314.lightmanscurrency.network.message.event;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SPacketSyncEventUnlocks extends ServerToClientPacket {

    public static final Handler<SPacketSyncEventUnlocks> HANDLER = new H();

    final List<String> unlocks;
    public SPacketSyncEventUnlocks(@Nonnull List<String> unlocks) { this.unlocks = unlocks; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeInt(this.unlocks.size());
        for(String v : this.unlocks)
            buffer.writeUtf(v);
    }

    private static class H extends Handler<SPacketSyncEventUnlocks>
    {

        @Nonnull
        @Override
        public SPacketSyncEventUnlocks decode(@Nonnull FriendlyByteBuf buffer) {
            List<String> list = new ArrayList<>();
            int count = buffer.readInt();
            for(int i = 0; i < count; ++i)
                list.add(buffer.readUtf());
            return new SPacketSyncEventUnlocks(list);
        }

        @Override
        protected void handle(@Nonnull SPacketSyncEventUnlocks message, @Nullable ServerPlayer sender) {
            LightmansCurrency.PROXY.syncEventUnlocks(message.unlocks);
        }

    }

}
