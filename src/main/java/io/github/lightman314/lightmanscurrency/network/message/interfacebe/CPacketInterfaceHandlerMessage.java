package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketInterfaceHandlerMessage extends ClientToServerPacket {

	private static final Type<CPacketInterfaceHandlerMessage> TYPE = new Type<>(VersionUtil.lcResource("c_interface_handler_message"));
	public static final Handler<CPacketInterfaceHandlerMessage> HANDLER = new H();
	
	BlockPos pos;
	ResourceLocation type;
	CompoundTag updateInfo;
	
	public CPacketInterfaceHandlerMessage(BlockPos pos, ResourceLocation type, CompoundTag updateInfo)
	{
		super(TYPE);
		this.pos = pos;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketInterfaceHandlerMessage message) {
		buffer.writeBlockPos(message.pos);
		buffer.writeUtf(message.type.toString());
		buffer.writeNbt(message.updateInfo);
	}
	private static CPacketInterfaceHandlerMessage decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketInterfaceHandlerMessage(buffer.readBlockPos(),ResourceLocation.parse(buffer.readUtf()),readNBT(buffer)); }

	private static class H extends Handler<CPacketInterfaceHandlerMessage>
	{
		protected H() { super(TYPE, easyCodec(CPacketInterfaceHandlerMessage::encode,CPacketInterfaceHandlerMessage::decode)); }
		@Override
		protected void handle(@Nonnull CPacketInterfaceHandlerMessage message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			if(player.level().getBlockEntity(message.pos) instanceof TraderInterfaceBlockEntity be)
				be.receiveHandlerMessage(message.type, player, message.updateInfo);
		}
	}

}
