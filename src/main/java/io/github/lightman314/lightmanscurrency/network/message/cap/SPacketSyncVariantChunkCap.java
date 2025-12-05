package io.github.lightman314.lightmanscurrency.network.message.cap;

import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantChunkDataStorageAttachment.DataHolder;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class SPacketSyncVariantChunkCap extends ServerToClientPacket {

    public static final Handler<SPacketSyncVariantChunkCap> HANDLER = new H();

    private final ChunkPos pos;
    private final Map<BlockPos,DataHolder> data;
    public SPacketSyncVariantChunkCap(ChunkPos pos, Map<BlockPos,DataHolder> data)
    {
        this.pos = pos;
        this.data = data;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeInt(this.data.size());
        this.data.forEach((pos,entry) -> {
            buffer.writeBlockPos(pos);
            entry.encode(buffer);
        });
    }

    private static class H extends Handler<SPacketSyncVariantChunkCap>
    {

        @Override
        public SPacketSyncVariantChunkCap decode(FriendlyByteBuf buffer) {
            ChunkPos chunk = buffer.readChunkPos();
            Map<BlockPos,DataHolder> data = new HashMap<>();
            int count = buffer.readInt();
            while(count-- > 0)
            {
                BlockPos pos = buffer.readBlockPos();
                data.put(pos,DataHolder.decode(buffer));
            }
            return new SPacketSyncVariantChunkCap(chunk,data);
        }

        @Override
        protected void handle(SPacketSyncVariantChunkCap message, Player player) {
            if(player != null)
            {
                BlockPos pos = message.pos.getWorldPosition();
                if(player.level().isLoaded(pos))
                {
                    LevelChunk chunk = player.level().getChunk(message.pos.x,message.pos.z);
                    chunk.getCapability(VariantChunkDataStorageAttachment.CAP).ifPresent(data ->
                        data.loadData(message.data));
                }
            }
        }

    }

}
