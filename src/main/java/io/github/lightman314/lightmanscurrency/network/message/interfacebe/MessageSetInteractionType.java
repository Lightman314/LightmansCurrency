package io.github.lightman314.lightmanscurrency.network.message.interfacebe;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity.InteractionType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSetInteractionType {
	
	BlockPos pos;
	InteractionType interactionType;
	
	public MessageSetInteractionType(BlockPos pos, InteractionType interactionType)
	{
		this.pos = pos;
		this.interactionType = interactionType;
	}
	
	public static void encode(MessageSetInteractionType message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.interactionType.index);
	}

	public static MessageSetInteractionType decode(FriendlyByteBuf buffer) {
		return new MessageSetInteractionType(buffer.readBlockPos(), InteractionType.fromIndex(buffer.readInt()));
	}

	public static void handle(MessageSetInteractionType message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof UniversalTraderInterfaceBlockEntity<?>)
				{
					UniversalTraderInterfaceBlockEntity<?> interfaceBE = (UniversalTraderInterfaceBlockEntity<?>)blockEntity;
					if(interfaceBE.isOwner(player))
						interfaceBE.setInteractionType(message.interactionType);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
