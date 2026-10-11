package io.github.lightman314.lightmanscurrency.network.message.block_entity;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncBlockEntity extends ServerToClientPacket {

    public static final Type<SPacketSyncBlockEntity> TYPE = sType("sync_block_entity");

    public static final StreamCodec<RegistryFriendlyByteBuf,SPacketSyncBlockEntity> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,p -> p.pos,
            FancyPacketMap.STREAM_CODEC,p -> p.packet,
            SPacketSyncBlockEntity::new);

    private final BlockPos pos;
    private final FancyPacketMap packet;
    public SPacketSyncBlockEntity(BlockPos pos, FancyPacketMap packet) {
        super(TYPE);
        this.pos = pos;
        this.packet = packet.immutable();
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(player.level().getBlockEntity(this.pos) instanceof EasyBlockEntity.Synced be && be.isClient())
            be.handleSyncPacket(this.packet);
    }

}
