package io.github.lightman314.lightmanscurrency.network.message.event;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketSyncEventUnlocks extends ServerToClientPacket {

    public static final Handler<SPacketSyncEventUnlocks> HANDLER = new H();

    final List<String> unlocks;
    public SPacketSyncEventUnlocks(List<String> unlocks) { this.unlocks = unlocks; }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.unlocks.size());
        for(String v : this.unlocks)
            buffer.writeUtf(v);
    }

    private static class H extends Handler<SPacketSyncEventUnlocks>
    {

        @Override
        public SPacketSyncEventUnlocks decode(FriendlyByteBuf buffer) {
            List<String> list = new ArrayList<>();
            int count = buffer.readInt();
            for(int i = 0; i < count; ++i)
                list.add(buffer.readUtf());
            return new SPacketSyncEventUnlocks(list);
        }

        @Override
        protected void handle(SPacketSyncEventUnlocks message, Player player) {
            LightmansCurrency.getProxy().syncEventUnlocks(message.unlocks);
        }

    }

}
