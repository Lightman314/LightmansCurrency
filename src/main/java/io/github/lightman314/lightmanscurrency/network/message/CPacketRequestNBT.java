package io.github.lightman314.lightmanscurrency.network.message;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketRequestNBT extends ClientToServerPacket {

	public static final Handler<CPacketRequestNBT> HANDLER = new H();

	private final BlockPos pos;
	
	public CPacketRequestNBT(BlockPos pos) { this.pos = pos; }
	
	public void encode(FriendlyByteBuf buffer) { buffer.writeBlockPos(this.pos); }

	private static class H extends Handler<CPacketRequestNBT>
	{
		@Override
		public CPacketRequestNBT decode(FriendlyByteBuf buffer) { return new CPacketRequestNBT(buffer.readBlockPos()); }
		@Override
		protected void handle(CPacketRequestNBT message, Player player) {
            BlockEntity blockEntity = player.level().getBlockEntity(message.pos);
            if(blockEntity != null)
                BlockEntityUtil.sendUpdatePacket(blockEntity,player);
		}
	}

}
