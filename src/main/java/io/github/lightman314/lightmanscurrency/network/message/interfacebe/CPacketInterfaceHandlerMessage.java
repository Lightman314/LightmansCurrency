package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketInterfaceHandlerMessage extends ClientToServerPacket {

	public static final Handler<CPacketInterfaceHandlerMessage> HANDLER = new H();

	private static final int MAX_TYPE_LENGTH = 100;
	
	BlockPos pos;
	ResourceLocation type;
	CompoundTag updateInfo;
	
	public CPacketInterfaceHandlerMessage(BlockPos pos, ResourceLocation type, CompoundTag updateInfo)
	{
		this.pos = pos;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeUtf(this.type.toString(), MAX_TYPE_LENGTH);
		buffer.writeNbt(this.updateInfo);
	}

	private static class H extends Handler<CPacketInterfaceHandlerMessage>
	{
		@Nonnull
		@Override
		public CPacketInterfaceHandlerMessage decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketInterfaceHandlerMessage(buffer.readBlockPos(), VersionUtil.parseResource(buffer.readUtf(MAX_TYPE_LENGTH)), buffer.readAnySizeNbt()); }
		@Override
		protected void handle(@Nonnull CPacketInterfaceHandlerMessage message, @Nullable ServerPlayer sender) {
			if(sender != null && sender.level().getBlockEntity(message.pos) instanceof TraderInterfaceBlockEntity interfaceBE)
				interfaceBE.receiveHandlerMessage(message.type, sender, message.updateInfo);
		}
	}

}
