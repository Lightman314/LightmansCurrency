package io.github.lightman314.lightmanscurrency.network.message.block_entity;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketRequestBlockEntityPacket extends ClientToServerPacket {

    public static final Type<CPacketRequestBlockEntityPacket> TYPE = cType("request_block_entity_sync");
    public static final StreamCodec<ByteBuf,CPacketRequestBlockEntityPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(CPacketRequestBlockEntityPacket::new,p -> p.pos);

    private final BlockPos pos;
    public CPacketRequestBlockEntityPacket(BlockPos pos) {
        super(TYPE);
        this.pos = pos;
    }

    @Override
    protected void handle(IPayloadContext context,Player player) {
        if(player.level().getBlockEntity(this.pos) instanceof EasyBlockEntity.Synced be) {
            FancyPacketMap.Mutable response = FancyPacketMap.map();
            be.writeInitialPacket(response,player);
            if(!response.isEmpty())
                context.reply(new SPacketSyncBlockEntity(this.pos,response));
        }
    }

}
