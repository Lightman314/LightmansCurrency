package io.github.lightman314.lightmanscurrency.network.message;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketRequestNBT extends ClientToServerPacket {

	private static final Type<CPacketRequestNBT> TYPE = new Type<>(VersionUtil.lcResource("c_request_block_nbt"));
	public static final Handler<CPacketRequestNBT> HANDLER = new H();

	private final BlockPos pos;
	
	public CPacketRequestNBT(BlockPos pos) { super(TYPE); this.pos = pos; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketRequestNBT message) { buffer.writeBlockPos(message.pos); }
	private static CPacketRequestNBT decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketRequestNBT(buffer.readBlockPos()); }

	private static class H extends Handler<CPacketRequestNBT>
	{
		protected H() { super(TYPE,easyCodec(CPacketRequestNBT::encode,CPacketRequestNBT::decode)); }
		@Override
		protected void handle(@Nonnull CPacketRequestNBT message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			BlockEntity blockEntity = player.level().getBlockEntity(message.pos);
			if(blockEntity != null)
				BlockEntityUtil.sendUpdatePacket(blockEntity,player);
		}
	}

}
