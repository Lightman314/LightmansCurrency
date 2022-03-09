package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageToggleInteractionActive {
	
	BlockPos pos;
	boolean isActive;
	
	public MessageToggleInteractionActive(BlockPos pos, boolean isActive)
	{
		this.pos = pos;
		this.isActive = isActive;
	}
	
	public static void encode(MessageToggleInteractionActive message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeBoolean(message.isActive);
	}

	public static MessageToggleInteractionActive decode(FriendlyByteBuf buffer) {
		return new MessageToggleInteractionActive(buffer.readBlockPos(), buffer.readBoolean());
	}

	public static void handle(MessageToggleInteractionActive message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof UniversalTraderInterfaceBlockEntity<?>)
				{
					UniversalTraderInterfaceBlockEntity<?> interfaceBE = (UniversalTraderInterfaceBlockEntity<?>)blockEntity;
					if(interfaceBE.interactionActive() != message.isActive)
						interfaceBE.toggleActive();
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
