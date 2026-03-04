package io.github.lightman314.lightmanscurrency.network.message;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketRequestNBT extends ClientToServerPacket {

	private static final Type<CPacketRequestNBT> TYPE = cType("request_block_nbt");
    private static final StreamCodec<ByteBuf,CPacketRequestNBT> STREAM_CODEC = BlockPos.STREAM_CODEC
            .map(CPacketRequestNBT::new,p -> p.pos);
	public static final Handler<CPacketRequestNBT> HANDLER = new H();

	private final BlockPos pos;
	
	public CPacketRequestNBT(BlockPos pos) { super(TYPE); this.pos = pos; }

	private static class H extends Handler<CPacketRequestNBT>
	{
		protected H() { super(TYPE,STREAM_CODEC); }
		@Override
		protected void handle(CPacketRequestNBT message, IPayloadContext context, Player player) {
			BlockEntity blockEntity = player.level().getBlockEntity(message.pos);
			if(blockEntity != null)
				BlockEntityUtil.sendUpdatePacket(blockEntity,player);
		}
	}

}
