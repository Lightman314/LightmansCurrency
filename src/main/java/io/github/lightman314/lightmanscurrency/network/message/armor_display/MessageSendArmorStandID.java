package io.github.lightman314.lightmanscurrency.network.message.armor_display;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSendArmorStandID {

	BlockPos pos;
	int entityID;
	
	public MessageSendArmorStandID(BlockPos pos, int entityID) {
		this.pos = pos;
		this.entityID = entityID;
	}
	
	public static void encode(MessageSendArmorStandID message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.entityID);
	}
	
	public static MessageSendArmorStandID decode(PacketBuffer buffer) {
		return new MessageSendArmorStandID(buffer.readBlockPos(), buffer.readInt());
	}
	
	public static void handle(MessageSendArmorStandID message, Supplier<NetworkEvent.Context> source) {
		source.get().enqueueWork(() ->{
			ArmorDisplayTraderBlockEntity.receiveArmorStandID(message.pos, message.entityID);
		});
		source.get().setPacketHandled(true);
	}
	
}
