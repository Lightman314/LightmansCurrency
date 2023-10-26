package io.github.lightman314.lightmanscurrency.network.message;

import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketRequestNBT extends ClientToServerPacket {

	public static final Handler<CPacketRequestNBT> HANDLER = new H();

	private final BlockPos pos;
	
	public CPacketRequestNBT(BlockPos pos) { this.pos = pos; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeBlockPos(this.pos); }

	private static class H extends Handler<CPacketRequestNBT>
	{
		@Nonnull
		@Override
		public CPacketRequestNBT decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketRequestNBT(buffer.readBlockPos()); }
		@Override
		protected void handle(@Nonnull CPacketRequestNBT message, @Nullable ServerPlayer sender) {
			if(sender != null)
			{
				BlockEntity blockEntity = sender.level.getBlockEntity(message.pos);
				if(blockEntity != null)
					BlockEntityUtil.sendUpdatePacket(blockEntity);
			}
		}
	}

}
