package io.github.lightman314.lightmanscurrency.network.message.armor_display;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.ArmorDisplayTraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageSendArmorStandID {

	BlockPos pos;
	int entityID;
	
	public MessageSendArmorStandID(BlockPos pos, int entityID) {
		this.pos = pos;
		this.entityID = entityID;
	}
	
	public static void encode(MessageSendArmorStandID message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.entityID);
	}
	
	public static MessageSendArmorStandID decode(FriendlyByteBuf buffer) {
		return new MessageSendArmorStandID(buffer.readBlockPos(), buffer.readInt());
	}
	
	public static void handle(MessageSendArmorStandID message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			ArmorDisplayTraderBlockEntity.receiveArmorStandID(message.pos, message.entityID);
		});
		source.get().setPacketHandled(true);
	}
	
}
