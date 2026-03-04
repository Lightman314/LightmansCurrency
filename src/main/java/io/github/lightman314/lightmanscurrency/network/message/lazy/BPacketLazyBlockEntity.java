package io.github.lightman314.lightmanscurrency.network.message.lazy;

import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.packet.BiDirectionalPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BPacketLazyBlockEntity extends BiDirectionalPacket {

    private static final Type<BPacketLazyBlockEntity> TYPE = bType("_lazy_be");
    private static final StreamCodec<RegistryFriendlyByteBuf,BPacketLazyBlockEntity> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,p -> p.pos,
            LazyPacketData.STREAM_CODEC,p -> p.data,
            BPacketLazyBlockEntity::new);
    public static final Handler<BPacketLazyBlockEntity> HANDLER = new H();

    private final BlockPos pos;
    private final LazyPacketData data;

    public BPacketLazyBlockEntity(BlockPos pos,LazyPacketData data) {
        super(TYPE);
        this.pos = pos;
        this.data = data;
    }

    private static class H extends Handler<BPacketLazyBlockEntity>
    {
        private H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(BPacketLazyBlockEntity message,IPayloadContext context,Player player) {
            if(player.level().getBlockEntity(message.pos) instanceof EasyBlockEntity be)
                be.handleMessage(player,message.data);
        }
    }

}
