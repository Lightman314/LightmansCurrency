package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketInterfaceInteraction extends ClientToServerPacket {

	public static final Handler<CPacketInterfaceInteraction> HANDLER = new H();

	CompoundTag message;
	
	public CPacketInterfaceInteraction(CompoundTag message) { this.message = message; }
	
	public void encode(@Nonnull FriendlyByteBuf buffer) {
		buffer.writeNbt(this.message);
	}

	private static class H extends Handler<CPacketInterfaceInteraction>
	{
		@Nonnull
		@Override
		public CPacketInterfaceInteraction decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketInterfaceInteraction(buffer.readNbt()); }
		@Override
		protected void handle(@Nonnull CPacketInterfaceInteraction message, @Nullable ServerPlayer sender) {
			if(sender != null && sender.containerMenu instanceof TraderInterfaceMenu menu)
				menu.receiveMessage(message.message);
		}
	}
	
}
